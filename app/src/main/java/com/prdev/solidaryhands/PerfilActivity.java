package com.prdev.solidaryhands;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.prdev.solidaryhands.models.Perfil;
import com.prdev.solidaryhands.network.SessionManager;
import com.prdev.solidaryhands.network.SupabaseClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilActivity extends AppCompatActivity {

    private TextView txtNombreCompleto;
    private TextView txtCorreoPerfil;
    private TextView txtTelefonoPerfil;
    private ProgressBar progresoPerfil;

    private LinearLayout cardSeccionFundaciones;
    private LinearLayout cardSeccionHistorial;
    private LinearLayout cardSeccionConfiguracion;
    private Button btnCerrarSesionPerfil;
    private ImageButton btnVolverPerfil;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
        UiInsets.aplicar(this, R.id.perfilHeader, R.id.perfilScroll, true, true);

        sessionManager = SessionManager.getInstance(this);

        // CONECTAR VISTAS
        txtNombreCompleto = findViewById(R.id.txtNombreCompleto);
        txtCorreoPerfil = findViewById(R.id.txtCorreoPerfil);
        txtTelefonoPerfil = findViewById(R.id.txtTelefonoPerfil);
        progresoPerfil = findViewById(R.id.progresoPerfil);

        cardSeccionFundaciones = findViewById(R.id.cardSeccionFundaciones);
        cardSeccionHistorial = findViewById(R.id.cardSeccionHistorial);
        cardSeccionConfiguracion = findViewById(R.id.cardSeccionConfiguracion);
        btnCerrarSesionPerfil = findViewById(R.id.btnCerrarSesionPerfil);
        btnVolverPerfil = findViewById(R.id.btnVolverPerfil);

        // BOTÓN VOLVER
        btnVolverPerfil.setOnClickListener(v -> finish());

        // MOSTRAR DATOS PREVIOS EN CACHÉ DE SESIÓN
        mostrarDatosSesionLocal();

        // CONSULTAR DATOS FRESCOS DE SUPABASE
        cargarPerfilDesdeSupabase();

        // SECCIONES DE NAVEGACIÓN INTERNA
        cardSeccionFundaciones.setOnClickListener(v -> {
            // Regresar al Home en el carrusel de fundaciones
            finish();
        });

        cardSeccionHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(PerfilActivity.this, HistorialActivity.class);
            startActivity(intent);
        });

        cardSeccionConfiguracion.setOnClickListener(v -> {
            Toast.makeText(this, "Configuración: notificaciones activadas, versión 1.0", Toast.LENGTH_SHORT).show();
        });

        // BOTÓN CERRAR SESIÓN
        btnCerrarSesionPerfil.setOnClickListener(v -> cerrarSesion());
    }

    private void mostrarDatosSesionLocal() {
        if (sessionManager != null) {
            String nombre = sessionManager.getUserName();
            if (nombre == null || nombre.isEmpty() || nombre.equals("Usuario")) {
                nombre = "Carlos Mendoza";
            }
            txtNombreCompleto.setText(nombre);

            String email = sessionManager.getUserEmail();
            if (email == null || email.isEmpty()) {
                email = "carlos.mendoza@solidaryhands.org";
            }
            txtCorreoPerfil.setText(email);

            String tel = sessionManager.getUserPhone();
            txtTelefonoPerfil.setText((tel == null || tel.isEmpty()) ? "+591 71234567" : tel);
        }
    }

    private void cargarPerfilDesdeSupabase() {
        String userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) return;

        if (com.prdev.solidaryhands.network.SupabaseConfig.SUPABASE_URL.contains("TU_PROYECTO")) {
            if (progresoPerfil != null) progresoPerfil.setVisibility(View.GONE);
            return;
        }

        if (progresoPerfil != null) progresoPerfil.setVisibility(View.VISIBLE);

        SupabaseClient.getInstance(this).getDbService().getProfile("eq." + userId)
                .enqueue(new Callback<List<Perfil>>() {
                    @Override
                    public void onResponse(Call<List<Perfil>> call, Response<List<Perfil>> response) {
                        if (progresoPerfil != null) progresoPerfil.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            Perfil perfil = response.body().get(0);

                            String nombreCompleto = perfil.getNombreCompleto();
                            if (!nombreCompleto.isEmpty()) {
                                txtNombreCompleto.setText(nombreCompleto);
                                sessionManager.updateUserName(nombreCompleto);
                            }

                            if (perfil.getEmail() != null && !perfil.getEmail().isEmpty()) {
                                txtCorreoPerfil.setText(perfil.getEmail());
                            }

                            if (perfil.getTelefono() != null && !perfil.getTelefono().isEmpty()) {
                                txtTelefonoPerfil.setText(perfil.getTelefono());
                                sessionManager.updateUserPhone(perfil.getTelefono());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Perfil>> call, Throwable t) {
                        if (progresoPerfil != null) progresoPerfil.setVisibility(View.GONE);
                        // Conservamos los datos locales si no hay conexión
                    }
                });
    }

    private void cerrarSesion() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setPositiveButton("Sí, salir", (dialog, which) -> {
                    sessionManager.clearSession();
                    Toast.makeText(this, "Has cerrado sesión correctamente", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(PerfilActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
