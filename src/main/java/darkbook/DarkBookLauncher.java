package darkbook;

import javafx.application.Application;

/** Plain Java launcher avoids the JDK's special JavaFX module-path check in packaged classpath applications. */
public final class DarkBookLauncher {
    private DarkBookLauncher() { }

    public static void main(String[] args) {
        Application.launch(DarkBookApplication.class, args);
    }
}
