package com.prdev.solidaryhands;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.prdev.solidaryhands.ai.DonationScorer;
import com.prdev.solidaryhands.ai.ScoreResult;
import com.prdev.solidaryhands.models.EstadoDonacion;
import com.prdev.solidaryhands.models.DonacionSupabase;
import com.prdev.solidaryhands.models.Fundacion;
import com.prdev.solidaryhands.models.Necesidad;
import com.prdev.solidaryhands.network.SessionManager;
import com.prdev.solidaryhands.network.SupabaseClient;
import com.prdev.solidaryhands.util.ImagenUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings({"unchecked", "deprecation"})
public class DonacionActivity extends Activity {

    // CONTENEDOR PRINCIPAL
    private FrameLayout contenedorPasos;
    private TextView txtNumeroPaso;
    private View progreso;

    private int pasoActual = 1;

    // PASO 1
    private EditText edtTipoObjeto;
    private EditText edtDescripcion;
    private EditText edtCantidad;

    private Spinner spnCategoria;
    private Spinner spnUnidad;
    private Spinner spnCondicion;
    private Spinner spnEntrega;
    private Spinner spnFundacion;
    private TextView txtCoincidenciaFundacion;
    private final List<Necesidad> necesidadesActivas = new ArrayList<>();
    private final List<Fundacion> fundacionesDisponibles = new ArrayList<>();
    private long fundacionId = -1L;
    private long necesidadId = -1L;

    // DATOS DEL OBJETO
    private String tipo, descripcion, cantidad;
    private String categoria, unidad, condicion;
    private String entrega, fundacion;

    // PASO 2
    private ImageView[] imagenes = new ImageView[4];
    private Uri[] fotos = new Uri[4];

    // PASO 3
    private TextView txtCalidad;
    private TextView txtEstadoObjeto;
    private TextView txtResultado;
    private TextView txtMensajeResultado;
    private TextView txtEstadoAnalisis;

    private TextView txtDetalleCalidad;
    private TextView txtDetalleObjeto;

    private ProgressBar progresoAnalisis;
    private ProgressBar barraCalidad;
    private ProgressBar barraEstadoObjeto;

    private Button btnContinuar3;

    private boolean analisisAceptado = false;

    // RED NEURONAL (TFLite) + PUNTUACION
    private DonationScorer scorer;
    private ScoreResult ultimoResultado;
    private int analisisId = 0;   // para ignorar resultados viejos si el usuario retrocede

    private static final int REQ_GALERIA = 100;   // 100..103
    private static final int REQ_CAMARA = 200;    // 200..203
    private static final String[] NOMBRES_FOTO = {
            "Fotografía frontal", "Fotografía trasera", "Detalle / Etiqueta", "Fotografía extra"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_donacion);
        UiInsets.aplicar(this, R.id.donacionHeader, R.id.contenedorPasos, true, true);

        contenedorPasos = findViewById(R.id.contenedorPasos);
        txtNumeroPaso = findViewById(R.id.txtNumeroPaso);
        progreso = findViewById(R.id.progreso);

        findViewById(R.id.btnVolver).setOnClickListener(v -> finish());

        categoria = getIntent().getStringExtra("categoriaSugerida");
        fundacion = getIntent().getStringExtra("fundacionSugerida");
        fundacionId = getIntent().getLongExtra("fundacionIdSugerida", -1L);
        necesidadId = getIntent().getLongExtra("necesidadIdSugerida", -1L);

        scorer = new DonationScorer(this);

