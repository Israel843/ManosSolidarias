package com.prdev.solidaryhands;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;

/**
 * Escáner / cámara DENTRO de la aplicación (CameraX).
 * Recibe un título (qué foto se está tomando) y devuelve la ruta de la foto tomada.
 */
public class ScannerActivity extends AppCompatActivity {

    public static final String EXTRA_TITULO = "extra_titulo";
    public static final String EXTRA_RUTA_FOTO = "extra_ruta_foto";

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private Button btnCapturar;

    private final ActivityResultLauncher<String> permisoCamara =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), concedido -> {
                if (concedido) {
                    iniciarCamara();
                } else {
                    Toast.makeText(this,
                            "Necesitamos el permiso de cámara para escanear el objeto.",
                            Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        previewView = findViewById(R.id.previewView);
        btnCapturar = findViewById(R.id.btnCapturar);
        TextView txtTitulo = findViewById(R.id.txtTituloScanner);

        String titulo = getIntent().getStringExtra(EXTRA_TITULO);
        if (titulo != null && !titulo.isEmpty()) {
            txtTitulo.setText(titulo);
        }

        findViewById(R.id.btnCerrarScanner).setOnClickListener(v -> finish());
        btnCapturar.setOnClickListener(v -> tomarFoto());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            iniciarCamara();
        } else {
            permisoCamara.launch(Manifest.permission.CAMERA);
        }
    }

    private void iniciarCamara() {
        ListenableFuture<ProcessCameraProvider> futuro = ProcessCameraProvider.getInstance(this);
        futuro.addListener(() -> {
            try {
                ProcessCameraProvider proveedor = futuro.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .build();

                proveedor.unbindAll();
                proveedor.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture);
                btnCapturar.setEnabled(true);
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "No se pudo iniciar la cámara.", Toast.LENGTH_LONG).show();
                finish();
            } catch (Exception e) {
                Toast.makeText(this, "Error de cámara: " + e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void tomarFoto() {
        if (imageCapture == null) return;
        btnCapturar.setEnabled(false);

        File carpeta = new File(getCacheDir(), "escaner");
        if (!carpeta.exists()) {
            //noinspection ResultOfMethodCallIgnored
            carpeta.mkdirs();
        }
        final File archivo = new File(carpeta, "foto_" + System.currentTimeMillis() + ".jpg");

        ImageCapture.OutputFileOptions opciones =
                new ImageCapture.OutputFileOptions.Builder(archivo).build();

        imageCapture.takePicture(opciones, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults resultado) {
                        Intent datos = new Intent();
                        datos.putExtra(EXTRA_RUTA_FOTO, archivo.getAbsolutePath());
                        setResult(RESULT_OK, datos);
                        finish();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException e) {
                        btnCapturar.setEnabled(true);
                        Toast.makeText(ScannerActivity.this,
                                "No se pudo tomar la foto. Intenta de nuevo.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
