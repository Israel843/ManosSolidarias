package com.prdev.solidaryhands;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.prdev.solidaryhands.models.AuthRequest;
import com.prdev.solidaryhands.models.AuthResponse;
import com.prdev.solidaryhands.models.Perfil;
import com.prdev.solidaryhands.models.User;
import com.prdev.solidaryhands.network.SessionManager;
import com.prdev.solidaryhands.network.SupabaseClient;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText edtUsuario, edtContrasena;
    private Button btnIniciarSesion;
    private TextView txtRegistrarse;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = SessionManager.getInstance(this);

        // Si ya hay una sesión activa, redirigir directo al Home
        if (sessionManager.isLoggedIn()) {
            irAHome();
            return;
        }

        setContentView(R.layout.activity_main);

        // CONECTAR ELEMENTOS XML
        edtUsuario = findViewById(R.id.edtUsuario);
        edtContrasena = findViewById(R.id.edtContrasena);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        txtRegistrarse = findViewById(R.id.txtRegistrarse);

        // BOTON INICIAR SESION CON SUPABASE AUTH
        btnIniciarSesion.setOnClickListener(v -> iniciarSesionSupabase());

        // BOTON REGISTRARSE
        txtRegistrarse.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegistroActivity.class);
            startActivity(intent);
        });
    }

    private void iniciarSesionSupabase() {
        String correo = edtUsuario.getText().toString().trim();
        String contrasena = edtContrasena.getText().toString().trim();

        if (correo.isEmpty()) {
            edtUsuario.setError("Ingresa tu correo electrónico");
            edtUsuario.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            edtUsuario.setError("Ingresa un correo electrónico válido");
            edtUsuario.requestFocus();
            return;
        }

        if (contrasena.isEmpty()) {
            edtContrasena.setError("Ingresa tu contraseña");
            edtContrasena.requestFocus();
            return;
        }

        // Si la URL de Supabase aún no ha sido configurada con el proyecto real, iniciar en modo demostración/local
        if (com.prdev.solidaryhands.network.SupabaseConfig.SUPABASE_URL.contains("TU_PROYECTO")) {
            Toast.makeText(MainActivity.this, "Iniciando sesión en modo demostración...", Toast.LENGTH_SHORT).show();
            sessionManager.saveSession("token_demo", "demo_user", correo, "Usuario Solidario", "+591 71234567");
            irAHome();
            return;
        }

        // FEEDBACK VISUAL DE CARGA
        btnIniciarSesion.setEnabled(false);
        btnIniciarSesion.setText("Verificando credenciales...");

        AuthRequest request = new AuthRequest(correo, contrasena);

        SupabaseClient.getInstance(this).getAuthService().signIn(request)
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        btnIniciarSesion.setEnabled(true);
                        btnIniciarSesion.setText("Iniciar sesión");

                        if (response.isSuccessful() && response.body() != null) {
                            AuthResponse authResp = response.body();
                            String token = authResp.getAccessToken();
                            User user = authResp.getUser();
                            String userId = user != null ? user.getId() : "";
                            String userEmail = user != null ? user.getEmail() : correo;

                            // Intentar extraer nombre de metadata
                            String nombreInicial = "Usuario";
                            String telefonoInicial = "";
                            if (user != null && user.getUserMetadata() != null) {
                                Map<String, Object> meta = user.getUserMetadata();
                                if (meta.containsKey("nombre")) {
                                    nombreInicial = String.valueOf(meta.get("nombre"));
                                }
                                if (meta.containsKey("telefono")) {
                                    telefonoInicial = String.valueOf(meta.get("telefono"));
                                }
                            }

                            // Guardar sesión
                            sessionManager.saveSession(token, userId, userEmail, nombreInicial, telefonoInicial);

                            // Consultar perfil completo de Supabase en segundo plano
                            cargarYGuardarPerfilCompleto(userId);

                            Toast.makeText(MainActivity.this, "¡Bienvenido a Solidaryhands!", Toast.LENGTH_SHORT).show();
                            irAHome();

                        } else {
                            String mensajeError = "Correo o contraseña incorrectos.";
                            if (response.code() == 400) {
                                mensajeError = "Credenciales incorrectas. Verifica tu correo y contraseña.";
                            }
                            Toast.makeText(MainActivity.this, mensajeError, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        btnIniciarSesion.setEnabled(true);
                        btnIniciarSesion.setText("Iniciar sesión");

                        // Si la URL de Supabase es la plantilla o falla la red, permitir acceso local para probar la app
                        if (com.prdev.solidaryhands.network.SupabaseConfig.SUPABASE_URL.contains("TU_PROYECTO")
                                || t instanceof java.net.UnknownHostException) {
                            Toast.makeText(MainActivity.this, "Iniciando sesión en modo local/demostración...", Toast.LENGTH_SHORT).show();
                            sessionManager.saveSession("token_demo", "demo_user", correo, "Carlos Mendoza", "+591 71234567");
                            irAHome();
                        } else {
                            Toast.makeText(MainActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void cargarYGuardarPerfilCompleto(String userId) {
        if (userId == null || userId.isEmpty()) return;

        SupabaseClient.getInstance(this).getDbService().getProfile("eq." + userId)
                .enqueue(new Callback<List<Perfil>>() {
                    @Override
                    public void onResponse(Call<List<Perfil>> call, Response<List<Perfil>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            Perfil perfil = response.body().get(0);
                            String nombreCompleto = perfil.getNombreCompleto();
                            if (!nombreCompleto.isEmpty()) {
                                sessionManager.updateUserName(nombreCompleto);
                            }
                            if (perfil.getTelefono() != null) {
                                sessionManager.updateUserPhone(perfil.getTelefono());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Perfil>> call, Throwable t) {
                        // Ignorar en background
                    }
                });
    }

    private void irAHome() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
