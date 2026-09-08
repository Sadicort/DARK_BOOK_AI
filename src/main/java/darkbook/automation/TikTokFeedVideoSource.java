package darkbook.automation;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitUntilState;
import darkbook.intelligence.ContentAnalyzer;
import darkbook.models.VideoRecord;
import darkbook.scanner.VideoSource;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Drives an authorized TikTok "For You" session in a real browser: watches the centered video for a
 * human-like interval, extracts its public metadata and returns one {@link VideoRecord} per call so the
 * canonical knowledge pipeline stores it. It performs no login itself (the persistent {@code userDataDir}
 * profile keeps whatever session the user established) and no CAPTCHA bypass.
 */
public final class TikTokFeedVideoSource implements VideoSource {
    private static final String FEED_URL = "https://www.tiktok.com/foryou";
    private static final Pattern VIDEO_ID = Pattern.compile("/video/(\\d+)");

    /** Snapshot of the scanner/playwright configuration this source needs. */
    public record Options(boolean headless, String channel, Path userDataDir, boolean autoInstallBrowser,
                          int watchSecondsMin, int watchSecondsMax, boolean skipAds, boolean skipDuplicates,
                          boolean humanBehavior, int captchaPauseSeconds, int maxConsecutiveSkips, int timeoutMs) { }

    private final Options options;
    private final ContentAnalyzer analyzer;
    private final java.util.Set<String> visited = new java.util.HashSet<>();

    private Playwright playwright;
    private BrowserContext context;
    private Page page;

    public TikTokFeedVideoSource(Options options, ContentAnalyzer analyzer) {
        this.options = Objects.requireNonNull(options, "options");
        this.analyzer = Objects.requireNonNull(analyzer, "analyzer");
    }

    @Override public String name() { return "TikTok For You" + (options.headless() ? " (headless)" : ""); }

    @Override public synchronized Optional<VideoRecord> collectNext() throws Exception {
        if (page == null) open();

        int skips = 0;
        String reason = "";
        while (true) {
            if (Thread.currentThread().isInterrupted()) throw new InterruptedException("Escaneo interrumpido");
            if (skips > options.maxConsecutiveSkips()) {
                throw new IllegalStateException("Demasiados intentos sin un video utilizable: " + reason);
            }

            if (!pageHealthy()) { reason = "la página de TikTok no responde"; recover(); skips++; continue; }
            if (captchaPresent()) {
                reason = "CAPTCHA de TikTok activo";
                if (options.captchaPauseSeconds() > 0) sleepInterruptibly(options.captchaPauseSeconds() * 1000L);
                skips++;
                continue;
            }
            if (!ensurePlaying()) { reason = "no hay video reproducible en el feed"; scrollNext(); skips++; continue; }

            String key = currentVideoKey();
            if (options.skipAds() && isAdvertisement()) { reason = "anuncio omitido"; scrollNext(); skips++; continue; }
            if (options.skipDuplicates() && (key.isBlank() || !visited.add(key))) {
                reason = "video duplicado omitido"; scrollNext(); skips++; continue;
            }

            humanWatch();
            VideoRecord record = extract(key);
            scrollNext();
            return Optional.of(record);
        }
    }

    @Override public synchronized void close() {
        try { if (context != null) context.close(); } catch (RuntimeException ignored) { /* shutting down */ }
        try { if (playwright != null) playwright.close(); } catch (RuntimeException ignored) { /* shutting down */ }
        context = null; playwright = null; page = null;
    }

    // ------------------------------------------------------------------ browser lifecycle

    private void open() throws Exception {
        playwright = Playwright.create();
        context = launchContext();
        context.addInitScript(STEALTH_SCRIPT);
        page = context.pages().isEmpty() ? context.newPage() : context.pages().get(0);
        page.setDefaultTimeout(options.timeoutMs());
        page.navigate(FEED_URL, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        try {
            page.waitForSelector("video", new Page.WaitForSelectorOptions().setTimeout(options.timeoutMs()));
        } catch (RuntimeException firstLoad) {
            // The feed can require a manual login on the very first run; collectNext() retries and recovers.
        }
    }

    private BrowserContext launchContext() throws Exception {
        List<String> args = List.of(
                "--disable-blink-features=AutomationControlled", "--disable-infobars",
                "--no-default-browser-check", "--disable-dev-shm-usage", "--start-maximized");
        BrowserType chromium = playwright.chromium();

        if (options.channel() != null && !options.channel().isBlank()) {
            try {
                return chromium.launchPersistentContext(options.userDataDir(),
                        persistentOptions(args).setChannel(options.channel()));
            } catch (RuntimeException noSystemBrowser) {
                // Falls through to the bundled Chromium below.
            }
        }
        try {
            return chromium.launchPersistentContext(options.userDataDir(), persistentOptions(args));
        } catch (RuntimeException noBundledBrowser) {
            if (!options.autoInstallBrowser()) throw noBundledBrowser;
            installChromium();
            return chromium.launchPersistentContext(options.userDataDir(), persistentOptions(args));
        }
    }

    private BrowserType.LaunchPersistentContextOptions persistentOptions(List<String> args) {
        return new BrowserType.LaunchPersistentContextOptions()
                .setHeadless(options.headless())
                .setArgs(args)
                .setViewportSize(1280, 720)
                .setLocale("es-ES")
                .setTimezoneId("America/Santo_Domingo")
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                        + "(KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36")
                .setIgnoreHTTPSErrors(true);
    }

