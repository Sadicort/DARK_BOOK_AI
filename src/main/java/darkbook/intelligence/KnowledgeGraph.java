package darkbook.intelligence;

import java.util.*;

public class KnowledgeGraph {

    private final Map<String, Set<String>> graph =
            new HashMap<>();

    public void connect(String topic, String keyword){

        graph.computeIfAbsent(topic,
                k -> new HashSet<>()).add(keyword);

    }

    public Set<String> related(String topic){

        return graph.getOrDefault(topic, Set.of());

    }

}