package com.prdev.solidaryhands;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    EditText edtUsuario, edtContrasena;
    Button btnIniciarSesion;
    TextView txtRegistrarse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // CONECTAR ELEMENTOS XML
        edtUsuario = findViewById(R.id.edtUsuario);
        edtContrasena = findViewById(R.id.edtContrasena);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        txtRegistrarse = findViewById(R.id.txtRegistrarse);

        // BOTON INICIAR SESION
        btnIniciarSesion.setOnClickListener(v -> {

            String usuario = edtUsuario.getText().toString().trim();
            String contrasena = edtContrasena.getText().toString().trim();

            if (usuario.isEmpty()) {
                edtUsuario.setError("Ingresa tu usuario");
                edtUsuario.requestFocus();
                return;
            }

            if (contrasena.isEmpty()) {
                edtContrasena.setError("Ingresa tu contraseña");
                edtContrasena.requestFocus();
                return;
            }
            Toast.makeText(
                    MainActivity.this,
                    "Bienvenido a Solidaryhands",
                    Toast.LENGTH_SHORT
            ).show();

            Intent intent = new Intent(
                    MainActivity.this,
                    HomeActivity.class
            );

            startActivity(intent);
            finish();


            // AQUI CONECTAREMOS EL INICIO
            // DE SESION CON NUESTRA BASE DE DATOS
        });

        // BOTON REGISTRARSE
        txtRegistrarse.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    RegistroActivity.class
            );

            startActivity(intent);

            // AQUI ABRIREMOS RegisterActivity
        });
    }
}
