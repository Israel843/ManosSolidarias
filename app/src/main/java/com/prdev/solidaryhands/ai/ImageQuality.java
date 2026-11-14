package com.prdev.solidaryhands.ai;

import android.graphics.Bitmap;

/**
 * Mide la calidad de una foto de forma local (sin internet):
 *  - Nitidez: varianza del Laplaciano (si la foto está borrosa, la varianza es baja).
 *  - Iluminación: brillo promedio (ni muy oscura ni quemada).
 */
public final class ImageQuality {

    public static class Result {
        public float score;       // 0..10
        public float sharpness;   // varianza del laplaciano
        public float brightness;  // 0..255
    }

    private static final int MAX_SIDE = 480;

    private ImageQuality() {}

    public static Result evaluate(Bitmap source) {
        Result r = new Result();
        if (source == null) {
            return r;
        }
        float scale = Math.min(1f, (float) MAX_SIDE / Math.max(source.getWidth(), source.getHeight()));
        int w = Math.max(3, Math.round(source.getWidth() * scale));
        int h = Math.max(3, Math.round(source.getHeight() * scale));
        Bitmap bmp = Bitmap.createScaledBitmap(source, w, h, true);

        int[] px = new int[w * h];
        bmp.getPixels(px, 0, w, 0, 0, w, h);
        float[] gray = new float[w * h];
        double sum = 0;
        for (int i = 0; i < px.length; i++) {
            int p = px[i];
            float g = 0.299f * ((p >> 16) & 0xFF) + 0.587f * ((p >> 8) & 0xFF) + 0.114f * (p & 0xFF);
            gray[i] = g;
            sum += g;
        }
        r.brightness = (float) (sum / px.length);

        // Laplaciano 3x3: 4*c - (arriba + abajo + izquierda + derecha)
        double lapSum = 0;
        double lapSqSum = 0;
        int count = 0;
        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                int i = y * w + x;
                float lap = 4f * gray[i] - gray[i - 1] - gray[i + 1] - gray[i - w] - gray[i + w];
                lapSum += lap;
                lapSqSum += lap * lap;
                count++;
            }
        }
        double mean = lapSum / Math.max(1, count);
        r.sharpness = (float) (lapSqSum / Math.max(1, count) - mean * mean);

        // 0 = muy borrosa, 1 = nítida (50+ de varianza; calibrado con fotos nítidas vs. desenfocadas)
        float sharpScore = clamp((r.sharpness - 3f) / 47f, 0f, 1f);
        // 1 cuando el brillo ronda 140, baja hacia 0 en los extremos
        float lightScore = clamp(1f - Math.abs(r.brightness - 140f) / 110f, 0f, 1f);

        r.score = 10f * (0.6f * sharpScore + 0.4f * lightScore);
        return r;
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}
