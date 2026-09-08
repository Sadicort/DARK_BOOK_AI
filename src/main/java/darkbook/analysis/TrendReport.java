package darkbook.analysis;

import java.util.Map;

public class TrendReport {

    private Map<String,Integer> hashtags;
    private Map<String,Integer> audios;

    public TrendStatistics statistics;

    public void setHashtags(Map<String,Integer> hashtags){
        this.hashtags = hashtags;
    }

    public void setAudios(Map<String,Integer> audios){
        this.audios = audios;
    }

    public Map<String,Integer> getHashtags(){
        return hashtags;
    }

    public Map<String,Integer> getAudios(){
        return audios;
    }

}