package com.prdev.solidaryhands.ai;

import java.util.ArrayList;
import java.util.List;

public class PredictionResult {

    public static class Candidate {
        public final String key;
        public final String label;
        public final String category;
        public final float confidence;

        public Candidate(String key, String label, String category, float confidence) {
            this.key = key;
            this.label = label;
            this.category = category;
            this.confidence = confidence;
        }
    }

    public final List<Candidate> topCandidates = new ArrayList<>();
    public String dominantColor = "Negro";
    public String detectedSize = "";
    public String detectedOcrHint = "";
    public String engineUsed = "";
    public long inferenceTimeMs;

    public Candidate getBestCandidate() {
        if (topCandidates.isEmpty()) {
            CatalogConfig.CatalogItem fallback = CatalogConfig.getByKey("01_polera");
            return new Candidate(fallback.key, fallback.displayName, fallback.category, 0f);
        }
        return topCandidates.get(0);
    }
}
