package darkbook.intelligence;

import com.microsoft.playwright.Page;

public class VideoDetector {

    private final Page page;

    private String lastVideoUrl = "";

    public VideoDetector(Page page){
        this.page = page;
    }

    public boolean isNewVideo(){

        String currentUrl = getCurrentVideoKey();

        if (currentUrl.isBlank()) {
            return false;
        }

        if(currentUrl.equals(lastVideoUrl))
            return false;

        lastVideoUrl = currentUrl;

        return true;

    }

    public String getCurrentVideoKey() {
        Object key = page.evaluate("""
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

        return key == null ? "" : key.toString();
    }

}
