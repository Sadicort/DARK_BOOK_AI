package darkbook;

import darkbook.dashboard.LocalApiServer;
import darkbook.services.DarkBookRuntime;

import java.util.concurrent.CountDownLatch;

/** Diagnostic/API entrypoint for servers and automated verification without opening the desktop window. */
public final class DarkBookHeadless {
    private DarkBookHeadless() { }

    public static void main(String[] args) throws Exception {
        DarkBookRuntime runtime = new DarkBookRuntime(); runtime.initialize();
        var config = runtime.configuration().read("config.json");
        LocalApiServer api = new LocalApiServer(runtime);
        int port = api.start(config.path("api").path("host").asText("127.0.0.1"), config.path("api").path("port").asInt(17321));
        System.out.println("Dark Book API listening on http://127.0.0.1:" + port);
        CountDownLatch latch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            api.close(); try { runtime.close(); } catch (Exception ignored) { } latch.countDown();
        }));
        latch.await();
    }
}

