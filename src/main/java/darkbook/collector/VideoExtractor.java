package darkbook.collector;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import darkbook.models.VideoData;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VideoExtractor {

    private static final Pattern VIDEO_ID_PATTERN = Pattern.compile("/video/(\\d+)");

    private final Page page;

    public VideoExtractor(Page page) {
        this.page = page;
    }

    public VideoData extract() {
        VideoData video = new VideoData();
        String videoUrl = readActiveVideoUrl();

        video.setVideoUrl(videoUrl);
        video.setId(extractVideoId(videoUrl));
        video.setUsername(readText(
                "[data-e2e='browse-username']",
                "[data-e2e='video-author-uniqueid']"
        ));
        video.setDisplayName(readText(
                "[data-e2e='browser-nickname']",
                "[data-e2e='video-author-nickname']"
        ));
        video.setDescription(readText(
                "[data-e2e='browse-video-desc']",
                "[data-e2e='video-desc']"
        ));
        video.setAudioName(readText(
                "[data-e2e='browse-music']",
                "[data-e2e='video-music']"
        ));

        return video;
    }

    private String readActiveVideoUrl() {
        Object url = page.evaluate("""
                () => {
                    const videos = [...document.querySelectorAll('video')]
                        .filter(video => {
                            const rect = video.getBoundingClientRect();
                            return rect.width > 0 && rect.height > 0 &&
                                   rect.bottom > 0 && rect.top < window.innerHeight;
                        })
                        .sort((a, b) => {
                            const center = window.innerHeight / 2;
                            const aRect = a.getBoundingClientRect();
                            const bRect = b.getBoundingClientRect();
                            return Math.abs((aRect.top + aRect.bottom) / 2 - center) -
                                   Math.abs((bRect.top + bRect.bottom) / 2 - center);
                        });

                    const video = videos[0];
                    if (!video) return '';

                    const container = video.closest('[data-e2e="recommend-list-item-container"]');
                    const link = container?.querySelector('a[href*="/video/"]');

                    return link?.href || video.currentSrc || video.src || video.poster || '';
                }
                """);

        String activeUrl = url == null ? "" : url.toString();
        return activeUrl.isBlank() ? page.url() : activeUrl;
    }

    private String extractVideoId(String videoUrl) {
        Matcher matcher = VIDEO_ID_PATTERN.matcher(videoUrl);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String readText(String... selectors) {
        for (String selector : selectors) {
            try {
                Locator element = page.locator(selector).first();

                if (element.count() > 0) {
                    String text = element.textContent();

                    if (text != null && !text.isBlank()) {
                        return text.trim();
                    }
                }
            } catch (RuntimeException ignored) {
                // Prueba el siguiente selector cuando el elemento no está disponible.
            }
        }

        return null;
    }
}
