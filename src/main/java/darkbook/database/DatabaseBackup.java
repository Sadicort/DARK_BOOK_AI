package darkbook.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class DatabaseBackup {

    public void createBackup(){

        try{

            Files.createDirectories(Path.of("database/backups"));

            Path source = Path.of("database/darkbook.db");

            Path backup =
                    Path.of("database/backups/db_"
                            + LocalDateTime.now()
                            .toString()
                            .replace(":","-")
                            + ".db");

            Files.copy(source, backup);

            System.out.println("Backup creado.");

        }catch(IOException e){

            e.printStackTrace();

        }

    }

}