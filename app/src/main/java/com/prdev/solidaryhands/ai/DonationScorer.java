package com.prdev.solidaryhands.ai;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Evalúa una donación usando la red neuronal (TFLite) y le da una puntuación de 0 a 10.
 *
 * Cómo se calcula:
 *  1) Calidad de fotos (35%): nitidez + iluminación de todas las fotos.
 *  2) Puntuación del objeto (65%):
 *       - La red neuronal clasifica cada foto en 12 tipos de objeto.
 *       - Se mide cuánta probabilidad cae en la categoría que eligió el usuario.
 *       - Se combina con la condición declarada (Nuevo, Muy bien, Bueno, Usado).
 *
 * Nota: la red neuronal reconoce QUÉ objeto es (12 clases), no mide el desgaste.
 * Para las categorías que no reconoce (Alimentos, Higiene, Otros) se evalúan
 * solo la calidad de fotos y la condición declarada.
 */
public class DonationScorer {

    public interface Callback {
        void onResult(ScoreResult result);
    }

    private static final float[] PESOS_FOTO = {1f, 1f, 0.5f, 1f};

    private final TfliteDonationClassifier tflite;
    private final MlKitFallbackAnalyzer mlKit;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    public DonationScorer(Context context) {
        this.tflite = new TfliteDonationClassifier(context.getApplicationContext());
        this.mlKit = new MlKitFallbackAnalyzer();
    }

    public void evaluate(List<Bitmap> fotos, String categoriaDeclarada,
                         String condicionDeclarada, Callback callback) {
        executor.execute(() -> {
            ScoreResult result;
            try {
                result = compute(fotos, categoriaDeclarada, condicionDeclarada);
            } catch (Exception e) {
                result = new ScoreResult();
                result.motor = "Error";
                result.detalleCalidad = "No se pudo analizar: " + e.getMessage();
                result.detalleObjeto = "Intenta con otras fotografías.";
            }
            final ScoreResult finalResult = result;
            main.post(() -> callback.onResult(finalResult));
        });
    }

    private ScoreResult compute(List<Bitmap> fotos, String categoriaDeclarada, String condicionDeclarada) {
        long inicio = System.currentTimeMillis();
        ScoreResult r = new ScoreResult();

        // ---------- 1) Calidad de fotos ----------
        float sumaCalidad = 0f;
        float sumaPesos = 0f;
        float minNitidez = Float.MAX_VALUE;
        float sumaBrillo = 0f;
        for (int i = 0; i < fotos.size(); i++) {
            ImageQuality.Result q = ImageQuality.evaluate(fotos.get(i));
            float peso = PESOS_FOTO[Math.min(i, PESOS_FOTO.length - 1)];
            sumaCalidad += q.score * peso;
            sumaPesos += peso;
            sumaBrillo += q.brightness;
            minNitidez = Math.min(minNitidez, q.sharpness);
        }
        r.calidadFotos = sumaPesos > 0 ? sumaCalidad / sumaPesos : 0f;
        float brilloMedio = fotos.isEmpty() ? 0f : sumaBrillo / fotos.size();
        r.detalleCalidad = describirCalidad(r.calidadFotos, minNitidez, brilloMedio);

        // ---------- 2) Red neuronal ----------
        String categoriaRed = mapearCategoria(categoriaDeclarada);
        r.categoriaReconocible = categoriaRed != null;

        Map<String, Float> probPorClase = new LinkedHashMap<>();
        float probCategoriaAcum = 0f;
        float pesoAcum = 0f;
        boolean usoTflite = tflite.isAvailable();
        r.motor = usoTflite ? "Red Neuronal TFLite" : "Respaldo ML Kit";

        for (int i = 0; i < fotos.size(); i++) {
            float peso = PESOS_FOTO[Math.min(i, PESOS_FOTO.length - 1)];
            List<PredictionResult.Candidate> cands = usoTflite
                    ? tflite.classifyTopK(fotos.get(i), 12)
                    : mlKit.classifySync(fotos.get(i));

            float masaCategoria = 0f;
            for (PredictionResult.Candidate c : cands) {
                Float prev = probPorClase.get(c.key);
                probPorClase.put(c.key, (prev == null ? 0f : prev) + c.confidence * peso);
                if (categoriaRed != null && categoriaRed.equals(c.category)) {
                    masaCategoria += c.confidence;
                }
            }
            probCategoriaAcum += masaCategoria * peso;
            pesoAcum += peso;
        }

        // Mejor objeto detectado (promedio ponderado entre fotos)
        String mejorClave = null;
        float mejorProb = 0f;
        for (Map.Entry<String, Float> e : probPorClase.entrySet()) {
            float p = pesoAcum > 0 ? e.getValue() / pesoAcum : 0f;
            if (p > mejorProb) {
                mejorProb = p;
                mejorClave = e.getKey();
            }
        }
        if (mejorClave != null) {
            CatalogConfig.CatalogItem item = CatalogConfig.getByKey(mejorClave);
            r.objetoDetectado = item.displayName;
            r.confianzaObjeto = mejorProb;
            r.coincideCategoria = categoriaRed != null && categoriaRed.equals(item.category);
        }

        // ---------- 3) Puntuación del objeto ----------
        float condicion = puntajeCondicion(condicionDeclarada);
        if (r.categoriaReconocible) {
            float masaMedia = pesoAcum > 0 ? probCategoriaAcum / pesoAcum : 0f;
            float reconocimiento = Math.min(1f, masaMedia / 0.70f) * 10f;
            r.puntuacionObjeto = 0.65f * reconocimiento + 0.35f * condicion;
            r.detalleObjeto = describirObjeto(r, masaMedia, categoriaDeclarada);
        } else {
            r.puntuacionObjeto = condicion * 0.85f;
            r.detalleObjeto = "La red neuronal aún no reconoce la categoría \"" + categoriaDeclarada
                    + "\". Se evaluó la condición que declaraste (" + condicionDeclarada + ").";
        }

        // ---------- 4) Final ----------
        r.puntuacionFinal = clamp10(0.35f * r.calidadFotos + 0.65f * r.puntuacionObjeto);
        r.calidadFotos = clamp10(r.calidadFotos);
        r.puntuacionObjeto = clamp10(r.puntuacionObjeto);

        if (r.puntuacionFinal >= 6f) {
            r.nivel = 2;
        } else if (r.puntuacionFinal >= 4f) {
            r.nivel = 1;
        } else {
            r.nivel = 0;
        }
        r.tiempoMs = System.currentTimeMillis() - inicio;
        return r;
    }

