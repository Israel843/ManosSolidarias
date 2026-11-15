package com.prdev.solidaryhands;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.prdev.solidaryhands.models.Fundacion;

import java.util.List;

public class FundacionActivity extends Activity {

    private static final int TAB_NOVEDADES = 0;
    private static final int TAB_TRABAJOS = 1;
    private static final int TAB_PERSONAS = 2;

    private TextView txtNombreFundacion;
    private TextView txtDescripcionFundacion;
    private TextView txtDireccionFundacion;
    private TextView txtProgramasFundacion;
    private LinearLayout contenedorContenido;
    private Button btnNovedades, btnTrabajos, btnPersonas;

    private String nombreFundacion;
    private Fundacion fundacionSeleccionada;
    private FundacionContenido.Contenido contenido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fundacion);
        UiInsets.aplicar(this, R.id.fundacionHeader, R.id.fundacionBottomNav, true, true);

        // CONECTAR ELEMENTOS XML
        txtNombreFundacion = findViewById(R.id.txtNombreFundacion);
        txtDescripcionFundacion = findViewById(R.id.txtDescripcionFundacion);
        txtDireccionFundacion = findViewById(R.id.txtDireccionFundacion);
        txtProgramasFundacion = findViewById(R.id.txtProgramasFundacion);
        contenedorContenido = findViewById(R.id.contenedorContenido);
        btnNovedades = findViewById(R.id.btnNovedades);
        btnTrabajos = findViewById(R.id.btnTrabajos);
        btnPersonas = findViewById(R.id.btnPersonas);

        // RECIBIR FUNDACION SELECCIONADA
        nombreFundacion = getIntent().getStringExtra("nombreFundacion");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            fundacionSeleccionada = getIntent().getSerializableExtra("fundacionObjeto", Fundacion.class);
        } else {
            //noinspection deprecation
            fundacionSeleccionada = (Fundacion) getIntent().getSerializableExtra("fundacionObjeto");
        }

        if (fundacionSeleccionada == null && (nombreFundacion == null || nombreFundacion.trim().isEmpty())) {
            Toast.makeText(this, "No se recibió una fundación.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // CARGAR INFORMACION
        cargarFundacion();

        // CONTENIDO (Novedades / Trabajos / Personas)
        contenido = FundacionContenido.obtener(
                fundacionSeleccionada.getNombre(),
                fundacionSeleccionada.getProgramas());

        // BOTON VOLVER
        findViewById(R.id.btnVolverFundacion).setOnClickListener(v -> finish());
        findViewById(R.id.btnDonarFundacion).setOnClickListener(v ->
                startActivity(new Intent(FundacionActivity.this, DonacionActivity.class)));

        // BOTONES INFERIORES (antes no tenían ninguna acción)
        btnNovedades.setOnClickListener(v -> mostrarTab(TAB_NOVEDADES));
        btnTrabajos.setOnClickListener(v -> mostrarTab(TAB_TRABAJOS));
        btnPersonas.setOnClickListener(v -> mostrarTab(TAB_PERSONAS));

        mostrarTab(TAB_NOVEDADES);
    }

    // =========================================================================
    //  PESTAÑAS
    // =========================================================================
    private void mostrarTab(int tab) {
        marcarBoton(btnNovedades, tab == TAB_NOVEDADES);
        marcarBoton(btnTrabajos, tab == TAB_TRABAJOS);
        marcarBoton(btnPersonas, tab == TAB_PERSONAS);

        contenedorContenido.removeAllViews();

        List<FundacionContenido.Entrada> lista;
        if (tab == TAB_NOVEDADES) {
            lista = contenido.novedades;
        } else if (tab == TAB_TRABAJOS) {
            lista = contenido.trabajos;
        } else {
            lista = contenido.personas;
        }

        if (lista.isEmpty()) {
            TextView vacio = new TextView(this);
            vacio.setText("Aún no hay información en esta sección.");
            vacio.setTextColor(Color.parseColor("#586A6E"));
            vacio.setTextSize(14f);
            vacio.setPadding(0, 40, 0, 0);
            contenedorContenido.addView(vacio);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (FundacionContenido.Entrada entrada : lista) {
            View tarjeta = inflater.inflate(R.layout.item_novedades, contenedorContenido, false);
            llenarTarjeta(tarjeta, entrada);
            contenedorContenido.addView(tarjeta);
        }
    }

    private void marcarBoton(Button boton, boolean activo) {
        boton.setBackgroundTintList(ColorStateList.valueOf(
                Color.parseColor(activo ? "#DDF2EE" : "#FFFFFF")));
        boton.setTextColor(Color.parseColor(activo ? "#0B8E83" : "#586A6E"));
    }

    private void llenarTarjeta(View tarjeta, FundacionContenido.Entrada entrada) {
        ImageView img = tarjeta.findViewById(R.id.imgNovedad);
        TextView categoria = tarjeta.findViewById(R.id.txtCategoriaNovedad);
        TextView titulo = tarjeta.findViewById(R.id.txtTituloNovedad);
        TextView fecha = tarjeta.findViewById(R.id.txtFechaNovedad);
        TextView descripcion = tarjeta.findViewById(R.id.txtDescripcionNovedad);
        Button btnVideo = tarjeta.findViewById(R.id.btnDetalleNovedad);

        categoria.setText(entrada.categoria);
        titulo.setText(entrada.titulo);
        descripcion.setText(entrada.descripcion);

        if (entrada.fecha == null || entrada.fecha.isEmpty()) {
            fecha.setVisibility(View.GONE);
        } else {
            fecha.setVisibility(View.VISIBLE);
            fecha.setText(entrada.fecha);
        }

        if (entrada.tieneVideo()) {
            // Miniatura del video de YouTube + botón para verlo
            img.setVisibility(View.VISIBLE);
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(this)
                    .load(FundacionContenido.miniaturaYoutube(entrada.videoUrl))
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(img);

            btnVideo.setVisibility(View.VISIBLE);
            btnVideo.setText("▶  Ver video en YouTube");
            View.OnClickListener abrir = v -> abrirVideo(entrada.videoUrl);
            btnVideo.setOnClickListener(abrir);
            img.setOnClickListener(abrir);
        } else {
            // Sin link de YouTube: no se muestra imagen ni botón
            img.setVisibility(View.GONE);
            btnVideo.setVisibility(View.GONE);
        }
    }

    private void abrirVideo(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No hay ninguna app para abrir el video.", Toast.LENGTH_SHORT).show();
        }
    }

    // =========================================================================
    //  INFORMACION GENERAL DE LA FUNDACION
    // =========================================================================
    private void cargarFundacion() {
        if (fundacionSeleccionada != null) {
            mostrarInformacion();
            return;
        }

        switch (nombreFundacion) {
            case "Fundación Alalay":
                fundacionSeleccionada = new Fundacion(
                        "Fundación Alalay",
                        "Organización que trabaja por el bienestar, la protección y el desarrollo de niños, adolescentes y familias.",
                        "Bolivia",
                        "Protección de la niñez, prevención, desarrollo integral y apoyo familiar."
                );
                break;

            case "Fundación Pallini":
                fundacionSeleccionada = new Fundacion(
                        "Fundación Pallini",
                        "Fundación orientada al apoyo social, educativo y comunitario.",
                        "Bolivia",
                        "Educación, actividades deportivas, apoyo infantil y fortalecimiento familiar."
                );
                break;

            case "World Vision Bolivia":
                fundacionSeleccionada = new Fundacion(
                        "World Vision Bolivia",
                        "Organización que trabaja por el bienestar, los derechos y la protección de niñas, niños y adolescentes.",
                        "Bolivia",
                        "Salud, saneamiento, educación y protección de la niñez."
                );
                break;

            default:
                fundacionSeleccionada = new Fundacion(
                        nombreFundacion,
                        "Fundación aliada comprometida con el desarrollo social y comunitario.",
                        "Bolivia",
                        "Programas integrales de apoyo y donaciones directas."
                );
                break;
        }

        mostrarInformacion();
    }

    private void mostrarInformacion() {
        if (fundacionSeleccionada == null) return;
        txtNombreFundacion.setText(fundacionSeleccionada.getNombre());
        txtDescripcionFundacion.setText(fundacionSeleccionada.getDescripcion());
        txtDireccionFundacion.setText("Ubicación: " + fundacionSeleccionada.getDireccion());
        txtProgramasFundacion.setText(fundacionSeleccionada.getProgramas());
    }
}
