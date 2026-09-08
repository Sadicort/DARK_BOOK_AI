package darkbook.knowledge;

import darkbook.models.VideoRecord;
import darkbook.utils.AppPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Writes safe UTF-8 Markdown knowledge notes to the structured knowledge store. */
public final class MarkdownWriter {
    private final AppPaths paths;

    public MarkdownWriter(AppPaths paths) { this.paths = paths; }

    public void writeVideo(VideoRecord video) throws IOException {
        String safeId = safe(video.id());
        String markdown = """
                ---
                id: "%s"
                platform: "%s"
                author: "%s"
                category: "%s"
                emotion: "%s"
                collected: "%s"
                ---

                # Video %s

                %s

                ## Métricas

                - Likes: %d
                - Comentarios: %d
                - Compartidos: %d
                - Audio: %s

                ## Relaciones

                %s

                [Abrir fuente](%s)
                """.formatted(escape(video.id()), escape(video.platform()), escape(video.author()),
                escape(video.category()), escape(video.emotion()), video.collectedAt(), escape(video.id()),
                escape(video.description()), video.likes(), video.comments(), video.shares(), escape(video.audio()),
                video.hashtags().stream().map(tag -> "- [[" + escape(tag) + "]]" ).reduce("", (a, b) -> a + b + "\n"),
                video.url());
        write(paths.resolve("knowledge/videos/" + safeId + ".md"), markdown);
    }

    private static void write(Path target, String content) throws IOException {
        Files.createDirectories(target.getParent()); Files.writeString(target, content);
    }

    public static String safe(String value) { return value.replaceAll("[^a-zA-Z0-9._-]", "_"); }
    private static String escape(String value) { return value == null ? "" : value.replace("\"", "'").replace("\r", " "); }
}
