package com.prdev.solidaryhands;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.prdev.solidaryhands.models.Donacion;
import com.prdev.solidaryhands.models.DonacionSupabase;
import com.prdev.solidaryhands.models.EstadoDonacion;
import com.prdev.solidaryhands.network.SessionManager;
import com.prdev.solidaryhands.network.SupabaseClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistorialActivity extends AppCompatActivity {

    private LinearLayout contenedorDonaciones;
    private LinearLayout contenedorVacio;
    private TextView txtTotalDonaciones;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);
        UiInsets.aplicar(this, R.id.historialScreen, R.id.historialScreen, true, true);

        sessionManager = SessionManager.getInstance(this);

        contenedorDonaciones = findViewById(R.id.contenedorDonaciones);
        contenedorVacio = findViewById(R.id.contenedorVacio);
        txtTotalDonaciones = findViewById(R.id.txtTotalDonaciones);

        findViewById(R.id.btnVolverHistorial).setOnClickListener(v -> finish());

        cargarDonaciones();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (contenedorDonaciones != null) {
            cargarDonaciones();
        }
    }

    private void cargarDonaciones() {
        String userId = sessionManager.getUserId();

        // Si no hay usuario logueado o estamos en modo local de demostración
        if (userId == null || userId.isEmpty() || com.prdev.solidaryhands.network.SupabaseConfig.SUPABASE_URL.contains("TU_PROYECTO")) {
            mostrarLista(DonacionesRepositorio.obtenerTodas(this));
            return;
        }

        // Consultar desde Supabase
        SupabaseClient.getInstance(this).getDbService()
                .getDonacionesDonante("*,fundacion:fundaciones(nombre)",
                        "eq." + userId, "fecha_registro.desc")
                .enqueue(new Callback<List<DonacionSupabase>>() {
                    @Override
                    public void onResponse(Call<List<DonacionSupabase>> call,
                                           Response<List<DonacionSupabase>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Donacion> listaSupabase = new ArrayList<>();
                            for (DonacionSupabase item : response.body()) {
                                listaSupabase.add(item.toDonacion());
                            }
                            // También sumamos donaciones locales si las hubiera
                            List<Donacion> listaFinal = new ArrayList<>(listaSupabase);
                            for (Donacion d : DonacionesRepositorio.obtenerTodas(HistorialActivity.this)) {
                                if (!contieneDonacion(listaFinal, d)) {
                                    listaFinal.add(0, d);
                                }
                            }
                            mostrarLista(listaFinal);
                        } else {
                            mostrarLista(DonacionesRepositorio.obtenerTodas(HistorialActivity.this));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<DonacionSupabase>> call, Throwable t) {
                        // En caso de fallo de red, mostrar datos locales
                        mostrarLista(DonacionesRepositorio.obtenerTodas(HistorialActivity.this));
                    }
                });
    }

    private boolean contieneDonacion(List<Donacion> lista, Donacion objetivo) {
        for (Donacion d : lista) {
            if (d.getFecha().equals(objetivo.getFecha())
                    && d.getTipo().equals(objetivo.getTipo())
                    && d.getFundacion().equals(objetivo.getFundacion())) {
                return true;
            }
        }
        return false;
    }

    private void mostrarLista(List<Donacion> lista) {
        contenedorDonaciones.removeAllViews();

        int total = lista != null ? lista.size() : 0;
        txtTotalDonaciones.setText(total + (total == 1 ? " donación" : " donaciones"));

        if (lista == null || lista.isEmpty()) {
            contenedorVacio.setVisibility(View.VISIBLE);
            return;
        }

        contenedorVacio.setVisibility(View.GONE);

        for (int i = 0; i < lista.size(); i++) {
            Donacion donacion = lista.get(i);
            crearTarjeta(donacion, i + 1);
        }
    }

    private void crearTarjeta(Donacion d, int numero) {
        LinearLayout tarjeta = new LinearLayout(this);
        tarjeta.setOrientation(LinearLayout.VERTICAL);
        tarjeta.setPadding(dp(16), dp(16), dp(16), dp(16));
        tarjeta.setBackgroundResource(R.drawable.bg_card);

        LinearLayout.LayoutParams parametros = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        parametros.setMargins(0, 0, 0, dp(15));
        tarjeta.setLayoutParams(parametros);
        tarjeta.setElevation(dp(2));

        // NÚMERO Y ESTADO
        LinearLayout encabezado = new LinearLayout(this);
        encabezado.setOrientation(LinearLayout.HORIZONTAL);
        encabezado.setGravity(Gravity.CENTER_VERTICAL);

        TextView titulo = crearTexto(
                "Donación #" + String.format("%03d", numero),
                17,
                "#0B8E83",
                true
        );
        encabezado.addView(titulo, new LinearLayout.LayoutParams(0, -2, 1));

        TextView estado = crearTexto(
                d.getEstado(),
                12,
                colorEstado(d.getEstado()),
                true
        );
        encabezado.addView(estado);
        tarjeta.addView(encabezado);

        // INFORMACIÓN DEL OBJETO
        agregarTexto(tarjeta, "Objeto: " + d.getTipo(), 16, "#102B32", true);
        agregarTexto(tarjeta, "Categoría: " + d.getCategoria(), 14, "#586A6E", false);
        agregarTexto(tarjeta, "Descripción: " + d.getDescripcion(), 14, "#586A6E", false);
        agregarTexto(tarjeta, "Cantidad: " + d.getCantidad() + " " + d.getUnidad(), 14, "#586A6E", false);
        agregarTexto(tarjeta, "Condición: " + d.getCondicion(), 14, "#586A6E", false);

        // FUNDACIÓN DESTINATARIA
        agregarTexto(tarjeta, "Fundación destinataria", 15, "#102B32", true);
        agregarTexto(tarjeta, d.getFundacion(), 17, "#0B8E83", true);

        // ENTREGA Y FECHA
        agregarTexto(tarjeta, "Entrega: " + d.getEntrega(), 13, "#586A6E", false);
        agregarTexto(tarjeta, "Fecha: " + d.getFecha(), 13, "#586A6E", false);

        contenedorDonaciones.addView(tarjeta);
    }

    private void agregarTexto(LinearLayout contenedor, String contenido, int tamano, String color, boolean negrita) {
        TextView texto = crearTexto(contenido, tamano, color, negrita);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(9);
        texto.setLayoutParams(params);
        contenedor.addView(texto);
    }

    private TextView crearTexto(String contenido, int tamano, String color, boolean negrita) {
        TextView texto = new TextView(this);
        texto.setText(contenido);
        texto.setTextSize(tamano);
        texto.setTextColor(Color.parseColor(color));
        if (negrita) {
            texto.setTypeface(null, Typeface.BOLD);
        }
        return texto;
    }

    private int dp(int valor) {
        return (int) (valor * getResources().getDisplayMetrics().density);
    }

    private String colorEstado(String estado) {
        if (EstadoDonacion.ACEPTADA.equals(estado)) return "#0B8E83";
        if (EstadoDonacion.RECHAZADA.equals(estado)) return "#C53030";
        if (EstadoDonacion.EN_REVISION.equals(estado)) return "#F45A3A";
        return "#9A6700";
    }
}
