package darkbook.analysis;

import darkbook.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class HashtagAnalyzer {

    public Map<String,Integer> topHashtags(int limit){

        Map<String,Integer> hashtags =
                new LinkedHashMap<>();

        try{

            Connection connection =
                    DatabaseManager.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement("""
                            SELECT hashtag,
                            COUNT(*) total
                            FROM hashtags
                            GROUP BY hashtag
                            ORDER BY total DESC
                            LIMIT ?
                            """);

            statement.setInt(1, limit);

            ResultSet rs = statement.executeQuery();

            while(rs.next()){

                hashtags.put(
                        rs.getString("hashtag"),
                        rs.getInt("total")
                );

            }

        }catch(Exception e){

            e.printStackTrace();

        }

        return hashtags;

    }

}