package darkbook.scanner;

import com.microsoft.playwright.Page;

public class VideoObserver {

    private final Page page;
    private final VideoPlayer player;

    public VideoObserver(Page page) {
        this.page = page;
        this.player = new VideoPlayer(page);
    }

    public boolean waitForVideo() {

        System.out.println("Esperando un video...");

        try {

            page.waitForSelector("video", new Page.WaitForSelectorOptions()
                    .setTimeout(15000));

            if (!player.ensurePlaying()) {
                System.out.println("El video no se pudo reproducir; se omitirá.");
                return false;
            }

            System.out.println("Video detectado.");

            return true;

        } catch (Exception e) {

            System.out.println("No se detectó video.");

            return false;

        }

    }


}