    /** Installs the Playwright Chromium build in a child process so a failure never calls System.exit here. */
    private static void installChromium() throws Exception {
        String javaBin = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        String classpath = System.getProperty("java.class.path");
        Process process = new ProcessBuilder(javaBin, "-cp", classpath,
                "com.microsoft.playwright.CLI", "install", "chromium").inheritIO().start();
        if (!process.waitFor(10, TimeUnit.MINUTES) || process.exitValue() != 0) {
            process.destroy();
            throw new IllegalStateException("No se pudo instalar Chromium de Playwright automáticamente");
        }
    }

    // ------------------------------------------------------------------ per-video steps

    private void humanWatch() throws InterruptedException {
        int low = Math.max(1, options.watchSecondsMin());
        int high = Math.max(low + 1, options.watchSecondsMax() + 1);
        long deadline = System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(low, high) * 1000L;
        while (System.currentTimeMillis() < deadline) {
            if (Thread.currentThread().isInterrupted()) throw new InterruptedException("Escaneo interrumpido");
            Thread.sleep(200);
            if (options.humanBehavior() && ThreadLocalRandom.current().nextInt(12) == 0) randomMouseMove();
        }
    }

    private void sleepInterruptibly(long millis) throws InterruptedException {
        long deadline = System.currentTimeMillis() + millis;
        while (System.currentTimeMillis() < deadline) {
            if (Thread.currentThread().isInterrupted()) throw new InterruptedException("Escaneo interrumpido");
            Thread.sleep(200);
        }
    }

    private void randomMouseMove() {
        try {
            page.mouse().move(ThreadLocalRandom.current().nextInt(100, 1000), ThreadLocalRandom.current().nextInt(100, 600));
        } catch (RuntimeException ignored) { /* mouse jitter is best-effort */ }
    }

    private VideoRecord extract(String key) {
        String url = key.startsWith("http") ? key : page.url();
        String description = firstText("[data-e2e='browse-video-desc']", "[data-e2e='video-desc']");
        String author = firstText("[data-e2e='browse-username']", "[data-e2e='video-author-uniqueid']");
        String audio = firstText("[data-e2e='browse-music']", "[data-e2e='video-music']");
        long likes = parseMetric(firstText("[data-e2e='like-count']"));
        long comments = parseMetric(firstText("[data-e2e='comment-count']"));
        long shares = parseMetric(firstText("[data-e2e='share-count']"));

        String text = description == null || description.isBlank() ? page.title() : description;
        ContentAnalyzer.Analysis analysis = analyzer.analyze(text == null ? "" : text);
        return new VideoRecord("tiktok-" + videoId(url), "tiktok", url, text == null ? "" : text,
                author == null || author.isBlank() ? "unknown" : author,
                likes, comments, shares, analysis.category(), analysis.emotion(), analysis.hashtags(),
                audio == null ? "" : audio, Instant.now());
    }

    private static String videoId(String url) {
        Matcher matcher = VIDEO_ID.matcher(url);
        return matcher.find() ? matcher.group(1) : UUID.nameUUIDFromBytes(url.getBytes()).toString();
    }

    /** Faithful port of the previous scanner's "1.2M" / "3,4K" style metric parser. */
    static long parseMetric(String value) {
        if (value == null || value.isBlank()) return 0;
        String normalized = value.replace(",", "").trim().toUpperCase(java.util.Locale.ROOT);
        try {
            if (normalized.endsWith("K")) return (long) (Double.parseDouble(normalized.replace("K", "")) * 1_000);
            if (normalized.endsWith("M")) return (long) (Double.parseDouble(normalized.replace("M", "")) * 1_000_000);
            if (normalized.endsWith("B")) return (long) (Double.parseDouble(normalized.replace("B", "")) * 1_000_000_000);
            return Long.parseLong(normalized);
        } catch (NumberFormatException notANumber) {
            return 0;
        }
    }

    private String firstText(String... selectors) {
        for (String selector : selectors) {
            try {
                var locator = page.locator(selector).first();
                if (locator.count() > 0) {
                    String text = locator.textContent();
                    if (text != null && !text.isBlank()) return text.trim();
                }
            } catch (RuntimeException ignored) { /* try the next selector */ }
        }
        return null;
    }

    // ------------------------------------------------------------------ page probes (ported JS)

    private boolean pageHealthy() {
        try {
            page.title();
            return page.locator("body").count() > 0;
        } catch (RuntimeException unhealthy) {
            return false;
        }
    }

