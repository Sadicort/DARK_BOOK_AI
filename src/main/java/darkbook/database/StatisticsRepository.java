package darkbook.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;

public class StatisticsRepository {

    public void saveSession(int videos,
                            int duplicates,
                            int ads,
                            int seconds){

        try{

            Connection connection =
                    DatabaseManager.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement("""
                            INSERT INTO statistics(
                            videos,
                            duplicates,
                            ads,
                            execution_time,
                            created_at
                            )
                            VALUES(?,?,?,?,?)
                            """);

            statement.setInt(1, videos);
            statement.setInt(2, duplicates);
            statement.setInt(3, ads);
            statement.setInt(4, seconds);
            statement.setString(5, LocalDateTime.now().toString());

            statement.executeUpdate();

        }catch(Exception e){

            e.printStackTrace();

        }

    }

}