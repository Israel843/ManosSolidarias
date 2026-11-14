package com.prdev.solidaryhands;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.prdev.solidaryhands.models.Fundacion;

public class FundacionActivity extends Activity {
    private TextView txtNombreFundacion;
    private TextView txtDescripcionFundacion;
    private TextView txtDireccionFundacion;
    private TextView txtProgramasFundacion;

    private String nombreFundacion;
    private Fundacion fundacionSeleccionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fundacion);

        // CONECTAR ELEMENTOS XML
        txtNombreFundacion = findViewById(R.id.txtNombreFundacion);
        txtDescripcionFundacion = findViewById(R.id.txtDescripcionFundacion);
        txtDireccionFundacion = findViewById(R.id.txtDireccionFundacion);
        txtProgramasFundacion = findViewById(R.id.txtProgramasFundacion);

        // RECIBIR FUNDACION SELECCIONADA
        nombreFundacion = getIntent().getStringExtra("nombreFundacion");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
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

        // BOTON VOLVER
        findViewById(R.id.btnVolverFundacion).setOnClickListener(v -> finish());
    }

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
