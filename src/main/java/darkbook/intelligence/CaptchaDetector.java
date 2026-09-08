package darkbook.intelligence;

import com.microsoft.playwright.Page;

public class CaptchaDetector {

    private final Page page;

    public CaptchaDetector(Page page) {
        this.page = page;
    }

    public boolean captchaPresent() {
        try {
            return page.locator("text=Verify").count() > 0
                    || page.locator("text=Verifica").count() > 0
                    || page.locator("text=Captcha").count() > 0;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
