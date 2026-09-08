package darkbook.core;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ConfigManager {

    private static Config config;

    public static Config loadConfig() {

        if (config != null) return config;

        try {

            InputStream input = ConfigManager.class
                    .getClassLoader()
                    .getResourceAsStream(Constants.CONFIG_PATH);

            if (input == null) {
                throw new RuntimeException("No se encontró config.json");
            }

            config = new Gson().fromJson(new InputStreamReader(input), Config.class);

            System.out.println("Config cargada correctamente.");

        } catch (Exception e) {
            throw new RuntimeException("Error leyendo configuración.", e);
        }

        return config;
    }

    public static Config getConfig() {
        return config;
    }

    // ==========================
    // Clases del JSON
    // ==========================

    public static class Config {

        public Browser browser;
        public Scanner scanner;
        public Collector collector;
        public Reports reports;
        public Debug debug;
        public Behavior behavior;
        public Intelligence intelligence;
        public Logging logging;

    }

    public static class Browser {
        public String engine;
        public boolean headless;
        public int slowMo;
        public int width;
        public int height;
        public boolean stealth;
    }

    public static class Scanner {
        public int hours;
        public int videos_limit;
        public String language;
        public String category;
    }

    public static class Collector {
        public boolean save_comments;
        public boolean save_audio;
        public boolean save_thumbnail;
    }

    public static class Reports {
        public boolean auto_generate;
        public String format;
    }

    public static class Debug {
        public boolean enabled;
        public boolean show_status;
        public boolean screenshots;
    }

    public static class Behavior {
        public boolean auto_play;
        public boolean human_mode;
        public boolean random_pause;
        public boolean scroll_variation;
    }

    public static class Intelligence {
        public boolean skip_ads;
        public boolean skip_duplicates;
        public boolean auto_recovery;
        public boolean captcha_pause;
    }

    public static class Logging {
        public boolean save_logs;
        public String level;
    }

}
