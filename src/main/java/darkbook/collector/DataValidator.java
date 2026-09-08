package darkbook.collector;

import darkbook.models.VideoData;

public class DataValidator {

    public boolean validate(VideoData video){

        if(video.getUsername() == null)
            return false;

        if(video.getDescription() == null)
            return false;

        if(video.getDescription().length() < 5)
            return false;

        return true;

    }

}