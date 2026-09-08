package darkbook.scanner;

import com.microsoft.playwright.Page;

public class ScrollEngine {

    private final Page page;

    public ScrollEngine(Page page) {
        this.page = page;
    }

    public void nextVideo() {

        System.out.println("Bajando al siguiente video...");

        page.mouse().wheel(0, 1200);

        page.waitForTimeout(1500);

    }

    public void previousVideo() {

        System.out.println("Regresando al video anterior...");

        page.mouse().wheel(0, -1200);

        page.waitForTimeout(1500);

    }

}