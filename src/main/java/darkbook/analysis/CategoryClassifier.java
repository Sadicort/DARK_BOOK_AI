package darkbook.analysis;

import darkbook.models.VideoData;

import java.util.Map;

public class CategoryClassifier {

    private static final Map<String,String> KEYWORDS = Map.ofEntries(

            Map.entry("minecraft","MINECRAFT"),
            Map.entry("creeper","MINECRAFT"),
            Map.entry("bedrock","MINECRAFT"),

            Map.entry("geometry dash","GEOMETRY_DASH"),
            Map.entry("gd","GEOMETRY_DASH"),
            Map.entry("extreme demon","GEOMETRY_DASH"),

            Map.entry("ia","AI_STORIES"),
            Map.entry("inteligencia artificial","AI_STORIES"),
            Map.entry("robot","AI_STORIES"),

            Map.entry("terror","HORROR"),
            Map.entry("horror","HORROR"),
            Map.entry("scary","HORROR"),

            Map.entry("codm","COD_MOBILE"),
            Map.entry("battle royale","COD_MOBILE")

    );

    public String classify(VideoData video){

        if(video.getDescription()==null)
            return "UNKNOWN";

        String text =
                video.getDescription().toLowerCase();

        for(String keyword : KEYWORDS.keySet()){

            if(text.contains(keyword))
                return KEYWORDS.get(keyword);

        }

        return "OTHER";

    }

}