package darkbook.scanner;

import com.microsoft.playwright.Page;
import darkbook.collector.DataCollector;
import darkbook.core.BrowserManager;
import darkbook.core.ConfigManager;
import darkbook.intelligence.AdDetector;
import darkbook.intelligence.CaptchaDetector;
import darkbook.intelligence.DuplicateDetector;
import darkbook.intelligence.IntelligenceReport;
import darkbook.intelligence.PageHealthMonitor;
import darkbook.intelligence.RecoveryEngine;
import darkbook.intelligence.ScrollStrategy;
import darkbook.intelligence.VideoDetector;
import darkbook.models.VideoData;
import darkbook.utils.Logger;

public class TrendScanner {

    private ScannerStatus status = ScannerStatus.STARTING;
    private final ScannerStatistics statistics = new ScannerStatistics();
    private final BrowserManager browser = BrowserManager.getInstance();

    public void startScanner() {
        ConfigManager.Config config = ConfigManager.getConfig();

        if (config == null || config.scanner == null) {
            throw new IllegalStateException("La configuración del scanner no está disponible.");
        }

        statistics.start();
        status = ScannerStatus.LOADING_TIKTOK;
        browser.openTikTok();

        Page page = browser.getPage();
        VideoDetector detector = new VideoDetector(page);
        DuplicateDetector duplicates = new DuplicateDetector();
        AdDetector ads = new AdDetector(page);
        CaptchaDetector captcha = new CaptchaDetector(page);
        RecoveryEngine recovery = new RecoveryEngine(page);
        PageHealthMonitor health = new PageHealthMonitor(page);
        ScrollStrategy scroll = new ScrollStrategy(page);
        IntelligenceReport report = new IntelligenceReport();
        DataCollector collector = new DataCollector(page);
        VideoObserver observer = new VideoObserver(page);
        WatchTimer timer = new WatchTimer();

        boolean skipAds = config.intelligence != null && config.intelligence.skip_ads;
        boolean skipDuplicates = config.intelligence != null
                && config.intelligence.skip_duplicates;
        boolean autoRecovery = config.intelligence == null
                || config.intelligence.auto_recovery;
        boolean pauseOnCaptcha = config.intelligence != null
                && config.intelligence.captcha_pause;

        System.out.println("==============================");
        System.out.println("Trend Scanner iniciado.");
        System.out.println("==============================");

        for (int i = 0; i < config.scanner.videos_limit; i++) {
            try {
                status = ScannerStatus.WAITING_VIDEO;

                if (!health.healthy()) {
                    Logger.warning("La página no responde correctamente.");
                    recoverIfEnabled(recovery, report, autoRecovery);
                    continue;
                }

                if (captcha.captchaPresent()) {
                    status = ScannerStatus.PAUSED;
                    report.captcha();
                    Logger.warning("CAPTCHA detectado.");

                    if (pauseOnCaptcha) {
                        page.waitForTimeout(15000);
                    }

                    continue;
                }

                if (!observer.waitForVideo()) {
                    Logger.warning("No se encontró un video reproducible.");
                    continue;
                }

                if (skipAds && ads.isAdvertisement()) {
                    report.adSkipped();
                    Logger.info("Publicidad omitida.");
                    continue;
                }

                String videoKey = detector.getCurrentVideoKey();
                if (skipDuplicates
                        && (videoKey.isBlank() || duplicates.alreadyVisited(videoKey))) {
                    report.duplicated();
                    Logger.info("Video duplicado omitido.");
                    continue;
                }

                status = ScannerStatus.WATCHING_VIDEO;
                timer.watchVideo();

                VideoData video = collector.collect();
                if (video == null) {
                    continue;
                }

                statistics.increaseVideos();
                System.out.println("Videos observados: " + statistics.getVideosScanned());
            } catch (RuntimeException e) {
                status = ScannerStatus.ERROR;
                Logger.error("Error procesando el video: " + e.getMessage());
                recoverIfEnabled(recovery, report, autoRecovery);
            } finally {
                advanceToNextVideo(scroll, recovery, report, autoRecovery);
            }
        }

        statistics.finish();
        status = ScannerStatus.FINISHED;

        System.out.println("==============================");
        System.out.println("Escaneo terminado.");
        System.out.println("==============================");
        System.out.println("Videos: " + statistics.getVideosScanned());
        System.out.println("Tiempo: " + statistics.getTotalExecutionSeconds() + " segundos.");
        report.printReport();
    }

    private void advanceToNextVideo(
            ScrollStrategy scroll,
            RecoveryEngine recovery,
            IntelligenceReport report,
            boolean autoRecovery
    ) {
        status = ScannerStatus.SCROLLING;

        try {
            if (!scroll.scrollNextVideo()) {
                Logger.warning("El feed no avanzó al siguiente video.");
                recoverIfEnabled(recovery, report, autoRecovery);
            }
        } catch (RuntimeException e) {
            Logger.error("Error desplazando el feed: " + e.getMessage());
            recoverIfEnabled(recovery, report, autoRecovery);
        }
    }

    private void recoverIfEnabled(
            RecoveryEngine recovery,
            IntelligenceReport report,
            boolean autoRecovery
    ) {
        if (autoRecovery) {
            recovery.reloadTikTok();
            report.reload();
        }
    }

    public ScannerStatus getStatus() {
        return status;
    }

    public ScannerStatistics getStatistics() {
        return statistics;
    }
}
