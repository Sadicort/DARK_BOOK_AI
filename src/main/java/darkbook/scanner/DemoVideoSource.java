package darkbook.scanner;

import darkbook.intelligence.ContentAnalyzer;
import darkbook.models.VideoRecord;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/** Offline deterministic source used to validate the complete pipeline without a social-network account. */
public final class DemoVideoSource implements VideoSource {
    private final AtomicLong counter = new AtomicLong();
    private final ContentAnalyzer analyzer;
    private static final List<String> EXAMPLES = List.of(
            "Tutorial de IA: aprende tres ideas para crear mejores videos #ai #tecnologia",
            "Nuevo baile con música increíble que todos están usando #dance #viral",
            "Dato de historia que probablemente no conocías #aprende #historia",
            "Este meme me dio mucha risa #humor #meme",
            "Outfit de hoy y consejos de moda #fashion #estilo");

    public DemoVideoSource(ContentAnalyzer analyzer) { this.analyzer = analyzer; }

    @Override public Optional<VideoRecord> collectNext() {
        long number = counter.incrementAndGet();
        String text = EXAMPLES.get((int) ((number - 1) % EXAMPLES.size()));
        var analysis = analyzer.analyze(text);
        return Optional.of(new VideoRecord("demo-" + Instant.now().toEpochMilli() + "-" + number, "demo", "offline://demo/" + number,
                text, "darkbook_demo", 120L * number, 11L * number, 4L * number,
                analysis.category(), analysis.emotion(), analysis.hashtags(), "audio-demo-" + (number % 3), Instant.now()));
    }

    @Override public String name() { return "Offline Demo"; }
}

