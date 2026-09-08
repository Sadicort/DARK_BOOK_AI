package darkbook.neural;

import darkbook.events.DarkBookEventBus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmbeddingEngineTest {
    private final EmbeddingEngine engine = new EmbeddingEngine(64, new Tokenizer(), new DarkBookEventBus());

    @Test void createsNormalizedDeterministicVectors() {
        double[] first = engine.embed("Inteligencia artificial y tendencias");
        double[] second = engine.embed("Inteligencia artificial y tendencias");
        assertArrayEquals(first, second);
        assertEquals(64, first.length);
        assertEquals(1.0, Math.sqrt(java.util.Arrays.stream(first).map(value -> value * value).sum()), 0.000001);
    }

    @Test void cosinePrefersIdenticalText() {
        double[] topic = engine.embed("tutorial inteligencia artificial");
        assertEquals(1.0, EmbeddingEngine.cosine(topic, topic), 0.000001);
        assertTrue(EmbeddingEngine.cosine(topic, engine.embed("tutorial de inteligencia artificial")) >
                EmbeddingEngine.cosine(topic, engine.embed("baile y música")));
    }
}

