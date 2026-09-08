package darkbook.dashboard;

import java.time.Instant;

/** Uniform response contract consumed by every Dark Book OS component. */
public record ApiEnvelope(String status, String message, Object data, Instant timestamp) {
    public static ApiEnvelope success(String message, Object data) { return new ApiEnvelope("success", message, data, Instant.now()); }
    public static ApiEnvelope error(String message) { return new ApiEnvelope("error", message, null, Instant.now()); }
}

