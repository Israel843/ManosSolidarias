package com.prdev.solidaryhands;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.prdev.solidaryhands.models.Donacion;
import com.prdev.solidaryhands.models.Necesidad;
import com.prdev.solidaryhands.network.SupabaseClient;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NecesidadesActivity extends AppCompatActivity {
    private LinearLayout contenedor;
    private LinearLayout contenedorEstado;
    private TextView resumen;
    private TextView txtEstado;
    private ProgressBar progreso;
    private String categoriaReferencia = "";
    private boolean cargando;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_necesidades);
        UiInsets.aplicar(this, R.id.necesidadesHeader, R.id.necesidadesScroll, true, true);

        contenedor = findViewById(R.id.contenedorNecesidadesActivas);
        contenedorEstado = findViewById(R.id.contenedorEstadoNecesidades);
        resumen = findViewById(R.id.txtResumenCoincidencias);
        txtEstado = findViewById(R.id.txtEstadoNecesidades);
        progreso = findViewById(R.id.progresoNecesidades);
        findViewById(R.id.btnVolverNecesidades).setOnClickListener(v -> finish());
        findViewById(R.id.btnActualizarNecesidades).setOnClickListener(v -> cargarNecesidades());
        findViewById(R.id.btnReintentarNecesidades).setOnClickListener(v -> cargarNecesidades());

        List<Donacion> donaciones = DonacionesRepositorio.obtenerTodas(this);
        if (!donaciones.isEmpty()) categoriaReferencia = donaciones.get(0).getCategoria();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarNecesidades();
    }

    private void cargarNecesidades() {
        if (cargando) return;
        cargando = true;
        progreso.setVisibility(View.VISIBLE);
        contenedorEstado.setVisibility(View.GONE);
        contenedor.removeAllViews();
        resumen.setText("Buscando necesidades activas cerca de tu intención de ayuda...");

        SupabaseClient.getInstance(this).getDbService()
                .getNecesidadesActivas("*,fundacion:fundaciones(nombre)", "eq.ACTIVA")
                .enqueue(new Callback<List<Necesidad>>() {
                    @Override
                    public void onResponse(Call<List<Necesidad>> call,
                                           Response<List<Necesidad>> response) {
                        cargando = false;
                        progreso.setVisibility(View.GONE);
                        if (!response.isSuccessful() || response.body() == null) {
                            mostrarError("No pudimos consultar las necesidades. Verifica que el esquema de Supabase esté instalado.");
                            return;
                        }
                        mostrarNecesidades(NecesidadesRepositorio.ordenar(
                                response.body(), categoriaReferencia));
                    }

                    @Override
                    public void onFailure(Call<List<Necesidad>> call, Throwable throwable) {
                        cargando = false;
                        progreso.setVisibility(View.GONE);
                        mostrarError("No hay conexión con Supabase. Revisa tu internet e inténtalo nuevamente.");
                    }
                });
    }

    private void mostrarNecesidades(List<Necesidad> necesidades) {
        if (necesidades == null) necesidades = Collections.emptyList();
        contenedor.removeAllViews();

        resumen.setText(categoriaReferencia.isEmpty()
                ? necesidades.size() + " necesidades activas · Las más urgentes aparecen primero."
                : "Coincidencias para “" + categoriaReferencia
                    + "” · Luego mostramos otras formas de ayudar.");

        if (necesidades.isEmpty()) {
            txtEstado.setText("Todavía no hay necesidades activas. Cuando una fundación publique una, aparecerá aquí automáticamente.");
            contenedorEstado.setVisibility(View.VISIBLE);
            return;
        }

        contenedorEstado.setVisibility(View.GONE);
        for (Necesidad necesidad : necesidades) {
            contenedor.addView(crearTarjeta(necesidad));
        }
    }

    private void mostrarError(String mensaje) {
        resumen.setText("No se pudieron actualizar las necesidades.");
        txtEstado.setText(mensaje);
        contenedorEstado.setVisibility(View.VISIBLE);
    }

    private LinearLayout crearTarjeta(Necesidad necesidad) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackgroundResource(R.drawable.bg_card);
        card.setElevation(dp(2));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(14);
        card.setLayoutParams(params);

        boolean coincide = NecesidadesRepositorio.coincide(necesidad, categoriaReferencia);
        String etiqueta = coincide ? "COINCIDE CONTIGO" : "NECESIDAD ACTIVA";
        card.addView(texto(etiqueta + " · URGENCIA " + necesidad.getUrgencia(),
                11, coincide ? "#0B8E83" : colorUrgencia(necesidad.getUrgencia()), true));
        card.addView(texto(necesidad.getFundacion(), 18, "#102B32", true));
        card.addView(texto(necesidad.getCategoria() + " · " + necesidad.getCantidad(),
                14, "#0B8E83", true));
        card.addView(texto(necesidad.getDescripcion(), 14, "#586A6E", false));

        Button donar = new Button(this);
        donar.setText("Quiero ayudar con esta necesidad");
        donar.setTextColor(Color.WHITE);
        donar.setTextSize(14);
        donar.setAllCaps(false);
        donar.setTypeface(null, Typeface.BOLD);
        donar.setBackgroundResource(R.drawable.bg_button);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(-1, dp(50));
        buttonParams.topMargin = dp(12);
        donar.setLayoutParams(buttonParams);
        donar.setOnClickListener(v -> {
            Intent intent = new Intent(this, DonacionActivity.class);
            intent.putExtra("categoriaSugerida", necesidad.getCategoria());
            intent.putExtra("fundacionSugerida", necesidad.getFundacion());
            if (necesidad.getFundacionId() != null) {
                intent.putExtra("fundacionIdSugerida", necesidad.getFundacionId());
            }
            if (necesidad.getId() != null) {
                intent.putExtra("necesidadIdSugerida", necesidad.getId());
            }
            startActivity(intent);
        });
        card.addView(donar);
        return card;
    }

    private String colorUrgencia(String urgencia) {
        if ("ALTA".equalsIgnoreCase(urgencia)) return "#F45A3A";
        if ("MEDIA".equalsIgnoreCase(urgencia)) return "#B45309";
        return "#586A6E";
    }

    private TextView texto(String valor, int size, String color, boolean bold) {
        TextView text = new TextView(this);
        text.setText(valor);
        text.setTextSize(size);
        text.setTextColor(Color.parseColor(color));
        text.setGravity(Gravity.START);
        if (bold) text.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(6);
        text.setLayoutParams(params);
        return text;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
