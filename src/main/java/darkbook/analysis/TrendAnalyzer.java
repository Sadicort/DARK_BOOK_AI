package darkbook.analysis;

import java.util.Map;

public class TrendAnalyzer {

    private final HashtagAnalyzer hashtagAnalyzer =
            new HashtagAnalyzer();

    private final AudioAnalyzer audioAnalyzer =
            new AudioAnalyzer();

    public TrendReport analyze(){

        TrendReport report = new TrendReport();

        TrendStatistics stats = new TrendStatistics();

        Map<String,Integer> hashtags =
                hashtagAnalyzer.topHashtags(10);

        Map<String,Integer> audios =
                audioAnalyzer.topAudios(10);

        report.setHashtags(hashtags);

        report.setAudios(audios);

        stats.setTotalHashtags(hashtags.size());

        stats.setTotalAudios(audios.size());

        report.statistics = stats;

        printReport(report);

        return report;

    }

    private void printReport(TrendReport report){

        System.out.println("\n========== TREND REPORT ==========");

        System.out.println("\nTOP HASHTAGS:");

        report.getHashtags().forEach((k,v)->
                System.out.println(k+" -> "+v));

        System.out.println("\nTOP AUDIOS:");

        report.getAudios().forEach((k,v)->
                System.out.println(k+" -> "+v));

        System.out.println("==============================");

    }

}