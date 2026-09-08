package darkbook.database;

import darkbook.models.VideoData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SearchRepository {

    public List<VideoData> searchByHashtag(String hashtag){

        List<VideoData> videos = new ArrayList<>();

        try{

            Connection connection =
                    DatabaseManager.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement("""
                            SELECT v.*
                            FROM videos v
                            JOIN hashtags h
                            ON v.id=h.video_id
                            WHERE h.hashtag=?
                            """);

            statement.setString(1, hashtag);

            ResultSet rs = statement.executeQuery();

            while(rs.next()){

                VideoData video = new VideoData();

                video.setUsername(rs.getString("username"));
                video.setDescription(rs.getString("description"));
                video.setLikes(rs.getString("likes"));
                video.setVideoUrl(rs.getString("url"));

                videos.add(video);

            }

        }catch(Exception e){

            e.printStackTrace();

        }

        return videos;

    }

}