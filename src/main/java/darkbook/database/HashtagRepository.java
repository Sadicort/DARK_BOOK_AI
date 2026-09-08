package darkbook.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

public class HashtagRepository {

    public void save(int videoId, List<String> hashtags){

        String sql =
                "INSERT INTO hashtags(video_id, hashtag) VALUES(?,?)";

        try{

            Connection connection =
                    DatabaseManager.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            for(String hashtag : hashtags){

                statement.setInt(1, videoId);

                statement.setString(2, hashtag);

                statement.addBatch();

            }

            statement.executeBatch();

        }catch(Exception e){

            e.printStackTrace();

        }

    }

}