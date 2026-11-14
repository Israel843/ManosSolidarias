package com.prdev.solidaryhands;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import java.util.List;

public class HistorialActivity extends Activity {

    private LinearLayout contenedorDonaciones;
    private LinearLayout contenedorVacio;
    private TextView txtTotalDonaciones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_historial);

        contenedorDonaciones =
                findViewById(R.id.contenedorDonaciones);

        contenedorVacio =
                findViewById(R.id.contenedorVacio);

        txtTotalDonaciones =
                findViewById(R.id.txtTotalDonaciones);

        findViewById(R.id.btnVolverHistorial)
                .setOnClickListener(v -> finish());

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

        contenedorDonaciones.removeAllViews();

        List<Donacion> lista =
                DonacionesRepositorio.obtenerTodas();

        txtTotalDonaciones.setText(
                lista.size() + (lista.size() == 1
                        ? " donación"
                        : " donaciones")
        );

        if (lista.isEmpty()) {
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

        LinearLayout.LayoutParams parametros =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        parametros.setMargins(0, 0, 0, dp(15));

        tarjeta.setLayoutParams(parametros);
        tarjeta.setElevation(dp(2));

        // NUMERO Y ESTADO
        LinearLayout encabezado = new LinearLayout(this);

        encabezado.setOrientation(LinearLayout.HORIZONTAL);
        encabezado.setGravity(Gravity.CENTER_VERTICAL);

        TextView titulo = crearTexto(
                "Donación #" + String.format("%03d", numero),
                17,
                "#287A5A",
                true
        );

        encabezado.addView(titulo,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                ));

        TextView estado = crearTexto(
                d.getEstado(),
                12,
                "#B7791F",
                true
        );

        encabezado.addView(estado);

        tarjeta.addView(encabezado);

        // INFORMACION DEL OBJETO
        agregarTexto(tarjeta,
                "Objeto: " + d.getTipo(), 16, "#263C50", true);

        agregarTexto(tarjeta,
                "Categoría: " + d.getCategoria(), 14, "#52645A", false);

        agregarTexto(tarjeta,
                "Descripción: " + d.getDescripcion(), 14, "#52645A", false);

        agregarTexto(tarjeta,
                "Cantidad: " + d.getCantidad() + " " + d.getUnidad(),
                14, "#52645A", false);

        agregarTexto(tarjeta,
                "Condición: " + d.getCondicion(),
                14, "#52645A", false);

        // FUNDACION
        agregarTexto(tarjeta,
                "Fundación destinataria",
                15, "#263C50", true);

        agregarTexto(tarjeta,
                d.getFundacion(),
                17, "#287A5A", true);

        // ENTREGA Y FECHA
        agregarTexto(tarjeta,
                "Entrega: " + d.getEntrega(),
                13, "#718096", false);

        agregarTexto(tarjeta,
                "Fecha: " + d.getFecha(),
                13, "#718096", false);

        contenedorDonaciones.addView(tarjeta);
    }

    private void agregarTexto(
            LinearLayout contenedor,
            String contenido,
            int tamano,
            String color,
            boolean negrita) {

        TextView texto = crearTexto(
                contenido, tamano, color, negrita
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        -1, -2
                );

        params.topMargin = dp(9);

        texto.setLayoutParams(params);

        contenedor.addView(texto);
    }

    private TextView crearTexto(
            String contenido,
            int tamano,
            String color,
            boolean negrita) {

        TextView texto = new TextView(this);

        texto.setText(contenido);
        texto.setTextSize(tamano);
        texto.setTextColor(android.graphics.Color.parseColor(color));

        if (negrita) {
            texto.setTypeface(null,
                    android.graphics.Typeface.BOLD);
        }

        return texto;
    }

    private int dp(int valor) {
        return (int) (valor *
                getResources().getDisplayMetrics().density);
    }
}

