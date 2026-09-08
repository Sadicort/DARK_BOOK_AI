package darkbook.analysis;

import darkbook.models.VideoData;

public class TrendScoreEngine {

    public int calculate(VideoData video){

        long likes =
                MetricParser.parse(video.getLikes());

        long comments =
                MetricParser.parse(video.getComments());

        long shares =
                MetricParser.parse(video.getShares());

        long favorites =
                MetricParser.parse(video.getFavorites());

        double score =
                likes +
                        comments * 3 +
                        shares * 5 +
                        favorites * 2;

        score = score / 20000;

        if(score > 100)
            score = 100;

        return (int)Math.round(score);

    }

}