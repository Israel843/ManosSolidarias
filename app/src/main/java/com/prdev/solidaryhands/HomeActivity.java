package com.prdev.solidaryhands;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.prdev.solidaryhands.adapters.FundacionAdapter;
import com.prdev.solidaryhands.models.Fundacion;
import com.prdev.solidaryhands.network.SessionManager;
import com.prdev.solidaryhands.network.SupabaseClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    // VISTAS PRINCIPALES
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private Button btnDonar, btnHistorial;
    private ImageButton btnMenu, btnPerfil;
    private EditText edtBuscar;
    private TextView txtSaludo;
    private ProgressBar progresoFundaciones;
    private TextView txtSinFundaciones;

    // RECYCLERVIEW Y ADAPTADOR
    private RecyclerView recyclerFundaciones;
    private FundacionAdapter fundacionAdapter;
    private final List<Fundacion> listaFundaciones = new ArrayList<>();

    // GESTOR DE SESIÓN
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        UiInsets.aplicar(this, R.id.homeHeader, R.id.homeBottomNav, false, true);

        sessionManager = SessionManager.getInstance(this);

        // CONECTAR VISTAS
        drawerLayout = findViewById(R.id.drawerLayout);
        navView = findViewById(R.id.navView);
        btnDonar = findViewById(R.id.btnDonar);
        btnHistorial = findViewById(R.id.btnHistorial);
        btnMenu = findViewById(R.id.btnMenu);
        btnPerfil = findViewById(R.id.btnPerfil);
        edtBuscar = findViewById(R.id.edtBuscar);
        txtSaludo = findViewById(R.id.txtSaludo);
        progresoFundaciones = findViewById(R.id.progresoFundaciones);
        txtSinFundaciones = findViewById(R.id.txtSinFundaciones);
        recyclerFundaciones = findViewById(R.id.recyclerFundaciones);

        // SALUDO PERSONALIZADO
        actualizarSaludo();

        // CONFIGURAR RECYCLERVIEW HORIZONTAL
        recyclerFundaciones.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        fundacionAdapter = new FundacionAdapter(this, listaFundaciones);
        recyclerFundaciones.setAdapter(fundacionAdapter);

        // CONFIGURAR NAVIGATION DRAWER
        configurarDrawer();

        // ACCIONES DE BOTONES
        btnDonar.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, DonacionActivity.class);
            startActivity(intent);
        });

        btnHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, HistorialActivity.class);
            startActivity(intent);
        });

        btnMenu.setOnClickListener(v -> {
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        btnPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, PerfilActivity.class);
            startActivity(intent);
        });

        // La barra inferior replica las rutas principales sin quitar el menú lateral.
        findViewById(R.id.btnNavInicio).setOnClickListener(v ->
                findViewById(R.id.homeScroll).scrollTo(0, 0));
        findViewById(R.id.btnNavDonar).setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, DonacionActivity.class)));
        findViewById(R.id.btnNavHistorial).setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, HistorialActivity.class)));
        findViewById(R.id.btnNavPerfil).setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, PerfilActivity.class)));

        // BUSCADOR EN TIEMPO REAL
        edtBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (fundacionAdapter != null) {
                    fundacionAdapter.filtrar(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // CARGAR FUNDACIONES DESDE SUPABASE
        cargarFundacionesDesdeSupabase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        actualizarSaludo();
        actualizarDatosDrawer();
    }

    private void actualizarSaludo() {
        if (txtSaludo != null && sessionManager != null) {
            String nombre = sessionManager.getUserName();
            txtSaludo.setText("Hola, " + (nombre.isEmpty() ? "amigo solidario" : nombre) + " 👋");
        }
    }

    private void configurarDrawer() {
        if (navView == null) return;

        actualizarDatosDrawer();

        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_inicio) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else if (id == R.id.nav_calendario) {
                drawerLayout.closeDrawer(GravityCompat.START);
                Toast.makeText(this, "Calendario de actividades comunitarias", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_notificaciones) {
                drawerLayout.closeDrawer(GravityCompat.START);
                Toast.makeText(this, "No tienes notificaciones pendientes", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_fundaciones) {
                drawerLayout.closeDrawer(GravityCompat.START);
                if (recyclerFundaciones != null) {
                    recyclerFundaciones.smoothScrollToPosition(0);
                }
            } else if (id == R.id.nav_perfil) {
                drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(HomeActivity.this, PerfilActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_cerrar_sesion) {
                drawerLayout.closeDrawer(GravityCompat.START);
                cerrarSesion();
            }

            return true;
        });
    }

    private void actualizarDatosDrawer() {
        if (navView == null || navView.getHeaderCount() == 0) return;
        View headerView = navView.getHeaderView(0);
        TextView txtNombre = headerView.findViewById(R.id.txtDrawerNombre);
        TextView txtEmail = headerView.findViewById(R.id.txtDrawerEmail);

        if (txtNombre != null) {
            txtNombre.setText(sessionManager.getUserName());
        }
        if (txtEmail != null) {
            txtEmail.setText(sessionManager.getUserEmail());
        }
    }

    private void cargarFundacionesDesdeSupabase() {
        if (com.prdev.solidaryhands.network.SupabaseConfig.SUPABASE_URL.contains("TU_PROYECTO")) {
            if (progresoFundaciones != null) progresoFundaciones.setVisibility(View.GONE);
            cargarFundacionesLocalesPorDefecto();
            return;
        }

        if (progresoFundaciones != null) progresoFundaciones.setVisibility(View.VISIBLE);
        if (txtSinFundaciones != null) txtSinFundaciones.setVisibility(View.GONE);

        SupabaseClient.getInstance(this).getDbService().getFoundations()
                .enqueue(new Callback<List<Fundacion>>() {
                    @Override
                    public void onResponse(Call<List<Fundacion>> call, Response<List<Fundacion>> response) {
                        if (progresoFundaciones != null) progresoFundaciones.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            listaFundaciones.clear();
                            listaFundaciones.addAll(response.body());
                            fundacionAdapter.actualizarDatos(listaFundaciones);
                        } else {
                            // Si la base de datos aún no tiene registros o retorna vacío, cargamos datos por defecto
                            cargarFundacionesLocalesPorDefecto();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Fundacion>> call, Throwable t) {
                        if (progresoFundaciones != null) progresoFundaciones.setVisibility(View.GONE);
                        // Modo de contingencia sin conexión: mostramos las fundaciones base
                        cargarFundacionesLocalesPorDefecto();
                    }
                });
    }

    private void cargarFundacionesLocalesPorDefecto() {
        listaFundaciones.clear();
        listaFundaciones.add(new Fundacion(
                1L,
                "Fundación Alalay",
                "Atención y protección integral a niños, adolescentes y familias.",
                "Bolivia",
                "Protección, desarrollo y nutrición infantil.",
                null, null, null
        ));
        listaFundaciones.add(new Fundacion(
                2L,
                "Fundación Pallini",
                "Programas comunitarios, apoyo social y actividades educativas.",
                "Bolivia",
                "Educación, deportes y bienestar familiar.",
                null, null, null
        ));
        listaFundaciones.add(new Fundacion(
                3L,
                "World Vision Bolivia",
                "Transformación humana integral y desarrollo comunitario para la niñez.",
                "Bolivia",
                "Salud, saneamiento, educación y derechos infantiles.",
                null, null, null
        ));
        fundacionAdapter.actualizarDatos(listaFundaciones);
    }

    private void cerrarSesion() {
        sessionManager.clearSession();
        Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
