package darkbook.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;

public class SessionRepository {

    public void startSession(){

        insert(LocalDateTime.now(), null,0);

    }

    public void finishSession(int scanned){

        insert(null, LocalDateTime.now(), scanned);

    }

    private void insert(LocalDateTime start,
                        LocalDateTime finish,
                        int scanned){

        try{

            Connection connection =
                    DatabaseManager.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement("""
                            INSERT INTO sessions(
                            started_at,
                            finished_at,
                            scanned
                            )
                            VALUES(?,?,?)
                            """);

            statement.setString(1,start==null?null:start.toString());
            statement.setString(2,finish==null?null:finish.toString());
            statement.setInt(3,scanned);

            statement.executeUpdate();

        }catch(Exception e){

            e.printStackTrace();

        }

    }

}