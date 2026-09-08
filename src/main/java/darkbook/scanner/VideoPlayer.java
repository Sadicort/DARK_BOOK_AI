package darkbook.scanner;

import com.microsoft.playwright.Page;

public class VideoPlayer {

    private final Page page;

    public VideoPlayer(Page page) {
        this.page = page;
    }

    public boolean isPlaying() {
        try {
            return Boolean.TRUE.equals(page.evaluate("""
                    () => {
                        const video = [...document.querySelectorAll('video')]
                            .filter(item => {
                                const rect = item.getBoundingClientRect();
                                return rect.width > 0 && rect.height > 0 &&
                                       rect.bottom > 0 && rect.top < window.innerHeight;
                            })
                            .sort((a, b) => {
                                const center = window.innerHeight / 2;
                                const aRect = a.getBoundingClientRect();
                                const bRect = b.getBoundingClientRect();
                                return Math.abs((aRect.top + aRect.bottom) / 2 - center) -
                                       Math.abs((bRect.top + bRect.bottom) / 2 - center);
                            })[0];

                        return Boolean(video && !video.paused && !video.ended);
                    }
                    """));
        } catch (Exception e) {
            return false;
        }
    }

    public void clickCenter() {
        page.evaluate("""
                () => {
                    const video = [...document.querySelectorAll('video')]
                        .find(item => {
                            const rect = item.getBoundingClientRect();
                            return rect.width > 0 && rect.height > 0 &&
                                   rect.bottom > 0 && rect.top < window.innerHeight;
                        });

                    video?.click();
                }
                """);
        page.waitForTimeout(700);
    }

    public boolean ensurePlaying() {
        try {
            boolean started = Boolean.TRUE.equals(page.evaluate("""
                    async () => {
                        const videos = [...document.querySelectorAll('video')]
                            .filter(item => {
                                const rect = item.getBoundingClientRect();
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
                        if (!video) return false;

                        try {
                            await video.play();
                        } catch (error) {
                            video.muted = true;
                            await video.play();
                        }

                        return !video.paused && !video.ended;
                    }
                    """));

            page.waitForTimeout(1000);

            if (!started && !isPlaying()) {
                clickCenter();
            }

            boolean playing = isPlaying();
            System.out.println(playing
                    ? "Video reproduciéndose."
                    : "No fue posible iniciar el video.");
            return playing;
        } catch (Exception e) {
            System.out.println("No fue posible iniciar el video.");
            return false;
        }
    }
}
