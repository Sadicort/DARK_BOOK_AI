package darkbook.neural;

import darkbook.events.DarkBookEventBus;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/** Creates deterministic, dependency-free semantic fingerprints used until a trained embedding model is loaded. */
public final class EmbeddingEngine {
    public static final String VERSION = "1.0";
    private final int dimension;
    private final Tokenizer tokenizer;
    private final DarkBookEventBus events;

    public EmbeddingEngine(int dimension, Tokenizer tokenizer, DarkBookEventBus events) {
        if (dimension < 8 || dimension > 4096) throw new IllegalArgumentException("Invalid embedding dimension");
        this.dimension = dimension;
        this.tokenizer = tokenizer;
        this.events = events;
    }

    public double[] embed(String text) {
        double[] vector = new double[dimension];
        for (String token : tokenizer.tokenize(text)) {
            byte[] hash = sha256(token);
            for (int i = 0; i < dimension; i++) {
                int value = hash[i % hash.length] & 0xff;
                vector[i] += (value / 127.5) - 1.0;
            }
        }
        double norm = Math.sqrt(java.util.Arrays.stream(vector).map(value -> value * value).sum());
        if (norm > 0) for (int i = 0; i < vector.length; i++) vector[i] /= norm;
        return vector;
    }

    public double[] create(String sourceId, String text) {
        double[] value = embed(text);
        events.publish("EmbeddingCreated", "embedding", Map.of("sourceId", sourceId, "dimension", dimension));
        return value;
    }

    public int dimension() { return dimension; }

    public static double cosine(double[] left, double[] right) {
        int length = Math.min(left.length, right.length);
        double dot = 0, leftNorm = 0, rightNorm = 0;
        for (int i = 0; i < length; i++) {
            dot += left[i] * right[i]; leftNorm += left[i] * left[i]; rightNorm += right[i] * right[i];
        }
        return leftNorm == 0 || rightNorm == 0 ? 0 : dot / Math.sqrt(leftNorm * rightNorm);
    }

    private static byte[] sha256(String token) {
        try { return MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)); }
        catch (NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
    }
}

