package darkbook.intelligence;

import com.microsoft.playwright.Page;

public class ScrollStrategy {

    private static final String NEXT_BUTTON = "[data-e2e='feed-navigation-next']";

    private final Page page;
    private final HumanBehavior human;

    public ScrollStrategy(Page page){

        this.page = page;
        this.human = new HumanBehavior(page);

    }

    public boolean scrollNextVideo(){
        String previousVideo = currentVideoFingerprint();

        human.randomMouseMovement();
        human.randomPause();

        if (clickNextButton() && waitForVideoChange(previousVideo, 3000)) {
            return true;
        }

        page.keyboard().press("ArrowDown");

        if (waitForVideoChange(previousVideo, 2500)) {
            return true;
        }

        page.mouse().wheel(0, human.randomScrollAmount());

        if (waitForVideoChange(previousVideo, 2500)) {
            return true;
        }

        boolean moved = Boolean.TRUE.equals(page.evaluate("""
                () => {
                    const videos = [...document.querySelectorAll('video')];
                    if (videos.length < 2) return false;

                    const center = window.innerHeight / 2;
                    const currentIndex = videos
                        .map((video, index) => ({
                            index,
                            distance: Math.abs(
                                (video.getBoundingClientRect().top +
                                 video.getBoundingClientRect().bottom) / 2 - center
                            )
                        }))
                        .sort((a, b) => a.distance - b.distance)[0].index;

                    const next = videos[currentIndex + 1];
                    if (!next) return false;

                    next.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    return true;
                }
                """));

        page.waitForTimeout(1500);
        return moved;
    }

    private boolean clickNextButton() {
        try {
            var button = page.locator(NEXT_BUTTON).first();

            if (button.count() > 0 && button.isVisible()) {
                button.click();
                return true;
            }
        } catch (RuntimeException ignored) {
            // Continúa con teclado y rueda si el control de TikTok no responde.
        }

        return false;
    }

    private boolean waitForVideoChange(String previousVideo, int timeoutMillis) {
        int elapsed = 0;

        while (elapsed < timeoutMillis) {
            page.waitForTimeout(250);
            elapsed += 250;

            String currentVideo = currentVideoFingerprint();
            if (!currentVideo.isBlank() && !currentVideo.equals(previousVideo)) {
                return true;
            }
        }

        return false;
    }

    private String currentVideoFingerprint() {
        Object fingerprint = page.evaluate("""
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
                            const aDistance = Math.abs((aRect.top + aRect.bottom) / 2 - center);
                            const bDistance = Math.abs((bRect.top + bRect.bottom) / 2 - center);
                            return aDistance - bDistance;
                        });

                    const video = videos[0];
                    if (!video) return '';

                    const container = video.closest('[data-e2e="recommend-list-item-container"]');
                    const link = container?.querySelector('a[href*="/video/"]');
                    const author = container?.querySelector('[data-e2e="video-author-uniqueid"]');

                    return [
                        link?.href || video.currentSrc || video.src || video.poster || '',
                        author?.textContent?.trim() || ''
                    ].join('|');
                }
                """);

        return fingerprint == null ? "" : fingerprint.toString();
    }

}
