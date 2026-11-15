package com.prdev.solidaryhands;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.prdev.solidaryhands.models.AuthResponse;
import com.prdev.solidaryhands.models.Perfil;
import com.prdev.solidaryhands.models.SignUpRequest;
import com.prdev.solidaryhands.models.User;
import com.prdev.solidaryhands.network.SessionManager;
import com.prdev.solidaryhands.network.SupabaseClient;
import com.prdev.solidaryhands.network.SupabaseConfig;
import com.prdev.solidaryhands.network.SupabaseError;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistroActivity extends AppCompatActivity {

    private EditText edtNombre;
    private EditText edtApPaterno;
    private EditText edtApMaterno;
    private EditText edtTelefono;
    private EditText edtCorreo;
    private EditText edtRegistroContrasena;
    private Button btnRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        UiInsets.aplicar(this, R.id.registroContent, R.id.registroContent, true, false);

        // CONECTAR CAMPOS XML
        edtNombre = findViewById(R.id.edtNombre);
        edtApPaterno = findViewById(R.id.edtApPaterno);
        edtApMaterno = findViewById(R.id.edtApMaterno);
        edtTelefono = findViewById(R.id.edtTelefono);
        edtCorreo = findViewById(R.id.edtCorreo);
        edtRegistroContrasena = findViewById(R.id.edtRegistroContrasena);
        btnRegistrar = findViewById(R.id.btnRegistrar);

        // BOTON REGISTRAR
        btnRegistrar.setOnClickListener(v -> registrarUsuarioSupabase());
    }

    private void registrarUsuarioSupabase() {
        String nombre = edtNombre.getText().toString().trim();
        String paterno = edtApPaterno.getText().toString().trim();
        String materno = edtApMaterno.getText().toString().trim();
        String telefono = edtTelefono.getText().toString().trim();
        String correo = edtCorreo.getText().toString().trim();
        String contrasena = edtRegistroContrasena.getText().toString();

        // VALIDACIONES
        if (nombre.isEmpty()) {
            edtNombre.setError("Ingresa tu nombre");
            edtNombre.requestFocus();
            return;
        }

        if (paterno.isEmpty()) {
            edtApPaterno.setError("Ingresa tu apellido paterno");
            edtApPaterno.requestFocus();
            return;
        }

        if (materno.isEmpty()) {
            edtApMaterno.setError("Ingresa tu apellido materno");
            edtApMaterno.requestFocus();
            return;
        }

        if (telefono.isEmpty()) {
            edtTelefono.setError("Ingresa tu teléfono");
            edtTelefono.requestFocus();
            return;
        }

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

        if (contrasena.isEmpty()) {
            edtRegistroContrasena.setError("Ingresa una contraseña");
            edtRegistroContrasena.requestFocus();
            return;
        }

        if (contrasena.length() < 6) {
            edtRegistroContrasena.setError("Debe tener al menos 6 caracteres");
            edtRegistroContrasena.requestFocus();
            return;
        }

        String nombreCompleto = (nombre + " " + paterno + " " + materno).trim();

        // Si la URL de Supabase es la plantilla predeterminada, registrar en modo local de demostración
        if (SupabaseConfig.SUPABASE_URL.contains("TU_PROYECTO")) {
            Toast.makeText(RegistroActivity.this, "Registrando usuario...", Toast.LENGTH_SHORT).show();
            SessionManager.getInstance(RegistroActivity.this)
                    .saveSession("token_demo", "demo_user", correo, nombreCompleto, telefono);

            Intent intent = new Intent(RegistroActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // DESHABILITAR BOTÓN MIENTRAS SE PROCESA
        btnRegistrar.setEnabled(false);
        btnRegistrar.setText("Registrando en Supabase...");

        // METADATA DEL USUARIO PARA VINCULAR CON PROFILES
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("nombre", nombre);
        metadata.put("apellido_paterno", paterno);
        metadata.put("apellido_materno", materno);
        metadata.put("telefono", telefono);

        SignUpRequest signUpRequest = new SignUpRequest(correo, contrasena, metadata);

        SupabaseClient.getInstance(this).getAuthService().signUp(signUpRequest)
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        btnRegistrar.setEnabled(true);
                        btnRegistrar.setText("Registrar");

                        if (response.isSuccessful() && response.body() != null) {
                            AuthResponse authResp = response.body();
                            User user = authResp.getUser();
                            String userId = user != null ? user.getId() : null;
                            String token = authResp.getAccessToken();

                            String nombreCompleto = (nombre + " " + paterno + " " + materno).trim();

                            // Si la confirmación de correo está desactivada y devuelve token, iniciamos sesión
                            if (token != null && !token.isEmpty() && userId != null) {
                                SessionManager.getInstance(RegistroActivity.this)
                                        .saveSession(token, userId, correo, nombreCompleto, telefono);
                                SessionManager.getInstance(RegistroActivity.this)
                                        .saveRefreshToken(authResp.getRefreshToken());

                                // Respaldo: asegurar perfil en la tabla profiles
                                asegurarPerfilEnBaseDeDatos(userId, correo, nombre, paterno, materno, telefono);

                                Toast.makeText(RegistroActivity.this, "¡Registro exitoso! Bienvenido.", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(RegistroActivity.this, HomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                // Supabase NO devolvió sesión: tiene activada la confirmación de correo.
                                Toast.makeText(RegistroActivity.this,
                                        "¡Cuenta creada! Revisa tu correo y abre el enlace de confirmación antes de iniciar sesión.",
                                        Toast.LENGTH_LONG).show();
                                finish();
                            }
                        } else {
                            Toast.makeText(RegistroActivity.this,
                                    SupabaseError.mensajeRegistro(response),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        btnRegistrar.setEnabled(true);
                        btnRegistrar.setText("Registrar");

                        if (t instanceof UnknownHostException) {
                            Toast.makeText(RegistroActivity.this,
                                    "Sin conexión a internet. Verifica tu red e intenta de nuevo.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(RegistroActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void asegurarPerfilEnBaseDeDatos(String userId, String email, String nombre, String pat, String mat, String tel) {
        Perfil perfil = new Perfil(userId, email, nombre, pat, mat, tel);
        SupabaseClient.getInstance(this).getDbService()
                .upsertProfile("resolution=merge-duplicates", perfil)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {}

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {}
                });
    }
}