    private static String describirCalidad(float score, float nitidezMin, float brillo) {
        StringBuilder sb = new StringBuilder();
        if (score >= 7.5f) {
            sb.append("Fotografías nítidas y bien iluminadas.");
        } else if (score >= 5f) {
            sb.append("Calidad aceptable.");
        } else {
            sb.append("Calidad baja.");
        }
        if (nitidezMin < 10f) {
            sb.append(" Alguna foto está borrosa: mantén firme el celular.");
        }
        if (brillo < 70f) {
            sb.append(" Hay poca luz, busca un lugar más iluminado.");
        } else if (brillo > 205f) {
            sb.append(" Hay demasiada luz o reflejos.");
        }
        return sb.toString();
    }

    private static String describirObjeto(ScoreResult r, float masaMedia, String categoriaDeclarada) {
        int pct = Math.round(r.confianzaObjeto * 100f);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.getDefault(),
                "La IA detectó: %s (%d%% de confianza). ", r.objetoDetectado, pct));
        if (r.coincideCategoria) {
            sb.append("Coincide con la categoría \"").append(categoriaDeclarada).append("\".");
        } else if (masaMedia >= 0.35f) {
            sb.append("Es parecido a la categoría \"").append(categoriaDeclarada).append("\".");
        } else {
            sb.append("No parece coincidir con la categoría \"").append(categoriaDeclarada)
                    .append("\". Revisa las fotos o la categoría elegida.");
        }
        return sb.toString();
    }

    /** Categorías del formulario -> categorías de la red neuronal. null = la red no la reconoce. */
    public static String mapearCategoria(String categoriaFormulario) {
        if (categoriaFormulario == null) return null;
        switch (categoriaFormulario) {
            case "Ropa y calzado":
                return "Ropa y Calzado";
            case "Muebles":
                return "Hogar / Muebles";
            case "Electrodomésticos":
                return "Electrodomésticos";
            case "Material escolar":
                return "Educación";
            case "Juguetes":
                return "Recreación";
            default:
                return null;
        }
    }

    private static float puntajeCondicion(String condicion) {
        if (condicion == null) return 6f;
        switch (condicion) {
            case "Nuevo":
                return 10f;
            case "Muy bien":
                return 8.5f;
            case "Bueno":
                return 7f;
            case "Usado":
                return 5.5f;
            default:
                return 6f;
        }
    }

    private static float clamp10(float v) {
        return Math.max(0f, Math.min(10f, v));
    }

    public void close() {
        tflite.close();
        executor.shutdown();
    }
}
