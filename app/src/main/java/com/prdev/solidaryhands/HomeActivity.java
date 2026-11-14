package com.prdev.solidaryhands;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;

public class HomeActivity extends Activity {

    // BOTONES
    Button btnDonar, btnHistorial;
    ImageButton btnMenu, btnPerfil;

    // BUSCADOR
    EditText edtBuscar;

    // TARJETAS DE FUNDACIONES
    LinearLayout cardAlalay;
    LinearLayout cardPallini;
    LinearLayout cardWorldVision;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // CONECTAR BOTONES
        btnDonar = findViewById(R.id.btnDonar);
        btnHistorial = findViewById(R.id.btnHistorial);

        btnMenu = findViewById(R.id.btnMenu);
        btnPerfil = findViewById(R.id.btnPerfil);

        // CONECTAR BUSCADOR
        edtBuscar = findViewById(R.id.edtBuscar);

        // CONECTAR TARJETAS
        cardAlalay = findViewById(R.id.cardAlalay);
        cardPallini = findViewById(R.id.cardPallini);
        cardWorldVision = findViewById(R.id.cardWorldVision);

        // BOTON DONAR
        btnDonar.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, DonacionActivity.class);
            startActivity(intent);
        });

        // BOTON HISTORIAL
        btnHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, HistorialActivity.class);
            startActivity(intent);
        });

        // MENU
        btnMenu.setOnClickListener(v -> {

            Toast.makeText(
                    HomeActivity.this,
                    "Menú de Solidaryhands",
                    Toast.LENGTH_SHORT
            ).show();

        });

        // PERFIL
        btnPerfil.setOnClickListener(v -> {

            Toast.makeText(
                    HomeActivity.this,
                    "Próximamente: Mi perfil",
                    Toast.LENGTH_SHORT
            ).show();

        });

        // SELECCIONAR FUNDACION ALALAY
        cardAlalay.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, FundacionActivity.class);
            intent.putExtra("nombreFundacion", "Fundación Alalay");
            startActivity(intent);
        });

        // SELECCIONAR FUNDACION PALLINI
        cardPallini.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, FundacionActivity.class);
            intent.putExtra("nombreFundacion", "Fundación Pallini");
            startActivity(intent);
        });

        // SELECCIONAR WORLD VISION
        cardWorldVision.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, FundacionActivity.class);
            intent.putExtra("nombreFundacion", "World Vision Bolivia");
            startActivity(intent);
        });

        // BUSCADOR EN TIEMPO REAL
        edtBuscar.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {

                buscarFundacion(s.toString());

            }

            @Override
            public void afterTextChanged(Editable s) {
            }

        });

    }

    // METODO PARA BUSCAR FUNDACIONES
    private void buscarFundacion(String texto) {

        String busqueda = texto.toLowerCase().trim();

        // FUNDACION ALALAY
        boolean alalay =
                "fundación alalay".contains(busqueda)
                        || "alalay".contains(busqueda);

        // FUNDACION PALLINI
        boolean pallini =
                "fundación pallini".contains(busqueda)
                        || "pallini".contains(busqueda);

        // WORLD VISION BOLIVIA
        boolean worldVision =
                "world vision bolivia".contains(busqueda)
                        || "world vision".contains(busqueda)
                        || "world".contains(busqueda)
                        || "vision".contains(busqueda);

        // MOSTRAR TODAS SI EL BUSCADOR ESTA VACIO
        if (busqueda.isEmpty()) {

            cardAlalay.setVisibility(View.VISIBLE);
            cardPallini.setVisibility(View.VISIBLE);
            cardWorldVision.setVisibility(View.VISIBLE);

            return;
        }

        // FILTRAR FUNDACIONES
        cardAlalay.setVisibility(
                alalay ? View.VISIBLE : View.GONE
        );

        cardPallini.setVisibility(
                pallini ? View.VISIBLE : View.GONE
        );

        cardWorldVision.setVisibility(
                worldVision ? View.VISIBLE : View.GONE
        );

    }

}
