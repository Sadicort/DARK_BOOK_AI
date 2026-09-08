package darkbook.intelligence;

import com.microsoft.playwright.Page;

public class PageHealthMonitor {

    private final Page page;

    public PageHealthMonitor(Page page){
        this.page = page;
    }

    public boolean healthy(){

        try{

            page.title();

            return page.locator("body").count() > 0;

        }catch(Exception e){

            return false;

        }

    }

}