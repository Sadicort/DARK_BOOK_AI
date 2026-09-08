package darkbook.automation;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import darkbook.intelligence.ContentAnalyzer;
import darkbook.models.VideoRecord;
import darkbook.scanner.VideoSource;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/** Collects metadata from explicit public TikTok URLs; it performs no login, evasion or CAPTCHA bypass. */
public final class PlaywrightVideoSource implements VideoSource {
    private final ArrayDeque<String> urls;
    private final ContentAnalyzer analyzer;
    private final Playwright playwright;
    private final Browser browser;

    public PlaywrightVideoSource(Collection<String> urls, ContentAnalyzer analyzer, boolean headless) {
        this.urls = new ArrayDeque<>(urls.stream().filter(PlaywrightVideoSource::allowed).toList());
        this.analyzer = analyzer;
        this.playwright = Playwright.create();
        this.browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(headless));
    }

    @Override public Optional<VideoRecord> collectNext() {
        String url = urls.pollFirst();
        if (url == null) return Optional.empty();
        try (Page page = browser.newPage()) {
            page.navigate(url, new Page.NavigateOptions().setWaitUntil(com.microsoft.playwright.options.WaitUntilState.DOMCONTENTLOADED));
            String description = meta(page, "meta[property='og:description']");
            if (description.isBlank()) description = page.title();
            String author = meta(page, "meta[name='twitter:title']");
            var analysis = analyzer.analyze(description);
            return Optional.of(new VideoRecord("tiktok-" + UUID.nameUUIDFromBytes(url.getBytes()), "tiktok", url,
                    description, author.isBlank() ? "unknown" : author, 0, 0, 0, analysis.category(),
                    analysis.emotion(), analysis.hashtags(), "", Instant.now()));
        }
    }

    private static String meta(Page page, String selector) {
        var locator = page.locator(selector).first();
        return locator.count() == 0 ? "" : java.util.Objects.toString(locator.getAttribute("content"), "");
    }

    private static boolean allowed(String value) {
        try {
            URI uri = URI.create(value);
            String host = uri.getHost();
            return "https".equalsIgnoreCase(uri.getScheme()) && host != null &&
                    (host.equals("tiktok.com") || host.endsWith(".tiktok.com"));
        } catch (IllegalArgumentException ignored) { return false; }
    }

    @Override public String name() { return "TikTok Public URLs"; }
    @Override public void close() { browser.close(); playwright.close(); }
}

