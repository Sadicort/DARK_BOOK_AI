package darkbook.intelligence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class KnowledgeNode {

    public String title;

    public String description;

    public List<String> keywords;

    public Set<String> topics;

    public String emotion;

    public String sourceUrl;

    public LocalDateTime createdAt =
            LocalDateTime.now();

}