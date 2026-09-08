package darkbook.collector;

import darkbook.models.VideoData;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextExtractor {

    public void extractHashtags(VideoData video){

        if(video.getDescription() == null)
            return;

        Pattern pattern = Pattern.compile("#(\\w+)");

        Matcher matcher =
                pattern.matcher(video.getDescription());

        List<String> hashtags = new ArrayList<>();

        while(matcher.find()){

            hashtags.add(matcher.group());

        }

        video.setHashtags(hashtags);

    }

}