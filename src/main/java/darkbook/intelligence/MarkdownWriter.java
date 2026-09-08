package darkbook.intelligence;

import java.io.File;
import java.io.FileWriter;

public class MarkdownWriter {

    public void writeVideo(KnowledgeNode node){

        try{

            new File("knowledge/videos").mkdirs();

            String file =
                    "knowledge/videos/" +
                            node.createdAt.toString().replace(":","-") +
                            ".md";

            FileWriter writer = new FileWriter(file);

            writer.write("# " + node.title + "\n\n");

            writer.write("## Descripción\n");
            writer.write(node.description + "\n\n");

            writer.write("## Emoción\n");
            writer.write(node.emotion + "\n\n");

            writer.write("## Topics\n");

            for(String topic : node.topics){

                writer.write("- [[" + topic + "]]\n");

            }

            writer.write("\n## Keywords\n");

            for(String keyword : node.keywords){

                writer.write("- " + keyword + "\n");

            }

            writer.write("\n## Fuente\n");
            writer.write(node.sourceUrl);

            writer.close();

        }catch(Exception e){

            e.printStackTrace();

        }

    }

}