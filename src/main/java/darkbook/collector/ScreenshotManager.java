package darkbook.collector;

import com.microsoft.playwright.Page;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScreenshotManager {

    private final Page page;

    public ScreenshotManager(Page page){
        this.page = page;
    }

    public String captureVideo(){

        try{

            Path folder = Path.of("screenshots");

            if(!Files.exists(folder)){
                Files.createDirectories(folder);
            }

            String filename =
                    "video_" +
                            LocalDateTime.now().format(
                                    DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
                            ) +
                            ".png";

            Path output = folder.resolve(filename);

            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(output));

            System.out.println("Screenshot guardado: " + filename);

            return output.toString();

        }catch(Exception e){

            System.out.println("Error capturando screenshot.");

            return "";

        }

    }

}