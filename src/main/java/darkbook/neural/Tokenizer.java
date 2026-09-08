package darkbook.neural;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** Normalizes Unicode text and emits stable lowercase word tokens for datasets and embeddings. */
public final class Tokenizer {
    public List<String> tokenize(String text) {
        if (text == null || text.isBlank()) return List.of();
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}#@]+", " ").trim();
        return normalized.isEmpty() ? List.of() : Arrays.stream(normalized.split("\\s+"))
                .filter(token -> token.length() > 1).toList();
    }
}

