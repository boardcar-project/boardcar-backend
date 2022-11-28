package database;

import org.json.JSONObject;
import server.HttpServer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class PostDAO {

    public int INSERT_post(JSONObject jsonObject) throws SQLException {

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

        // SQL query 실행
        return sqlQuery.executeUpdate(); // INSERT 된 레코드 수 반환
    }

    public List<PostVO> SELECT_postList(JSONObject jsonObject) throws SQLException {

        // JSON parse
        String type = jsonObject.getString("TYPE");

        // 게시글 리스트 생성
        List<PostVO> postVOList = new LinkedList<>();

        // SQL query 생성
        PreparedStatement sqlQuery = HttpServer.getDatabaseConnection().prepareStatement("SELECT * FROM POST WHERE TYPE=?");
        sqlQuery.setString(1, type);

        // SQL query 실행
        ResultSet resultSet = sqlQuery.executeQuery();
        while (resultSet.next()){
            postVOList.add(new PostVO(resultSet));
        }

        return postVOList; // 게시글 리스트 반환
    }

    public PostVO SELECT_postByPid(JSONObject jsonObject) throws SQLException {

        // JSON parse
        int PID = jsonObject.getInt("PID");

        // SQL query 생성
        PreparedStatement sqlQuery = HttpServer.getDatabaseConnection().prepareStatement("SELECT * FROM POST WHERE PID = ?");
        sqlQuery.setInt(1, PID);

        // SQL query 실행
        ResultSet resultSet = sqlQuery.executeQuery();
        resultSet.next();

        return new PostVO(resultSet); // 게시글 반환
    }



}
