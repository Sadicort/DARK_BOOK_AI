package darkbook.analysis;

import darkbook.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class AudioAnalyzer {

    public Map<String,Integer> topAudios(int limit){

        Map<String,Integer> audios =
                new LinkedHashMap<>();

        try{

            Connection connection =
                    DatabaseManager.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement("""
                            SELECT name,
                            occurrences
                            FROM audios
                            ORDER BY occurrences DESC
                            LIMIT ?
                            """);

            statement.setInt(1, limit);

            ResultSet rs = statement.executeQuery();

            while(rs.next()){

                audios.put(
                        rs.getString("name"),
                        rs.getInt("occurrences")
                );

            }

        }catch(Exception e){

            e.printStackTrace();

        }

        return audios;

    }

}