    private void recover() {
        try {
            page.navigate(FEED_URL, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
            page.waitForTimeout(2_000);
        } catch (RuntimeException ignored) { /* collectNext() will keep retrying */ }
    }

    private boolean captchaPresent() {
        try {
            return page.locator("text=Verify").count() > 0
                    || page.locator("text=Verifica").count() > 0
                    || page.locator("text=Captcha").count() > 0;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private boolean isAdvertisement() {
        try {
            return Boolean.TRUE.equals(page.evaluate(AD_SCRIPT));
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private boolean ensurePlaying() {
        try {
            Boolean started = (Boolean) page.evaluate(PLAY_SCRIPT);
            page.waitForTimeout(800);
            return Boolean.TRUE.equals(started) || Boolean.TRUE.equals(page.evaluate(IS_PLAYING_SCRIPT));
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private String currentVideoKey() {
        try {
            Object key = page.evaluate(KEY_SCRIPT);
            return key == null ? "" : key.toString();
        } catch (RuntimeException ignored) {
            return "";
        }
    }

    private void scrollNext() {
        try {
            if (options.humanBehavior()) { randomMouseMove(); page.waitForTimeout(ThreadLocalRandom.current().nextInt(300, 900)); }
            var next = page.locator("[data-e2e='feed-navigation-next']").first();
            if (next.count() > 0 && next.isVisible()) { next.click(); page.waitForTimeout(1_200); return; }
        } catch (RuntimeException ignored) { /* fall back to keyboard + wheel */ }
        try { page.keyboard().press("ArrowDown"); page.waitForTimeout(900); } catch (RuntimeException ignored) { }
        try { page.mouse().wheel(0, 1_200); page.waitForTimeout(1_200); } catch (RuntimeException ignored) { }
    }

    // ------------------------------------------------------------------ browser-side scripts

    private static final String CENTERED_VIDEO = """
            const centered = () => [...document.querySelectorAll('video')]
                .filter(item => {
                    const rect = item.getBoundingClientRect();
                    return rect.width > 0 && rect.height > 0 && rect.bottom > 0 && rect.top < window.innerHeight;
                })
                .sort((a, b) => {
                    const center = window.innerHeight / 2;
                    const ay = (a.getBoundingClientRect().top + a.getBoundingClientRect().bottom) / 2;
                    const by = (b.getBoundingClientRect().top + b.getBoundingClientRect().bottom) / 2;
                    return Math.abs(ay - center) - Math.abs(by - center);
                })[0];
            """;

    private static final String PLAY_SCRIPT = "async () => {" + CENTERED_VIDEO + """
            const video = centered();
            if (!video) return false;
            try { await video.play(); } catch (error) { video.muted = true; try { await video.play(); } catch (ignored) {} }
            return !video.paused && !video.ended;
        }
        """;

    private static final String IS_PLAYING_SCRIPT = "() => {" + CENTERED_VIDEO + """
            const video = centered();
            return Boolean(video && !video.paused && !video.ended);
        }
        """;

    private static final String KEY_SCRIPT = "() => {" + CENTERED_VIDEO + """
            const video = centered();
            if (!video) return '';
            const container = video.closest('[data-e2e="recommend-list-item-container"]');
            const link = container?.querySelector('a[href*="/video/"]');
            return link?.href || video.currentSrc || video.src || video.poster || '';
        }
        """;

    private static final String AD_SCRIPT = "() => {" + CENTERED_VIDEO + """
            const video = centered();
            if (!video) return false;
            const container = video.closest('[data-e2e="recommend-list-item-container"]');
            const text = (container?.innerText || '').toLowerCase();
            return text.includes('sponsored') || text.includes('patrocinado') || text.includes('promocionado');
        }
        """;

    /** Ported verbatim from the previous scanner's StealthManager. */
    private static final String STEALTH_SCRIPT = """
            (() => {
              Object.defineProperty(navigator, 'webdriver', { get: () => undefined });
              Object.defineProperty(navigator, 'platform', { get: () => 'Win32' });
              Object.defineProperty(navigator, 'language', { get: () => 'es-ES' });
              Object.defineProperty(navigator, 'languages', { get: () => ['es-ES', 'es', 'en-US'] });
              Object.defineProperty(navigator, 'hardwareConcurrency', { get: () => 8 });
              Object.defineProperty(navigator, 'deviceMemory', { get: () => 8 });
              Object.defineProperty(navigator, 'plugins', { get: () => [1, 2, 3, 4, 5] });
              window.chrome = { runtime: {}, app: {}, csi: () => {}, loadTimes: () => {} };
              const originalQuery = navigator.permissions && navigator.permissions.query;
              if (originalQuery) {
                navigator.permissions.query = (parameters) => (
                  parameters.name === 'notifications'
                    ? Promise.resolve({ state: Notification.permission })
                    : originalQuery.call(navigator.permissions, parameters)
                );
              }
            })();
            """;
}
