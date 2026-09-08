package darkbook.obsidian;

import darkbook.knowledge.MarkdownWriter;
import darkbook.models.VideoRecord;
import darkbook.utils.AppPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/** Mounts the local vault by mirroring approved Markdown knowledge; it never stores models or embeddings. */
public final class ObsidianService {
    private final AppPaths paths;

    public ObsidianService(AppPaths paths) { this.paths = paths; }

    public void mirrorVideo(VideoRecord video) throws IOException {
        String name = MarkdownWriter.safe(video.id()) + ".md";
        var source = paths.resolve("knowledge/videos/" + name);
        var target = paths.resolve("obsidian/videos/" + name);
        Files.createDirectories(target.getParent());
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }
}
