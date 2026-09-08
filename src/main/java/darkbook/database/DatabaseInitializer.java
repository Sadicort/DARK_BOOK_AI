package darkbook.database;

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize(){

        new File("database").mkdirs();

        Connection connection =
                DatabaseManager.getConnection();

        createVideosTable(connection);

        createHashtagsTable(connection);

        createAudiosTable(connection);

        createStatisticsTable(connection);

        createSessionsTable(connection);

    }

    private static void execute(Connection connection, String sql){

        try(Statement statement = connection.createStatement()){

            statement.execute(sql);

        }catch(Exception e){

            throw new RuntimeException(e);

        }

    }

    private static void createVideosTable(Connection connection){

        execute(connection, """
                CREATE TABLE IF NOT EXISTS videos(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    url TEXT UNIQUE,
                    username TEXT,
                    display_name TEXT,
                    description TEXT,
                    likes TEXT,
                    comments TEXT,
                    shares TEXT,
                    favorites TEXT,
                    audio TEXT,
                    screenshot TEXT,
                    watch_time INTEGER,
                    category TEXT,
                    collected_at TEXT
                );
                """);

    }

    private static void createHashtagsTable(Connection connection){

        execute(connection, """
                CREATE TABLE IF NOT EXISTS hashtags(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    video_id INTEGER,
                    hashtag TEXT
                );
                """);

    }

    private static void createAudiosTable(Connection connection){

        execute(connection, """
                CREATE TABLE IF NOT EXISTS audios(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT UNIQUE,
                    occurrences INTEGER DEFAULT 1
                );
                """);

    }

    private static void createStatisticsTable(Connection connection){

        execute(connection, """
                CREATE TABLE IF NOT EXISTS statistics(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    videos INTEGER,
                    duplicates INTEGER,
                    ads INTEGER,
                    execution_time INTEGER,
                    created_at TEXT
                );
                """);

    }

    private static void createSessionsTable(Connection connection){

        execute(connection, """
                CREATE TABLE IF NOT EXISTS sessions(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    started_at TEXT,
                    finished_at TEXT,
                    scanned INTEGER
                );
                """);

    }

}