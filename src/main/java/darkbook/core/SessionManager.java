package darkbook.core;

import java.nio.file.Files;
import java.nio.file.Path;

public class SessionManager {

    public static void initializeSessionFolder() {

        try {

            Path sessionPath = Path.of(Constants.USER_DATA_DIR);

            if (!Files.exists(sessionPath)) {

                Files.createDirectories(sessionPath);

                System.out.println("Carpeta userdata creada.");

            } else {

                System.out.println("Carpeta userdata encontrada.");

            }

        } catch (Exception e) {

            throw new RuntimeException("No se pudo crear userdata.", e);

        }

    }

}