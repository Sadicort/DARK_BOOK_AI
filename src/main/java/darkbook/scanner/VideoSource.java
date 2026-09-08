package darkbook.scanner;

import darkbook.models.VideoRecord;

import java.util.Optional;

/** Pluggable source boundary that allows TikTok, YouTube and future platforms without coupling the scanner. */
public interface VideoSource extends AutoCloseable {
    Optional<VideoRecord> collectNext() throws Exception;
    String name();
    @Override default void close() throws Exception { }
}

