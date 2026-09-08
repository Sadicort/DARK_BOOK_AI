package darkbook.intelligence;

import com.microsoft.playwright.Page;
import darkbook.core.Constants;
import darkbook.utils.Logger;

public class RecoveryEngine {

    private final Page page;

    public RecoveryEngine(Page page){
        this.page = page;
    }

    public void reloadTikTok(){

        Logger.warning("Recargando TikTok...");

        page.navigate(Constants.TIKTOK_URL);

        page.waitForLoadState();

    }

    public boolean pageHealthy(){

        try{

            return page.locator("video").count() > 0;

        }catch(Exception e){

            return false;

        }

    }

}