package darkbook.intelligence;

import darkbook.models.VideoData;

public class KnowledgeBuilder {

    private final CaptionCleaner cleaner =
            new CaptionCleaner();

    private final KeywordExtractor keywords =
            new KeywordExtractor();

    private final TopicDetector topics =
            new TopicDetector();

    private final EmotionClassifier emotions =
            new EmotionClassifier();

    public KnowledgeNode build(VideoData video){

        String clean =
                cleaner.clean(video.getDescription());

        KnowledgeNode node = new KnowledgeNode();

        node.title =
                clean.length() > 40
                        ? clean.substring(0,40)
                        : clean;

        node.description = clean;

        node.keywords = keywords.extract(clean);

        node.topics = topics.detectTopics(node.keywords);

        node.emotion = emotions.detect(clean);

        node.sourceUrl = video.getVideoUrl();

        return node;

    }

}