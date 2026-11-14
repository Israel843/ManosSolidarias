package com.prdev.solidaryhands.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

import java.io.InputStream;

/**
 * Carga imágenes (cámara o galería) reducidas y con la orientación correcta.
 */
public final class ImagenUtil {

    private ImagenUtil() {}

    public static Bitmap cargarBitmap(Context context, Uri uri, int maxLado) throws Exception {
        // 1) Medir
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(in, null, bounds);
        }
        int sample = 1;
        int lado = Math.max(bounds.outWidth, bounds.outHeight);
        while (lado / (sample * 2) >= maxLado) {
            sample *= 2;
        }

        // 2) Decodificar
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = sample;
        Bitmap bitmap;
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            bitmap = BitmapFactory.decodeStream(in, null, opts);
        }
        if (bitmap == null) {
            throw new IllegalStateException("No se pudo leer la imagen");
        }

        // 3) Orientación EXIF
        int rotacion = 0;
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            if (in != null) {
                ExifInterface exif = new ExifInterface(in);
                int o = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                if (o == ExifInterface.ORIENTATION_ROTATE_90) rotacion = 90;
                else if (o == ExifInterface.ORIENTATION_ROTATE_180) rotacion = 180;
                else if (o == ExifInterface.ORIENTATION_ROTATE_270) rotacion = 270;
            }
        } catch (Exception ignored) {
        }
        if (rotacion != 0) {
            Matrix m = new Matrix();
            m.postRotate(rotacion);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
        }

        // 4) ARGB_8888 garantizado (el modelo lee los pixeles)
        if (bitmap.getConfig() != Bitmap.Config.ARGB_8888) {
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
        }
        return bitmap;
    }
}
