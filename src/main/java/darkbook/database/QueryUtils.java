package darkbook.database;

public class QueryUtils {

    public static final String COUNT_VIDEOS =
            "SELECT COUNT(*) FROM videos";

    public static final String COUNT_HASHTAGS =
            "SELECT COUNT(*) FROM hashtags";

    public static final String TOP_AUDIOS =
            """
            SELECT name, occurrences
            FROM audios
            ORDER BY occurrences DESC
            LIMIT 10
            """;

}
