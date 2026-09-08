package darkbook.core;

import com.microsoft.playwright.*;
import darkbook.database.DatabaseInitializer;
import darkbook.database.DatabaseManager;

import java.nio.file.Paths;

public class BrowserManager {


    private static BrowserManager instance;

    private Playwright playwright;
    private BrowserContext context;
    private Page page;

    private BrowserManager() {}

    // ==========================
    // Singleton
    // ==========================

    public static BrowserManager getInstance() {

        if (instance == null) {
            instance = new BrowserManager();
        }

        return instance;
    }

    // ==========================
    // Inicializar navegador
    // ==========================

    public void startBrowser() {

        ConfigManager.Config config = ConfigManager.loadConfig();

        DatabaseInitializer.initialize();

        SessionManager.initializeSessionFolder();

        playwright = Playwright.create();

        context = playwright.chromium().launchPersistentContext(
                Paths.get(Constants.USER_DATA_DIR),

                new BrowserType.LaunchPersistentContextOptions()

                        .setHeadless(config.browser.headless)

                        .setChannel("chrome")   // Usa Google Chrome instalado

                        .setSlowMo((double) config.browser.slowMo)

                        .setUserAgent(BrowserProfile.USER_AGENT)

                        .setLocale(BrowserProfile.LOCALE)

                        .setTimezoneId(BrowserProfile.TIMEZONE)

                        .setViewportSize(
                                config.browser.width,
                                config.browser.height
                        )

                        .setIgnoreHTTPSErrors(true)

                        .setArgs(java.util.List.of(

                                "--disable-blink-features=AutomationControlled",

                                "--disable-infobars",

                                "--start-maximized",

                                "--disable-dev-shm-usage",

                                "--no-default-browser-check",

                                "--disable-features=IsolateOrigins,site-per-process"

                        ))
        );

        if (config.browser.stealth) {
            StealthManager.apply(context);
        }

        page = context.pages().isEmpty()
                ? context.newPage()
                : context.pages().getFirst();

        System.out.println("Chromium iniciado correctamente.");

    }

    // ==========================
    // Abrir TikTok
    // ==========================

    public void openTikTok(){

        page.navigate(Constants.TIKTOK_URL);

        page.waitForLoadState();

        page.waitForTimeout(3000);

        System.out.println("TikTok abierto correctamente.");

    }

    // ==========================
    // Métodos utilitarios
    // ==========================

    public Page getPage() {
        return page;
    }

    public BrowserContext getContext() {
        return context;
    }

    // ==========================
    // Cerrar navegador
    // ==========================

    public void closeBrowser() {

        if (context != null)
            context.close();

        if (playwright != null)
            playwright.close();

        DatabaseManager.closeConnection();

        System.out.println("Navegador cerrado.");

    }


}
