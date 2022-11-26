package database;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    public static Connection getDatabaseConnection() throws IOException, SQLException {
        // DB 정보 읽기
        String DB_URL, DB_ID, DB_PW;

        Properties properties = new Properties();
        properties.load(new FileInputStream(".properties"));

        DB_URL = properties.getProperty("DB_URL");
        DB_ID = properties.getProperty("DB_ID");
        DB_PW = properties.getProperty("DB_PW");

        // DB 연결
        return DriverManager.getConnection(DB_URL, DB_ID, DB_PW);
    }
}
