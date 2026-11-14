package com.prdev.solidaryhands.ai;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Ejecuta inferencia local con el modelo entrenado `assets/donation_classifier.tflite`.
 * Si el archivo aun no fue generado/copiado a assets, `isAvailable()` devuelve false
 * para que `SmartDonationAnalyzer` active el respaldo local gratuito.
 */
public class TfliteDonationClassifier {

    private static final String MODEL_FILE = "donation_classifier.tflite";
    private static final String LABELS_FILE = "labels.txt";
    private static final int INPUT_SIZE = 224;

    private Interpreter interpreter;
    private List<String> labels = new ArrayList<>();

    public TfliteDonationClassifier(Context context) {
        try {
            if (!assetExists(context, MODEL_FILE) || !assetExists(context, LABELS_FILE)) {
                return;
            }
            MappedByteBuffer modelBuffer = loadModelFile(context, MODEL_FILE);
            Interpreter.Options options = new Interpreter.Options();
            options.setNumThreads(4);
            this.interpreter = new Interpreter(modelBuffer, options);
            this.labels = loadLabels(context, LABELS_FILE);
        } catch (Exception ignored) {
            this.interpreter = null;
        }
    }

    public boolean isAvailable() {
        return interpreter != null && !labels.isEmpty();
    }

    public List<PredictionResult.Candidate> classifyTopK(Bitmap bitmap, int topK) {
        List<PredictionResult.Candidate> results = new ArrayList<>();
        if (!isAvailable() || bitmap == null) {
            return results;
        }

        Bitmap resized = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);
        ByteBuffer inputBuffer = convertBitmapToFloatBuffer(resized);

        float[][] output = new float[1][labels.size()];
        interpreter.run(inputBuffer, output);

        float[] probs = output[0];
        Integer[] indices = new Integer[probs.length];
        for (int i = 0; i < probs.length; i++) {
            indices[i] = i;
        }
        Arrays.sort(indices, Comparator.comparingDouble((Integer idx) -> probs[idx]).reversed());

        int count = Math.min(topK, indices.length);
        for (int i = 0; i < count; i++) {
            int idx = indices[i];
            String key = labels.get(idx);
            CatalogConfig.CatalogItem item = CatalogConfig.getByKey(key);
            results.add(new PredictionResult.Candidate(
                    item.key,
                    item.displayName,
                    item.category,
                    probs[idx]
            ));
        }
        return results;
    }

    public void close() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
        }
    }

    private ByteBuffer convertBitmapToFloatBuffer(Bitmap bitmap) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * 3);
        buffer.order(ByteOrder.nativeOrder());
        int[] pixels = new int[INPUT_SIZE * INPUT_SIZE];
        bitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE);

        // MobileNetV3Small en Keras tiene include_preprocessing=True, espera RGB en [0..255]
        for (int pixel : pixels) {
            float r = (pixel >> 16) & 0xFF;
            float g = (pixel >> 8) & 0xFF;
            float b = pixel & 0xFF;
            buffer.putFloat(r);
            buffer.putFloat(g);
            buffer.putFloat(b);
        }
        buffer.rewind();
        return buffer;
    }

    private static boolean assetExists(Context context, String fileName) {
        try {
            String[] list = context.getAssets().list("");
            if (list == null) {
                return false;
            }
            for (String item : list) {
                if (fileName.equals(item)) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    private static MappedByteBuffer loadModelFile(Context context, String modelPath) throws IOException {
        try (AssetFileDescriptor fd = context.getAssets().openFd(modelPath);
             FileInputStream fis = new FileInputStream(fd.getFileDescriptor())) {
            FileChannel channel = fis.getChannel();
            return channel.map(FileChannel.MapMode.READ_ONLY, fd.getStartOffset(), fd.getDeclaredLength());
        }
    }

    private static List<String> loadLabels(Context context, String labelsPath) throws IOException {
        List<String> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(context.getAssets().open(labelsPath)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    list.add(trimmed);
                }
            }
        }
        return list;
    }
}
