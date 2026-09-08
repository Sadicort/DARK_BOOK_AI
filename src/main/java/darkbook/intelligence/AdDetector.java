package darkbook.intelligence;

import com.microsoft.playwright.Page;

public class AdDetector {

    private final Page page;

    public AdDetector(Page page){
        this.page = page;
    }

    public boolean isAdvertisement(){

        try{
            return Boolean.TRUE.equals(page.evaluate("""
                    () => {
                        const video = [...document.querySelectorAll('video')]
                            .filter(item => {
                                const rect = item.getBoundingClientRect();
                                return rect.width > 0 && rect.height > 0 &&
                                       rect.bottom > 0 && rect.top < window.innerHeight;
                            })[0];

                        if (!video) return false;

                        const container = video.closest(
                            '[data-e2e="recommend-list-item-container"]'
                        );
                        const text = (container?.innerText || '').toLowerCase();

                        return text.includes('sponsored') || text.includes('patrocinado');
                    }
                    """));

        }catch(Exception e){

            return false;

        }

    }

}
