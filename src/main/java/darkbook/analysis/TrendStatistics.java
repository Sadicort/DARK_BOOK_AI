package darkbook.analysis;

public class TrendStatistics {

    private int analyzedVideos;
    private int trendingVideos;
    private int totalHashtags;
    private int totalAudios;

    public void increaseVideos(){
        analyzedVideos++;
    }

    public void increaseTrending(){
        trendingVideos++;
    }

    public void setTotalHashtags(int totalHashtags){
        this.totalHashtags = totalHashtags;
    }

    public void setTotalAudios(int totalAudios){
        this.totalAudios = totalAudios;
    }

    public int getAnalyzedVideos(){
        return analyzedVideos;
    }

    public int getTrendingVideos(){
        return trendingVideos;
    }

    public int getTotalHashtags(){
        return totalHashtags;
    }

    public int getTotalAudios(){
        return totalAudios;
    }

}