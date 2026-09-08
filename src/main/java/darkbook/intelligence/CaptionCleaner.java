package darkbook.intelligence;

public class CaptionCleaner {

    public String clean(String caption){

        if(caption == null)
            return "";

        return caption
                .replaceAll("https?://\\S+", "")
                .replaceAll("@\\w+", "")
                .replaceAll("#", "")
                .replaceAll("[^\\p{L}\\p{N}\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();

    }

}