package darkbook.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class Logger {

    private static final String LOG_FILE = "logs/darkbook.log";

    public static void info(String message){
        write("INFO", message);
    }

    public static void warning(String message){
        write("WARNING", message);
    }

    public static void error(String message){
        write("ERROR", message);
    }

    private static void write(String level, String message){

        String log = "[" +
                LocalDateTime.now() +
                "] [" +
                level +
                "] " +
                message;

        System.out.println(log);

        Path logPath = Path.of(LOG_FILE);

        try {
            Files.createDirectories(logPath.getParent());
        } catch (IOException e) {
            System.err.println("No se pudo crear la carpeta de logs: " + e.getMessage());
            return;
        }

        try(FileWriter writer = new FileWriter(LOG_FILE, true)){
            writer.write(log + "\n");
        }catch(IOException e){
            System.err.println("No se pudo guardar el log: " + e.getMessage());
        }

    }

}
