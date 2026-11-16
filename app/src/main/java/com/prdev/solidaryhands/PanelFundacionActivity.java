package com.prdev.solidaryhands;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.prdev.solidaryhands.models.Donacion;
import com.prdev.solidaryhands.models.DonacionSupabase;
import com.prdev.solidaryhands.models.EstadoDonacion;
import com.prdev.solidaryhands.models.Fundacion;
import com.prdev.solidaryhands.models.Necesidad;
import com.prdev.solidaryhands.network.SessionManager;
import com.prdev.solidaryhands.network.SupabaseClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PanelFundacionActivity extends AppCompatActivity {
    private SessionManager session;
    private LinearLayout contenedorNecesidades;
    private LinearLayout contenedorSolicitudes;
    private TextView resumen;
    private TextView nombreFundacion;
    private Button btnNuevaNecesidad;
    private long fundacionId = -1L;
    private int necesidadesPublicadas;
    private int solicitudesPendientes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel_fundacion);
        UiInsets.aplicar(this, R.id.panelFundacionHeader, R.id.panelFundacionScroll, false, true);

        session = SessionManager.getInstance(this);
        contenedorNecesidades = findViewById(R.id.contenedorNecesidadesFundacion);
        contenedorSolicitudes = findViewById(R.id.contenedorSolicitudesFundacion);
        resumen = findViewById(R.id.txtResumenPanelFundacion);
        nombreFundacion = findViewById(R.id.txtNombrePanelFundacion);
        btnNuevaNecesidad = findViewById(R.id.btnNuevaNecesidad);
        nombreFundacion.setText(session.getFoundationName());

        btnNuevaNecesidad.setEnabled(false);
        btnNuevaNecesidad.setOnClickListener(v -> mostrarDialogoNecesidad());
        findViewById(R.id.btnPerfilPanelFundacion).setOnClickListener(v ->
                startActivity(new Intent(this, PerfilActivity.class)));
        findViewById(R.id.btnCerrarSesionFundacion).setOnClickListener(v -> cerrarSesion());
    }

    @Override
    protected void onResume() {
        super.onResume();
        resolverFundacion();
    }

    private void resolverFundacion() {
        resumen.setText("Sincronizando el panel con Supabase...");
        contenedorNecesidades.removeAllViews();
        contenedorNecesidades.addView(texto("Cargando tus necesidades...", 14, "#586A6E", false));
        btnNuevaNecesidad.setEnabled(false);

        String usuarioId = session.getUserId();
        if (usuarioId == null || usuarioId.isEmpty() || "demo_user".equals(usuarioId)) {
            mostrarErrorFundacion("Esta función necesita una sesión real de Supabase.");
            return;
        }

        SupabaseClient.getInstance(this).getDbService()
                .getFundacionPorUsuario("eq." + usuarioId)
                .enqueue(new Callback<List<Fundacion>>() {
                    @Override
                    public void onResponse(Call<List<Fundacion>> call,
                                           Response<List<Fundacion>> response) {
                        if (!response.isSuccessful() || response.body() == null
                                || response.body().isEmpty()) {
                            mostrarErrorFundacion("No encontramos la fundación asociada a esta cuenta. Ejecuta el esquema SQL y vuelve a iniciar sesión.");
                            return;
                        }
                        Fundacion fundacion = response.body().get(0);
                        if (fundacion.getId() == null) {
                            mostrarErrorFundacion("La fundación no tiene un identificador válido.");
                            return;
                        }
                        fundacionId = fundacion.getId();
                        session.updateFoundationId(fundacionId);
                        session.updateFoundationName(fundacion.getNombre());
                        nombreFundacion.setText(fundacion.getNombre());
                        btnNuevaNecesidad.setEnabled(true);
                        cargarNecesidadesPropias();
                        cargarSolicitudesFundacion();
                    }

                    @Override
                    public void onFailure(Call<List<Fundacion>> call, Throwable throwable) {
                        mostrarErrorFundacion("No se pudo conectar con Supabase. Revisa tu conexión.");
                    }
                });
    }

    private void cargarNecesidadesPropias() {
        SupabaseClient.getInstance(this).getDbService()
                .getNecesidadesDeFundacion("*,fundacion:fundaciones(nombre)",
                        "eq." + fundacionId, "fecha_publicacion.desc")
                .enqueue(new Callback<List<Necesidad>>() {
                    @Override
                    public void onResponse(Call<List<Necesidad>> call,
                                           Response<List<Necesidad>> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            contenedorNecesidades.removeAllViews();
                            contenedorNecesidades.addView(texto(
                                    "No pudimos cargar tus necesidades.", 14, "#C53030", false));
                            return;
                        }
                        List<Necesidad> necesidades = response.body();
                        necesidadesPublicadas = necesidades.size();
                        actualizarResumen();
                        mostrarNecesidades(necesidades);
                    }

                    @Override
                    public void onFailure(Call<List<Necesidad>> call, Throwable throwable) {
                        contenedorNecesidades.removeAllViews();
                        contenedorNecesidades.addView(texto(
                                "Sin conexión. No pudimos actualizar tus necesidades.",
                                14, "#C53030", false));
                    }
                });
    }

    private void mostrarErrorFundacion(String mensaje) {
        resumen.setText(mensaje);
        contenedorNecesidades.removeAllViews();
        contenedorNecesidades.addView(texto(mensaje, 14, "#C53030", false));
        btnNuevaNecesidad.setEnabled(false);
        contenedorSolicitudes.removeAllViews();
        contenedorSolicitudes.addView(texto(
                "No se pudieron consultar las donaciones.", 14, "#C53030", false));
    }

    private void actualizarResumen() {
        resumen.setText(necesidadesPublicadas + " necesidades publicadas  ·  "
                + solicitudesPendientes + " solicitudes pendientes");
    }

    private void mostrarNecesidades(List<Necesidad> necesidades) {
        contenedorNecesidades.removeAllViews();
        if (necesidades.isEmpty()) {
            contenedorNecesidades.addView(texto(
                    "Todavía no publicaste necesidades. Pulsa el botón superior para crear la primera.",
                    14, "#586A6E", false));
            return;
        }
        for (Necesidad necesidad : necesidades) {
            LinearLayout card = tarjeta();
            card.addView(texto(necesidad.getCategoria() + " · URGENCIA "
                    + necesidad.getUrgencia(), 16, "#102B32", true));
            card.addView(texto(necesidad.getDescripcion(), 14, "#586A6E", false));
            card.addView(texto("Cantidad: " + necesidad.getCantidad(), 13, "#0B8E83", true));
            card.addView(texto("Estado: " + necesidad.getEstado(), 12,
                    necesidad.isActiva() ? "#0B8E83" : "#586A6E", true));

            Button estado = boton(necesidad.isActiva()
                    ? "Marcar como cubierta" : "Volver a activar", "#0B8E83");
            estado.setOnClickListener(v -> cambiarEstadoNecesidad(necesidad, estado));
            card.addView(estado);
            contenedorNecesidades.addView(card);
        }
    }

    private void cambiarEstadoNecesidad(Necesidad necesidad, Button botonEstado) {
        if (necesidad.getId() == null) return;
        String estadoNuevo = necesidad.isActiva() ? "CUBIERTA" : "ACTIVA";
        botonEstado.setEnabled(false);
        Map<String, String> cambios = new HashMap<>();
        cambios.put("estado", estadoNuevo);
        SupabaseClient.getInstance(this).getDbService()
                .updateEstadoNecesidad("eq." + necesidad.getId(), cambios)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(PanelFundacionActivity.this,
                                    "Necesidad actualizada a " + estadoNuevo + ".",
                                    Toast.LENGTH_SHORT).show();
                            cargarNecesidadesPropias();
                        } else {
                            botonEstado.setEnabled(true);
                            Toast.makeText(PanelFundacionActivity.this,
                                    "No se pudo actualizar la necesidad.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable throwable) {
                        botonEstado.setEnabled(true);
                        Toast.makeText(PanelFundacionActivity.this,
                                "Sin conexión. Intenta nuevamente.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void cargarSolicitudesFundacion() {
        contenedorSolicitudes.removeAllViews();
        contenedorSolicitudes.addView(texto("Cargando donaciones recibidas...",
                14, "#586A6E", false));
        SupabaseClient.getInstance(this).getDbService()
                .getDonacionesFundacion("*,fundacion:fundaciones(nombre)",
                        "eq." + fundacionId, "fecha_registro.desc")
                .enqueue(new Callback<List<DonacionSupabase>>() {
                    @Override
                    public void onResponse(Call<List<DonacionSupabase>> call,
                                           Response<List<DonacionSupabase>> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            contenedorSolicitudes.removeAllViews();
                            contenedorSolicitudes.addView(texto(
                                    "No pudimos cargar las donaciones recibidas.",
                                    14, "#C53030", false));
                            return;
                        }
                        List<Donacion> donaciones = new ArrayList<>();
                        solicitudesPendientes = 0;
                        for (DonacionSupabase item : response.body()) {
                            Donacion donacion = item.toDonacion();
                            donaciones.add(donacion);
                            if (EstadoDonacion.ENVIADA.equals(donacion.getEstado())
                                    || EstadoDonacion.EN_REVISION.equals(donacion.getEstado())) {
                                solicitudesPendientes++;
                            }
                        }
                        actualizarResumen();
                        mostrarSolicitudes(donaciones);
                    }

                    @Override
                    public void onFailure(Call<List<DonacionSupabase>> call, Throwable throwable) {
                        contenedorSolicitudes.removeAllViews();
                        contenedorSolicitudes.addView(texto(
                                "Sin conexión. No pudimos actualizar las donaciones.",
                                14, "#C53030", false));
                    }
                });
    }

    private void mostrarSolicitudes(List<Donacion> solicitudes) {
        contenedorSolicitudes.removeAllViews();
        if (solicitudes.isEmpty()) {
            contenedorSolicitudes.addView(texto(
                    "No hay solicitudes de donación para esta fundación todavía.",
                    14, "#586A6E", false));
            return;
        }
        for (Donacion donacion : solicitudes) {
            LinearLayout card = tarjeta();
            card.addView(texto(donacion.getTipo() + " · " + donacion.getCantidad() + " "
                    + donacion.getUnidad(), 16, "#102B32", true));
            card.addView(texto(donacion.getCategoria(), 13, "#0B8E83", true));
            card.addView(texto("Estado: " + donacion.getEstado(), 13,
                    colorEstado(donacion.getEstado()), true));

            if (!EstadoDonacion.ACEPTADA.equals(donacion.getEstado())
                    && !EstadoDonacion.RECHAZADA.equals(donacion.getEstado())) {
                LinearLayout acciones = new LinearLayout(this);
                acciones.setOrientation(LinearLayout.HORIZONTAL);
                Button revisar = boton("Revisar", "#0B8E83");
                Button aceptar = boton("Aceptar", "#075D5B");
                Button rechazar = boton("Rechazar", "#F45A3A");
                agregarAccion(acciones, revisar);
                agregarAccion(acciones, aceptar);
                agregarAccion(acciones, rechazar);
                revisar.setOnClickListener(v -> cambiarEstadoDonacion(donacion, EstadoDonacion.EN_REVISION));
                aceptar.setOnClickListener(v -> cambiarEstadoDonacion(donacion, EstadoDonacion.ACEPTADA));
                rechazar.setOnClickListener(v -> cambiarEstadoDonacion(donacion, EstadoDonacion.RECHAZADA));
                card.addView(acciones);
            }
            contenedorSolicitudes.addView(card);
        }
    }

    private void mostrarDialogoNecesidad() {
        if (fundacionId < 0) {
            Toast.makeText(this, "Espera mientras cargamos tu fundación.", Toast.LENGTH_SHORT).show();
            return;
        }

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), dp(6), dp(20), 0);

        Spinner categoria = spinner(new String[]{"Ropa y calzado", "Alimentos", "Muebles",
                "Electrodomésticos", "Material escolar", "Juguetes",
                "Higiene y limpieza", "Otros"});
        Spinner urgencia = spinner(new String[]{"ALTA", "MEDIA", "BAJA"});
        EditText cantidad = campo("Cantidad requerida (ej. 30)");
        cantidad.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        Spinner unidad = spinner(new String[]{"unidades", "kilogramos", "litros", "paquetes",
                "cajas", "docenas", "prendas", "kits"});
        EditText descripcion = campo("Describe claramente qué necesitan");
        descripcion.setMinLines(3);
        descripcion.setGravity(Gravity.TOP);
        descripcion.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        form.addView(categoria);
        form.addView(urgencia);
        form.addView(cantidad);
        form.addView(unidad);
        form.addView(descripcion);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Publicar necesidad")
                .setMessage("El donante la verá inmediatamente en Necesidades activas.")
                .setView(form)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Publicar", null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String cantidadValor = cantidad.getText().toString().trim();
                    String descripcionValor = descripcion.getText().toString().trim();
                    if (cantidadValor.isEmpty() || descripcionValor.isEmpty()) {
                        Toast.makeText(this, "Completa cantidad y descripción.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double cantidadNumero;
                    try {
                        cantidadNumero = Double.parseDouble(cantidadValor);
                    } catch (NumberFormatException exception) {
                        cantidad.setError("Ingresa una cantidad válida");
                        return;
                    }
                    if (cantidadNumero <= 0) {
                        cantidad.setError("La cantidad debe ser mayor a cero");
                        return;
                    }

                    Button publicar = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    publicar.setEnabled(false);
                    publicar.setText("Publicando...");
                    Necesidad necesidad = new Necesidad(fundacionId,
                            String.valueOf(categoria.getSelectedItem()), descripcionValor,
                            cantidadNumero, String.valueOf(unidad.getSelectedItem()),
                            String.valueOf(urgencia.getSelectedItem()));

                    SupabaseClient.getInstance(this).getDbService()
                            .insertNecesidad("return=representation", necesidad)
                            .enqueue(new Callback<List<Necesidad>>() {
                                @Override
                                public void onResponse(Call<List<Necesidad>> call,
                                                       Response<List<Necesidad>> response) {
                                    if (response.isSuccessful()) {
                                        dialog.dismiss();
                                        Toast.makeText(PanelFundacionActivity.this,
                                                "Necesidad publicada. Ya está visible para los donantes.",
                                                Toast.LENGTH_LONG).show();
                                        cargarNecesidadesPropias();
                                    } else {
                                        publicar.setEnabled(true);
                                        publicar.setText("Publicar");
                                        Toast.makeText(PanelFundacionActivity.this,
                                                "Supabase rechazó la publicación. Revisa las políticas del esquema.",
                                                Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<List<Necesidad>> call, Throwable throwable) {
                                    publicar.setEnabled(true);
                                    publicar.setText("Publicar");
                                    Toast.makeText(PanelFundacionActivity.this,
                                            "Sin conexión. La necesidad no fue publicada.",
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                }));
        dialog.show();
    }

    private void cambiarEstadoDonacion(Donacion donacion, String estado) {
        if (donacion.getId() == null) return;
        Map<String, String> cambios = new HashMap<>();
        cambios.put("estado", estado);
        SupabaseClient.getInstance(this).getDbService()
                .updateEstadoDonacion("eq." + donacion.getId(), cambios)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(PanelFundacionActivity.this,
                                    "Donación actualizada a " + estado + ".",
                                    Toast.LENGTH_SHORT).show();
                            cargarSolicitudesFundacion();
                        } else {
                            Toast.makeText(PanelFundacionActivity.this,
                                    "No se pudo cambiar el estado.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable throwable) {
                        Toast.makeText(PanelFundacionActivity.this,
                                "Sin conexión. Intenta nuevamente.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void cerrarSesion() {
        session.clearSession();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private LinearLayout tarjeta() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(15), dp(15), dp(15), dp(15));
        card.setBackgroundResource(R.drawable.bg_card);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(12);
        card.setLayoutParams(params);
        card.setElevation(dp(2));
        return card;
    }

    private TextView texto(String valor, int size, String color, boolean bold) {
        TextView text = new TextView(this);
        text.setText(valor);
        text.setTextSize(size);
        text.setTextColor(Color.parseColor(color));
        if (bold) text.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(5);
        text.setLayoutParams(params);
        return text;
    }

    private Button boton(String valor, String color) {
        Button button = new Button(this);
        button.setText(valor);
        button.setTextSize(12);
        button.setTextColor(Color.WHITE);
        button.setAllCaps(false);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(color)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(46));
        params.topMargin = dp(10);
        button.setLayoutParams(params);
        return button;
    }

    private void agregarAccion(LinearLayout acciones, Button button) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(46), 1);
        params.topMargin = dp(10);
        params.setMarginEnd(dp(5));
        button.setLayoutParams(params);
        acciones.addView(button);
    }

    private Spinner spinner(String[] opciones) {
        Spinner spinner = new Spinner(this);
        spinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, opciones));
        spinner.setBackgroundResource(R.drawable.bg_input);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(52));
        params.bottomMargin = dp(10);
        spinner.setLayoutParams(params);
        spinner.setPadding(dp(10), 0, dp(10), 0);
        return spinner;
    }

    private EditText campo(String hint) {
        EditText editText = new EditText(this);
        editText.setHint(hint);
        editText.setTextColor(Color.parseColor("#102B32"));
        editText.setHintTextColor(Color.parseColor("#78909C"));
        editText.setBackgroundResource(R.drawable.bg_input);
        editText.setPadding(dp(12), dp(10), dp(12), dp(10));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(10);
        editText.setLayoutParams(params);
        return editText;
    }

    private String colorEstado(String estado) {
        if (EstadoDonacion.ACEPTADA.equals(estado)) return "#0B8E83";
        if (EstadoDonacion.RECHAZADA.equals(estado)) return "#C53030";
        return "#9A6700";
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
