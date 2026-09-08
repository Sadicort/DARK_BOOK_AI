package darkbook;

import darkbook.core.BrowserManager;
import darkbook.core.ConfigManager;
import darkbook.scanner.TrendScanner;



public class DarkBook {


    public static void main(String[] args) {


        System.out.println("""

██████╗  █████╗ ██████╗ ██╗  ██╗
██╔══██╗██╔══██╗██╔══██╗██║ ██╔╝
██║  ██║███████║██████╔╝█████╔╝
██║  ██║██╔══██║██╔══██╗██╔═██╗
██████╔╝██║  ██║██║  ██║██║  ██╗
╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═╝╚═╝  ╚═╝

        DARK BOOK AI
        Trend Intelligence Engine
        """);

        ConfigManager.loadConfig();

        BrowserManager browser = BrowserManager.getInstance();

        try {
            browser.startBrowser();

            TrendScanner scanner = new TrendScanner();

            scanner.startScanner();
        } finally {
            browser.closeBrowser();
        }

    }

}
