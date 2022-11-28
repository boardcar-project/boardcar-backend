package database;

import org.json.JSONObject;
import server.HttpServer;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PostDAO {

    public int INSERT_uploadPost(JSONObject jsonObject) throws SQLException {

        // JSON parse
        String MID = jsonObject.getString("MID");
        String PDATE = jsonObject.getString("PDATE");
        String TITLE = jsonObject.getString("TITLE");
        String BODY = jsonObject.getString("BODY");
        String TYPE = jsonObject.getString("TYPE");

        // SQL query 생성
        PreparedStatement sqlQuery = HttpServer.getDatabaseConnection().prepareStatement("INSERT INTO POST (PID, MID, PDATE, TITLE, BODY, TYPE) VALUES (PCOUNTER.nextval, ?,to_date(?, 'YYYY-MM-DD'),?,?,?)");

        sqlQuery.setString(1, MID);
        sqlQuery.setString(2, PDATE);
        sqlQuery.setString(3, TITLE);
        sqlQuery.setString(4, BODY);
        sqlQuery.setString(5, TYPE);

        return sqlQuery.executeUpdate(); // SQL 실행 후 업데이트 된 레코드 수 반환
    }



}
