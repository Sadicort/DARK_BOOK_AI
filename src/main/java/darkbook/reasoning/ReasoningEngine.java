package darkbook.reasoning;

import darkbook.memory.MemoryRecord;
import darkbook.memory.MemoryRepository;

import java.sql.SQLException;
import java.util.List;

/** Answers with retrieved evidence and explicit uncertainty; it does not fabricate facts outside Dark Book memory. */
public final class ReasoningEngine {
    public static final String VERSION = "1.0";
    private final MemoryRepository memories;

    public ReasoningEngine(MemoryRepository memories) { this.memories = memories; }

    public ReasoningResult reason(String question) throws SQLException {
        List<MemoryRecord> evidence = memories.search(question, 5).stream().filter(item -> item.similarity() > 0.05).toList();
        if (evidence.isEmpty()) return new ReasoningResult("No existe evidencia suficiente en la memoria de Dark Book.", 0, List.of());
        String answer = "La memoria relacionada indica: " + evidence.stream().limit(3)
                .map(MemoryRecord::content).reduce((left, right) -> left + " · " + right).orElse("");
        double confidence = evidence.stream().mapToDouble(MemoryRecord::similarity).average().orElse(0);
        return new ReasoningResult(answer, confidence, evidence);
    }

    public record ReasoningResult(String answer, double confidence, List<MemoryRecord> evidence) { }
}

