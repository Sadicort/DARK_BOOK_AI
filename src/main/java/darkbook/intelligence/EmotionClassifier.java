package darkbook.intelligence;

import java.util.Map;

public class EmotionClassifier {

    private static final Map<String,String> EMOTIONS = Map.ofEntries(

            Map.entry("terror","FEAR"),
            Map.entry("miedo","FEAR"),
            Map.entry("horror","FEAR"),
            Map.entry("scary","FEAR"),

            Map.entry("feliz","JOY"),
            Map.entry("meme","JOY"),
            Map.entry("divertido","JOY"),

            Map.entry("secreto","SURPRISE"),
            Map.entry("descubrió","SURPRISE"),
            Map.entry("increíble","SURPRISE"),

            Map.entry("odio","ANGER"),
            Map.entry("rage","ANGER")

    );

    public String detect(String text){

        if(text == null)
            return "NEUTRAL";

        text = text.toLowerCase();

        for(String keyword : EMOTIONS.keySet()){

            if(text.contains(keyword))
                return EMOTIONS.get(keyword);

        }

        return "NEUTRAL";

    }

}