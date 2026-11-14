package com.prdev.solidaryhands;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_donacion);

        contenedorPasos = findViewById(R.id.contenedorPasos);
        txtNumeroPaso = findViewById(R.id.txtNumeroPaso);
        progreso = findViewById(R.id.progreso);

        findViewById(R.id.btnVolver).setOnClickListener(v -> finish());

        mostrarPaso1();
    }

    // CAMBIAR PASO
    private void cambiarPaso(int numero) {

        pasoActual = numero;

        txtNumeroPaso.setText(numero + " de 3");

        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams) progreso.getLayoutParams();

        params.width = (int) (getResources()
                .getDisplayMetrics().widthPixels * numero / 3.0);

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
                imagenes[i].setImageURI(fotos[i]);
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

    private void seleccionarFoto(int indice) {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");

        startActivityForResult(intent, indice + 100);
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK
                && data != null
                && requestCode >= 100
                && requestCode <= 103) {

            int indice = requestCode - 100;

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
                    imagenes[indice].setImageURI(uri);
                }
            }
        }
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
                .setOnClickListener(v -> mostrarPaso2());

        btnContinuar3.setOnClickListener(v -> {

            if (!analisisAceptado) return;

            Toast.makeText(this,
                    "Análisis aprobado. Próximamente registraremos tu donación.",
                    Toast.LENGTH_LONG).show();

            // Aquí conectaremos la base de datos.
        });
    }

    // ANALISIS PROVISIONAL
    private void analizarImagenes() {

        analisisAceptado = true;

        txtEstadoAnalisis.setText(
                "Evaluación de inteligencia artificial completada."
        );

        progresoAnalisis.setProgress(100);

        barraCalidad.setMax(10);
        barraEstadoObjeto.setMax(10);

        barraCalidad.setProgress(9);
        barraEstadoObjeto.setProgress(8);

        txtCalidad.setText("9/10");
        txtEstadoObjeto.setText("8/10");

        txtDetalleCalidad.setText(
                "Excelente nitidez e iluminación en las fotografías."
        );

        txtDetalleObjeto.setText(
                "Objeto en muy buen estado general y apto para donación."
        );

        txtResultado.setText("¡Aprobado para donación!");

        txtMensajeResultado.setText(
                "Tu producto cumple con las condiciones requeridas."
        );

        btnContinuar3.setEnabled(true);

        btnContinuar3.setOnClickListener(v -> {

            if (!analisisAceptado) return;

            // Crear fecha de registro
            String fecha = new java.text.SimpleDateFormat(
                    "dd/MM/yyyy HH:mm",
                    java.util.Locale.getDefault()
            ).format(new java.util.Date());

            // Registrar donación
            Donacion nuevaDonacion = new Donacion(
                    tipo,
                    categoria,
                    descripcion,
                    cantidad,
                    unidad,
                    condicion,
                    entrega,
                    fundacion,
                    fecha,
                    "Pendiente de revisión"
            );

            DonacionesRepositorio.agregar(nuevaDonacion);

            Toast.makeText(this,
                    "Donación registrada correctamente.",
                    Toast.LENGTH_LONG).show();

            // Abrir historial
            Intent intent = new Intent(
                    DonacionActivity.this,
                    HistorialActivity.class
            );

            startActivity(intent);
            finish();
        });

    }
}
