package darkbook.intelligence;

import java.util.*;

public class KeywordExtractor {

    private static final Set<String> STOPWORDS = Set.of(
            "de","la","el","y","en","que","un","una",
            "los","las","con","por","para","es","del"
    );

    public List<String> extract(String text){

        List<String> keywords = new ArrayList<>();

        if(text == null)
            return keywords;

        for(String word : text.toLowerCase().split(" ")){

            if(word.length() < 3)
                continue;

            if(STOPWORDS.contains(word))
                continue;

            keywords.add(word);

        }

        return keywords;

    }

}