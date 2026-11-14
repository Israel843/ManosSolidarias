package com.prdev.solidaryhands.ai;

import android.graphics.Bitmap;

import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Clasificador local gratuito de respaldo usando ML Kit On-Device Image Labeling.
 * Traduce etiquetas generales de ML Kit a las 12 clases del catalogo de donaciones.
 */
public class MlKitFallbackAnalyzer {

    private static final Map<String, String> KEYWORD_MAP = new LinkedHashMap<>();

    static {
        KEYWORD_MAP.put("t-shirt", "01_polera");
        KEYWORD_MAP.put("jersey", "01_polera");
        KEYWORD_MAP.put("top", "01_polera");
        KEYWORD_MAP.put("polo", "01_polera");
        KEYWORD_MAP.put("shirt", "02_camisa");
        KEYWORD_MAP.put("collar", "02_camisa");
        KEYWORD_MAP.put("sweater", "03_poleron_chompa");
        KEYWORD_MAP.put("hoodie", "03_poleron_chompa");
        KEYWORD_MAP.put("jacket", "03_poleron_chompa");
        KEYWORD_MAP.put("coat", "03_poleron_chompa");
        KEYWORD_MAP.put("outerwear", "03_poleron_chompa");
        KEYWORD_MAP.put("shorts", "04_short");
        KEYWORD_MAP.put("jeans", "05_pantalon");
        KEYWORD_MAP.put("trousers", "05_pantalon");
        KEYWORD_MAP.put("pants", "05_pantalon");
        KEYWORD_MAP.put("denim", "05_pantalon");
        KEYWORD_MAP.put("shoe", "06_zapatillas");
        KEYWORD_MAP.put("sneakers", "06_zapatillas");
        KEYWORD_MAP.put("footwear", "06_zapatillas");
        KEYWORD_MAP.put("blender", "07_licuadora");
        KEYWORD_MAP.put("kitchen appliance", "07_licuadora");
        KEYWORD_MAP.put("home appliance", "08_microondas");
        KEYWORD_MAP.put("microwave", "08_microondas");
        KEYWORD_MAP.put("oven", "08_microondas");
        KEYWORD_MAP.put("iron", "09_plancha");
        KEYWORD_MAP.put("chair", "10_silla");
        KEYWORD_MAP.put("furniture", "10_silla");
        KEYWORD_MAP.put("stool", "10_silla");
        KEYWORD_MAP.put("backpack", "11_mochila");
        KEYWORD_MAP.put("bag", "11_mochila");
        KEYWORD_MAP.put("luggage", "11_mochila");
        KEYWORD_MAP.put("toy", "12_juguete");
        KEYWORD_MAP.put("plush", "12_juguete");
        KEYWORD_MAP.put("teddy", "12_juguete");
        KEYWORD_MAP.put("doll", "12_juguete");
    }

    public List<PredictionResult.Candidate> classifySync(Bitmap bitmap) {
        Map<String, Float> scores = new LinkedHashMap<>();
        try {
            ImageLabeler labeler = ImageLabeling.getClient(
                    new ImageLabelerOptions.Builder().setConfidenceThreshold(0.45f).build()
            );
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            List<ImageLabel> labels = Tasks.await(labeler.process(image));
            labeler.close();

            for (ImageLabel label : labels) {
                String text = label.getText().toLowerCase(Locale.ROOT);
                for (Map.Entry<String, String> entry : KEYWORD_MAP.entrySet()) {
                    if (text.contains(entry.getKey())) {
                        String classKey = entry.getValue();
                        float prev = scores.containsKey(classKey) ? scores.get(classKey) : 0f;
                        scores.put(classKey, Math.max(prev, label.getConfidence()));
                    }
                }
            }
        } catch (Exception ignored) {
        }

        List<Map.Entry<String, Float>> sorted = new ArrayList<>(scores.entrySet());
        sorted.sort((a, b) -> Float.compare(b.getValue(), a.getValue()));

        List<PredictionResult.Candidate> candidates = new ArrayList<>();
        for (int i = 0; i < Math.min(3, sorted.size()); i++) {
            CatalogConfig.CatalogItem item = CatalogConfig.getByKey(sorted.get(i).getKey());
            candidates.add(new PredictionResult.Candidate(
                    item.key, item.displayName, item.category, sorted.get(i).getValue()
            ));
        }

        // Completar hasta 3 opciones rapidas para el usuario si ML Kit devolvio menos
        List<CatalogConfig.CatalogItem> all = CatalogConfig.getAllItems();
        for (CatalogConfig.CatalogItem item : all) {
            if (candidates.size() >= 3) {
                break;
            }
            boolean exists = false;
            for (PredictionResult.Candidate c : candidates) {
                if (c.key.equals(item.key)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                candidates.add(new PredictionResult.Candidate(
                        item.key, item.displayName, item.category, 0.15f
                ));
            }
        }
        return candidates;
    }
}
