package darkbook.intelligence;

public class ObsidianManager {

    private final MarkdownWriter writer =
            new MarkdownWriter();

    public void saveKnowledge(KnowledgeNode node){

        writer.writeVideo(node);

        System.out.println("Nodo guardado en Obsidian.");

    }

}