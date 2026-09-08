package darkbook.intelligence;

import java.util.*;

public class TopicDetector {

    private final Map<String,String> topics = Map.ofEntries(

            Map.entry("minecraft","Minecraft"),
            Map.entry("creeper","Minecraft"),

            Map.entry("geometry","Geometry Dash"),
            Map.entry("demon","Geometry Dash"),

            Map.entry("ia","AI Stories"),
            Map.entry("robot","AI Stories"),

            Map.entry("terror","Horror"),
            Map.entry("scary","Horror")

    );

    public Set<String> detectTopics(List<String> keywords){

        Set<String> detected = new HashSet<>();

        for(String keyword : keywords){

            if(topics.containsKey(keyword))
                detected.add(topics.get(keyword));

        }

        return detected;

    }

}