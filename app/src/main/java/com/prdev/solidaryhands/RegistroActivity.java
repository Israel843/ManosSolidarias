package com.prdev.solidaryhands;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.prdev.solidaryhands.models.AuthResponse;
import com.prdev.solidaryhands.models.Perfil;
import com.prdev.solidaryhands.models.RolUsuario;
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

    private View pasoRol;
    private View pasoDatos;
    private View pasoAcceso;
    private TextView txtPasoRegistro;
    private View progreso1;
    private View progreso2;
    private View progreso3;
    private int pasoActual = 1;

    private RadioGroup grupoRol;
    private String rolSeleccionado;

    private EditText edtNombre;
    private EditText edtApPaterno;
    private EditText edtApMaterno;
    private EditText edtTelefono;
    private EditText edtCiudad;
    private LinearLayout contenedorDatosFundacion;
    private EditText edtNombreFundacion;
    private EditText edtDescripcionFundacion;
    private EditText edtDireccionFundacion;

    private EditText edtCorreo;
    private EditText edtRegistroContrasena;
    private Button btnRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        UiInsets.aplicar(this, R.id.registroContent, R.id.registroContent, true, false);

        txtPasoRegistro = findViewById(R.id.txtPasoRegistro);
        progreso1 = findViewById(R.id.progresoRegistro1);
        progreso2 = findViewById(R.id.progresoRegistro2);
        progreso3 = findViewById(R.id.progresoRegistro3);

        FrameLayout contenedor = findViewById(R.id.contenedorRegistro);
        LayoutInflater inflater = LayoutInflater.from(this);
        pasoRol = inflater.inflate(R.layout.registro_paso_rol, contenedor, false);
        pasoDatos = inflater.inflate(R.layout.registro_paso_datos, contenedor, false);
        pasoAcceso = inflater.inflate(R.layout.registro_paso_acceso, contenedor, false);
        contenedor.addView(pasoRol);
        contenedor.addView(pasoDatos);
        contenedor.addView(pasoAcceso);

        conectarVistas();
        configurarAcciones();
        mostrarPaso(1);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                retroceder();
            }
        });
    }

    private void conectarVistas() {
        grupoRol = pasoRol.findViewById(R.id.grupoRol);

        edtNombre = pasoDatos.findViewById(R.id.edtNombre);
        edtApPaterno = pasoDatos.findViewById(R.id.edtApPaterno);
        edtApMaterno = pasoDatos.findViewById(R.id.edtApMaterno);
        edtTelefono = pasoDatos.findViewById(R.id.edtTelefono);
        edtCiudad = pasoDatos.findViewById(R.id.edtCiudad);
        contenedorDatosFundacion = pasoDatos.findViewById(R.id.contenedorDatosFundacion);
        edtNombreFundacion = pasoDatos.findViewById(R.id.edtNombreFundacion);
        edtDescripcionFundacion = pasoDatos.findViewById(R.id.edtDescripcionFundacion);
        edtDireccionFundacion = pasoDatos.findViewById(R.id.edtDireccionFundacion);

        edtCorreo = pasoAcceso.findViewById(R.id.edtCorreo);
        edtRegistroContrasena = pasoAcceso.findViewById(R.id.edtRegistroContrasena);
        btnRegistrar = pasoAcceso.findViewById(R.id.btnRegistrar);
    }

    private void configurarAcciones() {
        findViewById(R.id.btnVolverRegistro).setOnClickListener(v -> retroceder());

        pasoRol.findViewById(R.id.btnSiguienteRol).setOnClickListener(v -> {
            int seleccion = grupoRol.getCheckedRadioButtonId();
            if (seleccion != R.id.radioDonante && seleccion != R.id.radioFundacion) {
                Toast.makeText(this, "Selecciona Donante o Fundación.", Toast.LENGTH_SHORT).show();
                return;
            }
            rolSeleccionado = seleccion == R.id.radioFundacion
                    ? RolUsuario.FUNDACION : RolUsuario.DONANTE;
            configurarFormularioSegunRol();
            mostrarPaso(2);
        });

        pasoDatos.findViewById(R.id.btnSiguienteDatos).setOnClickListener(v -> {
            if (!validarDatosPersonales()) return;
            actualizarResumenAcceso();
            mostrarPaso(3);
        });

        pasoAcceso.findViewById(R.id.btnAnteriorAcceso).setOnClickListener(v -> mostrarPaso(2));
        btnRegistrar.setOnClickListener(v -> registrarUsuarioSupabase());
    }

    private void mostrarPaso(int numero) {
        pasoActual = numero;
        pasoRol.setVisibility(numero == 1 ? View.VISIBLE : View.GONE);
        pasoDatos.setVisibility(numero == 2 ? View.VISIBLE : View.GONE);
        pasoAcceso.setVisibility(numero == 3 ? View.VISIBLE : View.GONE);
        txtPasoRegistro.setText("Paso " + numero + " de 3");
        progreso1.setBackgroundResource(R.drawable.bg_step_active);
        progreso2.setBackgroundResource(numero >= 2 ? R.drawable.bg_step_active : R.drawable.bg_step_track);
        progreso3.setBackgroundResource(numero >= 3 ? R.drawable.bg_step_active : R.drawable.bg_step_track);
    }

    private void retroceder() {
        if (pasoActual > 1) {
            mostrarPaso(pasoActual - 1);
        } else {
            finish();
        }
    }

    private void configurarFormularioSegunRol() {
        boolean fundacion = RolUsuario.FUNDACION.equals(rolSeleccionado);
        contenedorDatosFundacion.setVisibility(fundacion ? View.VISIBLE : View.GONE);
        ((TextView) pasoDatos.findViewById(R.id.txtTituloDatosRegistro)).setText(
                fundacion ? "Datos del representante" : "Tus datos personales"
        );
        ((TextView) pasoDatos.findViewById(R.id.txtSubtituloDatosRegistro)).setText(
                fundacion
                        ? "Primero necesitamos los datos de contacto y luego los de la fundación."
                        : "Usaremos esta información para identificar tu cuenta de donante."
        );
    }

    private void actualizarResumenAcceso() {
        TextView resumen = pasoAcceso.findViewById(R.id.txtResumenRolRegistro);
        if (RolUsuario.FUNDACION.equals(rolSeleccionado)) {
            resumen.setText("Crearás la cuenta de fundación: "
                    + edtNombreFundacion.getText().toString().trim());
        } else {
            resumen.setText("Crearás una cuenta de donante a nombre de "
                    + edtNombre.getText().toString().trim() + ".");
        }
    }

    private boolean validarDatosPersonales() {
        if (texto(edtNombre).isEmpty()) return error(edtNombre, "Ingresa tu nombre");
        if (texto(edtApPaterno).isEmpty()) return error(edtApPaterno, "Ingresa tu apellido paterno");
        if (texto(edtApMaterno).isEmpty()) return error(edtApMaterno, "Ingresa tu apellido materno");
        if (texto(edtTelefono).isEmpty()) return error(edtTelefono, "Ingresa tu teléfono");
        if (texto(edtCiudad).isEmpty()) return error(edtCiudad, "Ingresa tu ciudad");

        if (RolUsuario.FUNDACION.equals(rolSeleccionado)) {
            if (texto(edtNombreFundacion).isEmpty()) {
                return error(edtNombreFundacion, "Ingresa el nombre de la fundación");
            }
            if (texto(edtDescripcionFundacion).isEmpty()) {
                return error(edtDescripcionFundacion, "Describe la labor de la fundación");
            }
            if (texto(edtDireccionFundacion).isEmpty()) {
                return error(edtDireccionFundacion, "Ingresa la dirección de la fundación");
            }
        }
        return true;
    }

    private void registrarUsuarioSupabase() {
        if (rolSeleccionado == null) {
            mostrarPaso(1);
            return;
        }
        if (!validarDatosPersonales()) {
            mostrarPaso(2);
            return;
        }

        String correo = texto(edtCorreo);
        String contrasena = edtRegistroContrasena.getText().toString();
        if (correo.isEmpty()) {
            error(edtCorreo, "Ingresa tu correo");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            error(edtCorreo, "Ingresa un correo válido");
            return;
        }
        if (contrasena.length() < 6) {
            error(edtRegistroContrasena, "Debe tener al menos 6 caracteres");
            return;
        }

        String nombre = texto(edtNombre);
        String paterno = texto(edtApPaterno);
        String materno = texto(edtApMaterno);
        String telefono = texto(edtTelefono);
        String ciudad = texto(edtCiudad);
        String nombreCompleto = (nombre + " " + paterno + " " + materno).trim();
        String nombreFundacion = texto(edtNombreFundacion);

        if (SupabaseConfig.SUPABASE_URL.contains("TU_PROYECTO")) {
            SessionManager session = SessionManager.getInstance(this);
            session.saveSession("token_demo", "demo_user", correo, nombreCompleto, telefono);
            session.updateUserRole(rolSeleccionado);
            session.updateFoundationName(nombreFundacion);
            abrirInicioSegunRol(rolSeleccionado);
            return;
        }

        btnRegistrar.setEnabled(false);
        btnRegistrar.setText("Registrando...");

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("nombre", nombre);
        metadata.put("apellido_paterno", paterno);
        metadata.put("apellido_materno", materno);
        metadata.put("telefono", telefono);
        metadata.put("ciudad", ciudad);
        metadata.put("rol", rolSeleccionado);
        if (RolUsuario.FUNDACION.equals(rolSeleccionado)) {
            metadata.put("nombre_fundacion", nombreFundacion);
            metadata.put("descripcion_fundacion", texto(edtDescripcionFundacion));
            metadata.put("direccion_fundacion", texto(edtDireccionFundacion));
        }

        SignUpRequest request = new SignUpRequest(correo, contrasena, metadata);
        SupabaseClient.getInstance(this).getAuthService().signUp(request)
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        habilitarBoton();
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(RegistroActivity.this,
                                    SupabaseError.mensajeRegistro(response), Toast.LENGTH_LONG).show();
                            return;
                        }

                        AuthResponse auth = response.body();
                        User user = auth.getUser();
                        String userId = user != null ? user.getId() : null;
                        String token = auth.getAccessToken();

                        if (token != null && !token.isEmpty() && userId != null) {
                            SessionManager session = SessionManager.getInstance(RegistroActivity.this);
                            session.saveSession(token, userId, correo, nombreCompleto, telefono);
                            session.saveRefreshToken(auth.getRefreshToken());
                            session.updateUserRole(rolSeleccionado);
                            session.updateFoundationName(nombreFundacion);
                            asegurarPerfilEnBaseDeDatos(userId, correo, nombre, paterno, materno, telefono);
                            Toast.makeText(RegistroActivity.this,
                                    "¡Cuenta creada correctamente!", Toast.LENGTH_SHORT).show();
                            abrirInicioSegunRol(rolSeleccionado);
                        } else {
                            Toast.makeText(RegistroActivity.this,
                                    "Cuenta creada. Revisa tu correo antes de iniciar sesión.",
                                    Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable throwable) {
                        habilitarBoton();
                        String mensaje = throwable instanceof UnknownHostException
                                ? "Sin conexión a internet."
                                : "Error de red: " + throwable.getMessage();
                        Toast.makeText(RegistroActivity.this, mensaje, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void asegurarPerfilEnBaseDeDatos(String userId, String correo, String nombre,
                                               String paterno, String materno, String telefono) {
        Perfil perfil = new Perfil(userId, correo, nombre, paterno, materno, telefono);
        SupabaseClient.getInstance(this).getDbService()
                .upsertProfile("resolution=merge-duplicates", perfil)
                .enqueue(new Callback<Void>() {
                    @Override public void onResponse(Call<Void> call, Response<Void> response) {}
                    @Override public void onFailure(Call<Void> call, Throwable throwable) {}
                });
    }

    private void abrirInicioSegunRol(String rol) {
        Class<?> destino = RolUsuario.FUNDACION.equals(rol)
                ? PanelFundacionActivity.class : HomeActivity.class;
        Intent intent = new Intent(this, destino);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void habilitarBoton() {
        btnRegistrar.setEnabled(true);
        btnRegistrar.setText("Registrarse");
    }

    private String texto(EditText campo) {
        return campo.getText().toString().trim();
    }

    private boolean error(EditText campo, String mensaje) {
        campo.setError(mensaje);
        campo.requestFocus();
        return false;
    }
}
