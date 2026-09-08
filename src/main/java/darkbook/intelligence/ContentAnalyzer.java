package darkbook.intelligence;

import darkbook.neural.Tokenizer;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/** Applies transparent baseline topic and emotion rules before trained classifiers are available. */
public final class ContentAnalyzer {
    private final Tokenizer tokenizer;
    private static final Map<String, List<String>> TOPICS = topics();

    public ContentAnalyzer(Tokenizer tokenizer) { this.tokenizer = tokenizer; }

    public Analysis analyze(String text) {
        List<String> tokens = tokenizer.tokenize(text);
        List<String> comparableTokens = tokens.stream().map(token -> token.startsWith("#") ? token.substring(1) : token).toList();
        String category = TOPICS.entrySet().stream()
                .max(java.util.Comparator.comparingLong(entry -> entry.getValue().stream().filter(comparableTokens::contains).count()))
                .filter(entry -> entry.getValue().stream().anyMatch(comparableTokens::contains)).map(Map.Entry::getKey).orElse("general");
        String emotion = tokens.stream().anyMatch(List.of("wow", "increíble", "amor", "feliz", "genial")::contains) ? "positiva"
                : tokens.stream().anyMatch(List.of("triste", "enojo", "odio", "mal", "miedo")::contains) ? "negativa" : "neutral";
        List<String> hashtags = tokens.stream().filter(token -> token.startsWith("#")).distinct().toList();
        return new Analysis(category, emotion, hashtags);
    }

    private static Map<String, List<String>> topics() {
        Map<String, List<String>> topics = new LinkedHashMap<>();
        topics.put("tecnología", List.of("ia", "ai", "tech", "robot", "software", "app"));
        topics.put("música", List.of("music", "música", "song", "baile", "dance", "audio"));
        topics.put("humor", List.of("meme", "funny", "humor", "risa", "comedia"));
        topics.put("educación", List.of("aprende", "tutorial", "tips", "datos", "historia"));
        topics.put("estilo", List.of("fashion", "moda", "outfit", "beauty", "maquillaje"));
        return java.util.Collections.unmodifiableMap(topics);
    }

    public record Analysis(String category, String emotion, List<String> hashtags) { }
}
