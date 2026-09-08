package darkbook.dashboard;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import darkbook.models.VideoRecord;
import darkbook.services.DarkBookRuntime;
import darkbook.utils.Json;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/** Loopback-only HTTP boundary between Java services and Dark Book OS; no SQL is exposed. */
public final class LocalApiServer implements AutoCloseable {
    private static final int MAX_REQUEST_BYTES = 1_048_576;
    private final DarkBookRuntime runtime;
    private HttpServer server;

    public LocalApiServer(DarkBookRuntime runtime) { this.runtime = runtime; }

    public int start(String host, int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(host, port), 0);
        server.createContext("/", this::handle);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor()); server.start();
        runtime.events().publish("DashboardApiStarted", "dashboard", Map.of("port", server.getAddress().getPort()));
        return server.getAddress().getPort();
    }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            if (!path.startsWith("/api/")) { serveStatic(exchange, path); return; }
            route(exchange, path);
        } catch (IllegalArgumentException exception) {
            json(exchange, 400, ApiEnvelope.error(exception.getMessage()));
        } catch (Exception exception) {
            runtime.events().publish("ErrorDetected", "dashboard", Map.of("message", message(exception)));
            json(exchange, 500, ApiEnvelope.error(message(exception)));
        } finally { exchange.close(); }
    }

    private void route(HttpExchange exchange, String path) throws Exception {
        String method = exchange.getRequestMethod();
        if (method.equals("OPTIONS")) { empty(exchange, 204); return; }
        if (method.equals("GET") && path.equals("/api/status")) {
            json(exchange, 200, ApiEnvelope.success("Dark Book OS online", runtime.modules())); return;
        }
        if (method.equals("GET") && path.equals("/api/dashboard")) {
            json(exchange, 200, ApiEnvelope.success("Dashboard snapshot", runtime.dashboard())); return;
        }
        if (method.equals("GET") && path.equals("/api/events")) {
            json(exchange, 200, ApiEnvelope.success("Recent events", runtime.events().recent(queryInt(exchange, "limit", 100)))); return;
        }
        if (method.equals("POST") && path.equals("/api/frontend/log")) {
            JsonNode log = body(exchange);
            String level = log.path("level").asText("info").substring(0, Math.min(20, log.path("level").asText("info").length()));
            String message = log.path("message").asText("Frontend event");
            String source = log.path("source").asText("web");
            runtime.events().publish("error".equalsIgnoreCase(level) ? "FrontendError" : "FrontendLog", "frontend",
                    Map.of("level", level, "message", message.substring(0, Math.min(2000, message.length())),
                            "source", source.substring(0, Math.min(300, source.length()))));
            json(exchange, 202, ApiEnvelope.success("Frontend log accepted", Map.of())); return;
        }
        if (method.equals("GET") && path.equals("/api/videos")) {
            json(exchange, 200, ApiEnvelope.success("Videos", runtime.videos().latest(queryInt(exchange, "limit", 100)))); return;
        }
        if (method.equals("GET") && path.equals("/api/scanner")) {
            json(exchange, 200, ApiEnvelope.success("Scanner state", runtime.scanner().status())); return;
        }
        if (method.equals("POST") && path.startsWith("/api/scanner/")) {
            switch (path.substring("/api/scanner/".length())) {
                case "start" -> runtime.startScanner(); case "pause" -> runtime.scanner().pause();
                case "resume" -> runtime.scanner().resume(); case "stop" -> runtime.scanner().stop();
                default -> throw new IllegalArgumentException("Unknown scanner action");
            }
            json(exchange, 200, ApiEnvelope.success("Scanner updated", runtime.scanner().status())); return;
        }
        if (method.equals("GET") && path.equals("/api/memory/search")) {
            String query = query(exchange, "q", "");
            json(exchange, 200, ApiEnvelope.success("Semantic memories", runtime.memories().search(query, queryInt(exchange, "limit", 10)))); return;
        }
        if (method.equals("GET") && path.equals("/api/graph")) {
            json(exchange, 200, ApiEnvelope.success("Knowledge graph", runtime.graph().snapshot(queryInt(exchange, "limit", 250)))); return;
        }
        if (method.equals("GET") && path.equals("/api/datasets")) {
            json(exchange, 200, ApiEnvelope.success("Datasets", runtime.datasets())); return;
        }
        if (method.equals("GET") && path.equals("/api/models")) {
            json(exchange, 200, ApiEnvelope.success("Models", runtime.models())); return;
        }
        if (method.equals("GET") && path.equals("/api/plugins")) {
            json(exchange, 200, ApiEnvelope.success("Discovered plugins", runtime.plugins())); return;
        }
        if (method.equals("POST") && path.equals("/api/training/start")) {
            runtime.trainer().start(); json(exchange, 202, ApiEnvelope.success("Training started", runtime.trainer().status())); return;
        }
        if (method.equals("POST") && path.equals("/api/training/cancel")) {
            runtime.trainer().cancel(); json(exchange, 200, ApiEnvelope.success("Training cancelled", runtime.trainer().status())); return;
        }
        if (method.equals("GET") && path.equals("/api/training")) {
            json(exchange, 200, ApiEnvelope.success("Training state", runtime.trainer().status())); return;
        }
        if (method.equals("POST") && path.equals("/api/reasoning")) {
            String question = body(exchange).path("question").asText("").trim();
            if (question.isEmpty()) throw new IllegalArgumentException("question is required");
            json(exchange, 200, ApiEnvelope.success("Reasoning result", runtime.reasoning().reason(question))); return;
        }
        if (method.equals("POST") && path.equals("/api/inference")) {
            String text = body(exchange).path("text").asText("").trim();
            if (text.isEmpty()) throw new IllegalArgumentException("text is required");
            json(exchange, 200, ApiEnvelope.success("Inference result", runtime.inference().predict(text))); return;
        }
        if (method.equals("GET") && path.equals("/api/settings")) {
            json(exchange, 200, ApiEnvelope.success("Settings", runtime.configuration().all())); return;
        }
        if (method.equals("PUT") && path.startsWith("/api/settings/")) {
            String file = path.substring("/api/settings/".length());
            if (!file.endsWith(".json")) file += ".json";
            runtime.configuration().update(file, body(exchange));
            json(exchange, 200, ApiEnvelope.success("Settings saved", runtime.configuration().read(file))); return;
        }
        if (method.equals("POST") && path.equals("/api/ingest")) {
            JsonNode node = body(exchange);
            String id = node.path("id").asText("manual-" + System.currentTimeMillis());
            VideoRecord video = new VideoRecord(id, node.path("platform").asText("manual"), node.path("url").asText("manual://" + id),
                    node.path("description").asText(""), node.path("author").asText("unknown"), node.path("likes").asLong(),
                    node.path("comments").asLong(), node.path("shares").asLong(), node.path("category").asText("general"),
                    node.path("emotion").asText("neutral"), Json.MAPPER.convertValue(node.path("hashtags"), Json.MAPPER.getTypeFactory().constructCollectionType(List.class, String.class)),
                    node.path("audio").asText(""), Instant.now());
            runtime.ingest(video);
            json(exchange, 201, ApiEnvelope.success("Video ingested", video)); return;
        }
        json(exchange, 404, ApiEnvelope.error("Endpoint not found"));
    }

    private void serveStatic(HttpExchange exchange, String path) throws IOException {
        if (!exchange.getRequestMethod().equals("GET") && !exchange.getRequestMethod().equals("HEAD")) { empty(exchange, 405); return; }
        String relative = path.equals("/") ? "index.html" : path.substring(1);
        if (relative.contains("..")) { empty(exchange, 400); return; }
        byte[] bytes;
        var file = runtime.paths().web(relative);
        if (Files.isRegularFile(file)) bytes = Files.readAllBytes(file);
        else try (var resource = LocalApiServer.class.getResourceAsStream("/web/" + relative)) {
            if (resource == null) { empty(exchange, 404); return; }
            bytes = resource.readAllBytes();
        }
        exchange.getResponseHeaders().set("Content-Type", contentType(relative));
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        if (exchange.getRequestMethod().equals("HEAD")) { exchange.sendResponseHeaders(200, -1); return; }
        exchange.sendResponseHeaders(200, bytes.length); exchange.getResponseBody().write(bytes);
    }

    private static JsonNode body(HttpExchange exchange) throws IOException {
        byte[] bytes = exchange.getRequestBody().readNBytes(MAX_REQUEST_BYTES + 1);
        if (bytes.length > MAX_REQUEST_BYTES) throw new IllegalArgumentException("Request body too large");
        return Json.parse(new String(bytes, StandardCharsets.UTF_8));
    }

    private static int queryInt(HttpExchange exchange, String key, int fallback) {
        try { return Integer.parseInt(query(exchange, key, Integer.toString(fallback))); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    private static String query(HttpExchange exchange, String key, String fallback) {
        String raw = exchange.getRequestURI().getRawQuery(); if (raw == null) return fallback;
        for (String pair : raw.split("&")) {
            String[] parts = pair.split("=", 2);
            if (java.net.URLDecoder.decode(parts[0], StandardCharsets.UTF_8).equals(key))
                return parts.length == 1 ? "" : java.net.URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
        }
        return fallback;
    }

    private static void json(HttpExchange exchange, int status, ApiEnvelope envelope) throws IOException {
        byte[] body = Json.MAPPER.writeValueAsBytes(envelope);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.getResponseHeaders().set("Content-Security-Policy", "default-src 'self'; style-src 'self' https://fonts.googleapis.com; font-src https://fonts.gstatic.com; script-src 'self'");
        exchange.sendResponseHeaders(status, body.length); exchange.getResponseBody().write(body);
    }

    private static void empty(HttpExchange exchange, int status) throws IOException { exchange.sendResponseHeaders(status, -1); }
    private static String message(Exception exception) { return exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage(); }
    private static String contentType(String path) {
        if (path.endsWith(".css")) return "text/css; charset=utf-8"; if (path.endsWith(".js")) return "text/javascript; charset=utf-8";
        if (path.endsWith(".svg")) return "image/svg+xml"; if (path.endsWith(".json")) return "application/json; charset=utf-8";
        if (path.endsWith(".png")) return "image/png"; return "text/html; charset=utf-8";
    }

    public int port() { return server == null ? -1 : server.getAddress().getPort(); }
    @Override public void close() { if (server != null) server.stop(1); }
}
