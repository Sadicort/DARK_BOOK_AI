package darkbook.intelligence;

import com.microsoft.playwright.Page;

public class DetectionChecker {

    private final Page page;

    public DetectionChecker(Page page){
        this.page = page;
    }

    public void printStatus(){

        Object webdriver = page.evaluate("""
            () => navigator.webdriver
        """);

        Object languages = page.evaluate("""
            () => navigator.languages
        """);

        System.out.println("webdriver = " + webdriver);

        System.out.println("languages = " + languages);

    }

}