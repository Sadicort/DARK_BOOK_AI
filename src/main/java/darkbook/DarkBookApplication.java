package darkbook;

import com.fasterxml.jackson.databind.JsonNode;
import darkbook.dashboard.LocalApiServer;
import darkbook.services.DarkBookRuntime;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.stage.Stage;

/** Desktop entrypoint: boots Java services and renders Dark Book OS in an embedded JavaFX WebView. */
public final class DarkBookApplication extends Application {
    private DarkBookRuntime runtime;
    private LocalApiServer api;
    private int port;

    @Override public void init() throws Exception {
        runtime = new DarkBookRuntime(); runtime.initialize();
        JsonNode config = runtime.configuration().read("config.json");
        api = new LocalApiServer(runtime);
        port = api.start(config.path("api").path("host").asText("127.0.0.1"), config.path("api").path("port").asInt(17321));
    }

    @Override public void start(Stage stage) {
        WebView webView = new WebView();
        webView.getEngine().setJavaScriptEnabled(true);
        webView.getEngine().load("http://127.0.0.1:" + port + "/");
        var bounds = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(webView, Math.min(1500, bounds.getWidth() * .9), Math.min(940, bounds.getHeight() * .9));
        stage.setTitle("DARK BOOK AI — Artificial Intelligence Operating System");
        stage.setMinWidth(1000); stage.setMinHeight(680); stage.setScene(scene); stage.centerOnScreen(); stage.show();
        stage.setOnCloseRequest(event -> Platform.exit());
    }

    @Override public void stop() throws Exception {
        if (api != null) api.close(); if (runtime != null) runtime.close();
    }

    public static void main(String[] args) { launch(args); }
}

