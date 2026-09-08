package darkbook.database;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class AudioRepository {

    public void save(String audio){

        if(audio == null)
            return;

        try{

            Connection connection =
                    DatabaseManager.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement("""
                            INSERT INTO audios(name)
                            VALUES(?)
                            ON CONFLICT(name)
                            DO UPDATE SET occurrences = occurrences + 1
                            """);

            statement.setString(1, audio);

            statement.executeUpdate();

        }catch(Exception e){

            e.printStackTrace();

        }

    }

}