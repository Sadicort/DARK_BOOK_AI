package darkbook.collector;

import com.microsoft.playwright.Page;
import darkbook.models.VideoData;
import darkbook.database.VideoRepository;
import darkbook.database.AudioRepository;
import darkbook.analysis.CategoryClassifier;
import darkbook.analysis.TrendScoreEngine;
import darkbook.intelligence.KnowledgeBuilder;
import darkbook.intelligence.ObsidianManager;
import darkbook.intelligence.KnowledgeNode;


public class DataCollector {
    private final KnowledgeBuilder knowledgeBuilder =
            new KnowledgeBuilder();

    private final ObsidianManager obsidian =
            new ObsidianManager();
    private final VideoExtractor extractor;
    private final MetricsExtractor metrics;
    private final ScreenshotManager screenshots;
    private final TextExtractor hashtags;
    private final DataValidator validator;
    private final VideoRepository videoRepository = new VideoRepository();
    private final AudioRepository audioRepository = new AudioRepository();
    private final CategoryClassifier classifier =
            new CategoryClassifier();

    private final TrendScoreEngine scoreEngine =
            new TrendScoreEngine();

    public DataCollector(Page page){


        extractor = new VideoExtractor(page);
        metrics = new MetricsExtractor(page);
        screenshots = new ScreenshotManager(page);
        hashtags = new TextExtractor();
        validator = new DataValidator();

    }

    public VideoData collect(){

        VideoData video = extractor.extract();

        metrics.extractMetrics(video);

        hashtags.extractHashtags(video);

        video.setCategory(classifier.classify(video));

        int trendScore = scoreEngine.calculate(video);

        System.out.println("Trend Score: " + trendScore);

        video.setScreenshotPath(
                screenshots.captureVideo()
        );

        if(!validator.validate(video)){

            System.out.println("Video descartado.");

            return null;

        }

        videoRepository.save(video);

        audioRepository.save(video.getAudioName());

        KnowledgeNode node = knowledgeBuilder.build(video);

        obsidian.saveKnowledge(node);

        printSummary(video);

        return video;
    }

    private void printSummary(VideoData video){

        System.out.println("----------------------------");
        System.out.println("Nuevo video capturado");
        System.out.println("----------------------------");

        System.out.println("Usuario: " + video.getUsername());

        System.out.println("Descripción: " + video.getDescription());

        System.out.println("Likes: " + video.getLikes());

        System.out.println("Comentarios: " + video.getComments());

        System.out.println("Compartidos: " + video.getShares());

        System.out.println("Audio: " + video.getAudioName());

        System.out.println("Hashtags: " + video.getHashtags());

        System.out.println("Screenshot: " + video.getScreenshotPath());

    }


}
