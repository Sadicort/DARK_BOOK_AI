package darkbook.intelligence;

import darkbook.neural.Tokenizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContentAnalyzerTest {
    private final ContentAnalyzer analyzer = new ContentAnalyzer(new Tokenizer());

    @Test void classifiesAndExtractsHashtags() {
        var result = analyzer.analyze("Tutorial increíble sobre IA #Tech #AI");
        assertEquals("tecnología", result.category());
        assertEquals("positiva", result.emotion());
        assertEquals(java.util.List.of("#tech", "#ai"), result.hashtags());
    }
}