        mostrarPaso1();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        analisisId++;
        if (scorer != null) {
            scorer.close();
        }
    }

    // CAMBIAR PASO
    private void cambiarPaso(int numero) {

        pasoActual = numero;

        txtNumeroPaso.setText("Paso " + numero + " de 3");

        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams) progreso.getLayoutParams();

        params.width = (int) (getResources()
                .getDisplayMetrics().widthPixels * 0.86 * numero / 3.0);

        progreso.setLayoutParams(params);

        contenedorPasos.removeAllViews();
    }
    // PASO 1: INFORMACION DEL OBJETO
    private void mostrarPaso1() {

        cambiarPaso(1);

        View vista = LayoutInflater.from(this)
                .inflate(R.layout.frag_donacion_paso1,
                        contenedorPasos, false);

        contenedorPasos.addView(vista);

        edtTipoObjeto = vista.findViewById(R.id.edtTipoObjeto);
        edtDescripcion = vista.findViewById(R.id.edtDescripcion);
        edtCantidad = vista.findViewById(R.id.edtCantidad);

        spnCategoria = vista.findViewById(R.id.spnCategoria);
        spnUnidad = vista.findViewById(R.id.spnUnidad);
        spnCondicion = vista.findViewById(R.id.spnCondicion);
        spnEntrega = vista.findViewById(R.id.spnEntrega);
        spnFundacion = vista.findViewById(R.id.spnFundacion);
        txtCoincidenciaFundacion = vista.findViewById(R.id.txtCoincidenciaFundacion);

        configurarSpinner(spnCategoria, new String[]{
                "Seleccionar categoría",
                "Ropa y calzado",
                "Alimentos",
                "Muebles",
                "Electrodomésticos",
                "Material escolar",
                "Juguetes",
                "Higiene y limpieza",
                "Otros"
        });

        configurarSpinner(spnUnidad, new String[]{
                "Unidad",
                "Kilogramos",
                "Litros",
                "Paquetes",
                "Cajas",
                "Docenas"
        });

        configurarSpinner(spnCondicion, new String[]{
                "Seleccionar condición",
                "Nuevo",
                "Muy bien",
                "Bueno",
                "Usado"
        });

        configurarSpinner(spnEntrega, new String[]{
                "Seleccionar entrega",
                "Retiro",
                "Punto de encuentro"
        });

        configurarSpinner(spnFundacion, new String[]{
                "Seleccionar fundación",
                "Fundación Alalay",
                "Fundación Pallini",
                "World Vision Bolivia"
        });

        spnCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                actualizarFundacionesRecomendadas(String.valueOf(parent.getItemAtPosition(position)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        cargarNecesidadesParaCoincidencias();

        // RESTAURAR DATOS
        if (tipo != null) edtTipoObjeto.setText(tipo);
        if (descripcion != null) edtDescripcion.setText(descripcion);
        if (cantidad != null) edtCantidad.setText(cantidad);

        seleccionarSpinner(spnCategoria, categoria);
        seleccionarSpinner(spnUnidad, unidad);
        seleccionarSpinner(spnCondicion, condicion);
        seleccionarSpinner(spnEntrega, entrega);
        seleccionarSpinner(spnFundacion, fundacion);

        vista.findViewById(R.id.btnSiguiente1)
                .setOnClickListener(v -> {

                    if (!validarPaso1()) return;

                    guardarDatos();

                    mostrarPaso2();
                });
    }

    private void actualizarFundacionesRecomendadas(String categoriaElegida) {
        if (spnFundacion == null) return;

        String seleccionActual = fundacion;
        if (spnFundacion.getSelectedItem() != null
                && !String.valueOf(spnFundacion.getSelectedItem()).startsWith("Seleccionar")) {
            seleccionActual = String.valueOf(spnFundacion.getSelectedItem());
        }

        Set<String> nombres = new LinkedHashSet<>();
        int coincidencias = 0;
        for (Necesidad necesidad : NecesidadesRepositorio.ordenar(necesidadesActivas, categoriaElegida)) {
            if (necesidad.getCategoria().equalsIgnoreCase(categoriaElegida)) {
                nombres.add(necesidad.getFundacion());
                coincidencias++;
            }
        }
        nombres.add("Fundación Alalay");
        nombres.add("Fundación Pallini");
        nombres.add("World Vision Bolivia");
        for (Fundacion fundacionDisponible : fundacionesDisponibles) {
            nombres.add(fundacionDisponible.getNombre());
        }

        List<String> opciones = new ArrayList<>();
        opciones.add("Seleccionar fundación");
        opciones.addAll(nombres);
        configurarSpinner(spnFundacion, opciones.toArray(new String[0]));
        seleccionarSpinner(spnFundacion, seleccionActual);

        if (txtCoincidenciaFundacion != null) {
            txtCoincidenciaFundacion.setText(coincidencias > 0
                    ? coincidencias + (coincidencias == 1 ? " necesidad coincide" : " necesidades coinciden")
                        + " con esta categoría. Las fundaciones recomendadas aparecen primero."
                    : "No hay una necesidad exacta publicada; puedes elegir cualquier fundación.");
        }
    }

    private void cargarNecesidadesParaCoincidencias() {
        SupabaseClient.getInstance(this).getDbService()
                .getNecesidadesActivas("*,fundacion:fundaciones(nombre)", "eq.ACTIVA")
                .enqueue(new Callback<List<Necesidad>>() {
                    @Override
                    public void onResponse(Call<List<Necesidad>> call,
                                           Response<List<Necesidad>> response) {
                        if (!response.isSuccessful() || response.body() == null) return;
                        necesidadesActivas.clear();
                        necesidadesActivas.addAll(response.body());
                        if (pasoActual == 1 && spnCategoria != null
                                && spnCategoria.getSelectedItem() != null) {
                            actualizarFundacionesRecomendadas(
                                    String.valueOf(spnCategoria.getSelectedItem()));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Necesidad>> call, Throwable throwable) {
                        // El formulario sigue disponible aunque no haya conexión para recomendar.
                    }
                });

        SupabaseClient.getInstance(this).getDbService().getFundaciones()
                .enqueue(new Callback<List<Fundacion>>() {
                    @Override
                    public void onResponse(Call<List<Fundacion>> call,
                                           Response<List<Fundacion>> response) {
                        if (!response.isSuccessful() || response.body() == null) return;
                        fundacionesDisponibles.clear();
                        fundacionesDisponibles.addAll(response.body());
                        if (pasoActual == 1 && spnCategoria != null
                                && spnCategoria.getSelectedItem() != null) {
                            actualizarFundacionesRecomendadas(
                                    String.valueOf(spnCategoria.getSelectedItem()));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Fundacion>> call, Throwable throwable) {
                        // Se conserva el catálogo visible como respaldo.
                    }
                });
    }

    private void configurarSpinner(
            Spinner spinner, String[] opciones) {

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        opciones
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
    }

    private void seleccionarSpinner(
            Spinner spinner, String valor) {

        if (valor == null) return;

        ArrayAdapter adapter =
                (ArrayAdapter) spinner.getAdapter();

        int posicion = adapter.getPosition(valor);

        if (posicion >= 0) {
            spinner.setSelection(posicion);
        }
    }

    private boolean validarPaso1() {

        if (edtTipoObjeto.getText().toString().trim().isEmpty()
                || edtDescripcion.getText().toString().trim().isEmpty()
                || edtCantidad.getText().toString().trim().isEmpty()) {

            Toast.makeText(this,
                    "Completa todos los campos.",
                    Toast.LENGTH_SHORT).show();

            return false;
        }

        try {

            double valor = Double.parseDouble(
                    edtCantidad.getText().toString().trim());

            if (valor <= 0) {

                Toast.makeText(this,
                        "La cantidad debe ser mayor a cero.",
                        Toast.LENGTH_SHORT).show();

                return false;
            }

        } catch (NumberFormatException e) {

            Toast.makeText(this,
                    "Introduce una cantidad válida.",
                    Toast.LENGTH_SHORT).show();

            return false;
        }

        if (spnCategoria.getSelectedItemPosition() == 0
                || spnCondicion.getSelectedItemPosition() == 0
                || spnEntrega.getSelectedItemPosition() == 0
                || spnFundacion.getSelectedItemPosition() == 0) {

            Toast.makeText(this,
                    "Selecciona todas las opciones.",
                    Toast.LENGTH_SHORT).show();

            return false;
        }

        return true;
    }

    private void guardarDatos() {

        tipo = edtTipoObjeto.getText().toString().trim();
        descripcion = edtDescripcion.getText().toString().trim();
        cantidad = edtCantidad.getText().toString().trim();

        categoria = spnCategoria.getSelectedItem().toString();
        unidad = spnUnidad.getSelectedItem().toString();
        condicion = spnCondicion.getSelectedItem().toString();
        entrega = spnEntrega.getSelectedItem().toString();
        fundacion = spnFundacion.getSelectedItem().toString();
        long idEncontrado = buscarFundacionId(fundacion);
        if (idEncontrado > 0) {
            if (fundacionId != idEncontrado) necesidadId = -1L;
            fundacionId = idEncontrado;
        }
    }

    private long buscarFundacionId(String nombre) {
        for (Fundacion item : fundacionesDisponibles) {
            if (item.getId() != null && item.getNombre().equalsIgnoreCase(nombre)) {
                return item.getId();
            }
        }
        return -1L;
    }

    // PASO 2: FOTOGRAFIA
    private void mostrarPaso2() {

        cambiarPaso(2);

        View vista = LayoutInflater.from(this)
                .inflate(R.layout.frag_donacion_paso2,
                        contenedorPasos, false);

        contenedorPasos.addView(vista);

        int[] idsImagenes = {
                R.id.imgFrente,
                R.id.imgTrasera,
                R.id.imgDetalle,
                R.id.imgExtra
        };

        int[] idsBotones = {
                R.id.btnFrente,
                R.id.btnTrasera,
                R.id.btnDetalle,
                R.id.btnExtra
        };

        for (int i = 0; i < 4; i++) {

            final int indice = i;

            imagenes[i] = vista.findViewById(idsImagenes[i]);

            if (fotos[i] != null) {
                mostrarMiniatura(imagenes[i], fotos[i]);
            }

            vista.findViewById(idsBotones[i])
                    .setOnClickListener(v -> seleccionarFoto(indice));
        }

        vista.findViewById(R.id.btnAnterior2)
                .setOnClickListener(v -> mostrarPaso1());

        vista.findViewById(R.id.btnSiguiente2)
                .setOnClickListener(v -> {

                    if (fotos[0] == null
                            || fotos[1] == null
                            || fotos[2] == null) {

                        Toast.makeText(this,
                                "Selecciona las tres fotografías obligatorias.",
                                Toast.LENGTH_SHORT).show();

                        return;
                    }

                    mostrarPaso3();
                    analizarImagenes();
                });
    }

    // Pregunta si quiere usar la CAMARA (dentro de la app) o la GALERIA
    private void seleccionarFoto(int indice) {

        String[] opciones = {"Tomar foto con la cámara", "Elegir de la galería"};

        new AlertDialog.Builder(this)
                .setTitle(NOMBRES_FOTO[indice])
                .setItems(opciones, (dialogo, cual) -> {
                    if (cual == 0) {
                        abrirCamara(indice);
                    } else {
                        abrirGaleria(indice);
                    }
                })
                .show();
    }

    private void abrirCamara(int indice) {

        Intent intent = new Intent(this, ScannerActivity.class);
        intent.putExtra(ScannerActivity.EXTRA_TITULO, NOMBRES_FOTO[indice]);

        startActivityForResult(intent, REQ_CAMARA + indice);
    }

    private void abrirGaleria(int indice) {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");

        startActivityForResult(intent, REQ_GALERIA + indice);
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) return;

        // FOTO TOMADA CON LA CAMARA DE LA APP
        if (requestCode >= REQ_CAMARA && requestCode <= REQ_CAMARA + 3) {

            int indice = requestCode - REQ_CAMARA;
            String ruta = data.getStringExtra(ScannerActivity.EXTRA_RUTA_FOTO);

            if (ruta != null) {
                fotos[indice] = Uri.fromFile(new File(ruta));

                if (pasoActual == 2 && imagenes[indice] != null) {
                    mostrarMiniatura(imagenes[indice], fotos[indice]);
                }
            }
            return;
        }

        // FOTO ELEGIDA DE LA GALERIA
        if (requestCode >= REQ_GALERIA && requestCode <= REQ_GALERIA + 3) {

            int indice = requestCode - REQ_GALERIA;

            Uri uri = data.getData();

            if (uri != null) {

                try {

                    getContentResolver().takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );

                } catch (SecurityException ignored) {
                    // El permiso persistente puede no estar disponible.
                }

                fotos[indice] = uri;

                if (pasoActual == 2 && imagenes[indice] != null) {
                    mostrarMiniatura(imagenes[indice], uri);
                }
            }
        }
    }

    // Muestra la foto en el recuadro (quita el padding/tinte del icono de camara)
    private void mostrarMiniatura(ImageView imageView, Uri uri) {
        imageView.setPadding(0, 0, 0, 0);
        imageView.setImageTintList(null);
        imageView.setImageURI(uri);
    }

    // PASO 3: ANALISIS DE IMAGENES
    private void mostrarPaso3() {

        cambiarPaso(3);

        View vista = LayoutInflater.from(this)
                .inflate(R.layout.frag_donacion_paso3,
                        contenedorPasos, false);

        contenedorPasos.addView(vista);

        progresoAnalisis =
                vista.findViewById(R.id.progresoAnalisis);

        txtEstadoAnalisis =
                vista.findViewById(R.id.txtEstadoAnalisis);

        txtCalidad =
                vista.findViewById(R.id.txtCalidad);

        txtEstadoObjeto =
                vista.findViewById(R.id.txtEstadoObjeto);

        txtResultado =
                vista.findViewById(R.id.txtResultado);

        txtMensajeResultado =
                vista.findViewById(R.id.txtMensajeResultado);

        // NUEVAS PUNTUACIONES
        barraCalidad =
                vista.findViewById(R.id.barraCalidad);

        barraEstadoObjeto =
                vista.findViewById(R.id.barraEstadoObjeto);

        txtDetalleCalidad =
                vista.findViewById(R.id.txtDetalleCalidad);

        txtDetalleObjeto =
                vista.findViewById(R.id.txtDetalleObjeto);

        btnContinuar3 =
                vista.findViewById(R.id.btnContinuar3);

        btnContinuar3.setEnabled(false);

        vista.findViewById(R.id.btnRetornar)
                .setOnClickListener(v -> {
                    analisisId++;   // cancela el analisis en curso
                    mostrarPaso2();
                });

        btnContinuar3.setOnClickListener(v -> {

            if (!analisisAceptado) return;

            Toast.makeText(this,
                    "Análisis aprobado. Próximamente registraremos tu donación.",
                    Toast.LENGTH_LONG).show();

            // Aquí conectaremos la base de datos.
        });
    }

    // ANALISIS REAL CON LA RED NEURONAL
    private void analizarImagenes() {

        analisisAceptado = false;
        ultimoResultado = null;

        final int idActual = ++analisisId;

        btnContinuar3.setEnabled(false);
        progresoAnalisis.setProgress(15);
        txtEstadoAnalisis.setText("Analizando fotografías con la red neuronal...");

        final Uri[] copiaFotos = fotos.clone();
        final String categoriaDeclarada = categoria;
        final String condicionDeclarada = condicion;

        // 1) Cargar las fotos en un hilo aparte
        new Thread(() -> {

            final List<Bitmap> bitmaps = new ArrayList<>();

            try {
                for (Uri uri : copiaFotos) {
                    if (uri != null) {
                        bitmaps.add(ImagenUtil.cargarBitmap(DonacionActivity.this, uri, 960));
                    }
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (idActual != analisisId || isFinishing()) return;
                    mostrarErrorAnalisis(
                            "No se pudieron leer las fotografías. Vuelve a seleccionarlas.");
                });
                return;
            }

            // 2) Pasarlas por la red neuronal
            runOnUiThread(() -> {
                if (idActual != analisisId || isFinishing()) return;

                progresoAnalisis.setProgress(45);

                scorer.evaluate(bitmaps, categoriaDeclarada, condicionDeclarada, resultado -> {
                    if (idActual != analisisId || isFinishing()) return;
                    mostrarResultado(resultado);
                });
            });

        }).start();
    }

    private void mostrarErrorAnalisis(String mensaje) {

        analisisAceptado = false;
        progresoAnalisis.setProgress(0);
        txtEstadoAnalisis.setText("No se pudo completar el análisis.");
        txtResultado.setText("Error en el análisis");
        txtResultado.setTextColor(Color.parseColor("#C53030"));
        txtMensajeResultado.setText(mensaje);
        btnContinuar3.setEnabled(false);
    }

    private String formatoPuntaje(float valor) {
        return String.format(Locale.getDefault(), "%.1f", valor);
    }

    private void mostrarResultado(ScoreResult r) {

        ultimoResultado = r;

        progresoAnalisis.setProgress(100);

        txtEstadoAnalisis.setText(String.format(Locale.getDefault(),
                "Evaluación completada · %s · %d ms", r.motor, r.tiempoMs));

        barraCalidad.setMax(100);
        barraEstadoObjeto.setMax(100);

        barraCalidad.setProgress(Math.round(r.calidadFotos * 10f));
        barraEstadoObjeto.setProgress(Math.round(r.puntuacionObjeto * 10f));

        txtCalidad.setText(formatoPuntaje(r.calidadFotos) + "/10");
        txtEstadoObjeto.setText(formatoPuntaje(r.puntuacionObjeto) + "/10");

        txtDetalleCalidad.setText(r.detalleCalidad);
        txtDetalleObjeto.setText(r.detalleObjeto);

        String puntajeFinal = formatoPuntaje(r.puntuacionFinal) + "/10";

        if (r.nivel == 2) {

            txtResultado.setText("¡Aprobado para donación!");
            txtResultado.setTextColor(Color.parseColor("#0B8E83"));
            txtMensajeResultado.setText(
                    "Puntuación final: " + puntajeFinal
                            + ". Tu producto cumple con las condiciones requeridas.");

        } else if (r.nivel == 1) {

            txtResultado.setText("Aprobado con observaciones");
            txtResultado.setTextColor(Color.parseColor("#B45309"));
            txtMensajeResultado.setText(
                    "Puntuación final: " + puntajeFinal
                            + ". La fundación revisará tu donación antes de aceptarla.");

        } else {

            txtResultado.setText("No aprobado");
            txtResultado.setTextColor(Color.parseColor("#C53030"));
            txtMensajeResultado.setText(
                    "Puntuación final: " + puntajeFinal
                            + ". Mejora las fotografías (buena luz, objeto centrado) o revisa "
                            + "la categoría elegida y vuelve a intentarlo.");
        }

        analisisAceptado = r.nivel >= 1;
        btnContinuar3.setEnabled(analisisAceptado);

        btnContinuar3.setOnClickListener(v -> {
            if (!analisisAceptado) return;
            registrarDonacion();
        });
    }

    private void registrarDonacion() {

        // Crear fecha de registro
        String fecha = new SimpleDateFormat(
                "dd/MM/yyyy HH:mm",
                Locale.getDefault()
        ).format(new Date());

        // La puntuación de la IA se guarda junto a la descripción
        String descripcionFinal = descripcion;
        if (ultimoResultado != null) {
            descripcionFinal += "\n[Puntuación IA: "
                    + formatoPuntaje(ultimoResultado.puntuacionFinal) + "/10";
            if (!ultimoResultado.objetoDetectado.isEmpty()) {
                descripcionFinal += " · Detectado: " + ultimoResultado.objetoDetectado;
            }
            descripcionFinal += "]";
        }

        String userId = SessionManager
                .getInstance(DonacionActivity.this).getUserId();

        Donacion nuevaDonacionLocal = new Donacion(
                userId,
                tipo,
                categoria,
                descripcionFinal,
                cantidad,
                unidad,
                condicion,
                entrega,
                fundacion,
                fecha,
                EstadoDonacion.ENVIADA
        );

        if (userId == null || userId.isEmpty() || "demo_user".equals(userId)
                || fundacionId < 0) {
            DonacionesRepositorio.agregar(this, nuevaDonacionLocal);
            finalizarDonacion("Donación guardada en este teléfono. Conéctate para sincronizarla.");
            return;
        }

        double cantidadNumero = Double.parseDouble(cantidad);
        Double puntaje = ultimoResultado != null
                ? (double) ultimoResultado.puntuacionFinal : null;
        DonacionSupabase nuevaDonacion = new DonacionSupabase(
                userId, fundacionId, necesidadId > 0 ? necesidadId : null,
                tipo, categoria, descripcionFinal, cantidadNumero, unidad,
                condicion, entrega, puntaje, EstadoDonacion.ENVIADA);

        btnContinuar3.setEnabled(false);
        btnContinuar3.setText("Enviando donación...");
        SupabaseClient.getInstance(this).getDbService()
                .insertDonacion("return=representation", nuevaDonacion)
                .enqueue(new Callback<List<DonacionSupabase>>() {
                    @Override
                    public void onResponse(Call<List<DonacionSupabase>> call,
                                           Response<List<DonacionSupabase>> response) {
                        if (response.isSuccessful()) {
                            finalizarDonacion("Donación enviada correctamente a " + fundacion + ".");
                        } else {
                            DonacionesRepositorio.agregar(DonacionActivity.this, nuevaDonacionLocal);
                            finalizarDonacion("Supabase no respondió. La donación quedó guardada localmente.");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<DonacionSupabase>> call, Throwable throwable) {
                        DonacionesRepositorio.agregar(DonacionActivity.this, nuevaDonacionLocal);
                        finalizarDonacion("Sin conexión. La donación quedó guardada localmente.");
                    }
                });
    }

    private void finalizarDonacion(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, HistorialActivity.class));
        finish();
    }
}
