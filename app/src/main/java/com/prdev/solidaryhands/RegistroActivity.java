package com.prdev.solidaryhands;

import android.app.Activity;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegistroActivity extends Activity {

    EditText edtNombre;
    EditText edtApPaterno;
    EditText edtApMaterno;
    EditText edtTelefono;
    EditText edtCorreo;
    EditText edtRegistroContrasena;

    Button btnRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        // CONECTAR CAMPOS XML
        edtNombre = findViewById(R.id.edtNombre);
        edtApPaterno = findViewById(R.id.edtApPaterno);
        edtApMaterno = findViewById(R.id.edtApMaterno);
        edtTelefono = findViewById(R.id.edtTelefono);
        edtCorreo = findViewById(R.id.edtCorreo);
        edtRegistroContrasena =
                findViewById(R.id.edtRegistroContrasena);

        btnRegistrar = findViewById(R.id.btnRegistrar);

        // BOTON REGISTRAR
        btnRegistrar.setOnClickListener(v -> {

            String nombre = edtNombre.getText().toString().trim();
            String paterno = edtApPaterno.getText().toString().trim();
            String materno = edtApMaterno.getText().toString().trim();
            String telefono = edtTelefono.getText().toString().trim();
            String correo = edtCorreo.getText().toString().trim();
            String contrasena =
                    edtRegistroContrasena.getText().toString();

            // VALIDAR NOMBRE
            if (nombre.isEmpty()) {
                edtNombre.setError("Ingresa tu nombre");
                edtNombre.requestFocus();
                return;
            }

            // VALIDAR APELLIDO PATERNO
            if (paterno.isEmpty()) {
                edtApPaterno.setError("Ingresa tu apellido paterno");
                edtApPaterno.requestFocus();
                return;
            }

            // VALIDAR APELLIDO MATERNO
            if (materno.isEmpty()) {
                edtApMaterno.setError("Ingresa tu apellido materno");
                edtApMaterno.requestFocus();
                return;
            }

            // VALIDAR TELEFONO
            if (telefono.isEmpty()) {
                edtTelefono.setError("Ingresa tu teléfono");
                edtTelefono.requestFocus();
                return;
            }

            // VALIDAR CORREO
            if (correo.isEmpty()) {
                edtCorreo.setError("Ingresa tu correo");
                edtCorreo.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                edtCorreo.setError("Ingresa un correo válido");
                edtCorreo.requestFocus();
                return;
            }

            // VALIDAR CONTRASEÑA
            if (contrasena.isEmpty()) {
                edtRegistroContrasena.setError(
                        "Ingresa una contraseña");
                edtRegistroContrasena.requestFocus();
                return;
            }

            if (contrasena.length() < 8) {
                edtRegistroContrasena.setError(
                        "Debe tener al menos 8 caracteres");
                edtRegistroContrasena.requestFocus();
                return;
            }

            // REGISTRO VALIDADO
            Toast.makeText(
                    RegistroActivity.this,
                    "¡Datos validados correctamente!",
                    Toast.LENGTH_LONG
            ).show();

            // AQUI CONECTAREMOS LA BASE DE DATOS

        });
    }
}
