package darkbook.collector;

import com.microsoft.playwright.Page;
import darkbook.models.VideoData;

public class MetricsExtractor {

    private final Page page;

    public MetricsExtractor(Page page){
        this.page = page;
    }

    public void extractMetrics(VideoData video){

        video.setLikes(getMetric("like-count"));

        video.setComments(getMetric("comment-count"));

        video.setShares(getMetric("share-count"));

        video.setFavorites(getMetric("undefined-count"));

    }

    private String getMetric(String dataId){

        try{

            return page.locator("[data-e2e='" + dataId + "']")
                    .first()
                    .textContent();

        }catch(Exception e){

            return "0";

        }

    }

}