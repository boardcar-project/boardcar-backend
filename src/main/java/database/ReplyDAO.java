package database;

import org.json.JSONObject;
import server.HttpServer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class ReplyDAO {

    public int INSERT_reply(JSONObject jsonObject) throws SQLException {

        // JSON parse
        String MID = jsonObject.getString("MID");
        int PID = jsonObject.getInt("PID");
        String BODY = jsonObject.getString("BODY");

        // SQL query 생성
        PreparedStatement sqlQuery = HttpServer.getDatabaseConnection()
                .prepareStatement("INSERT INTO REPLY (RID, MID, BODY, PID) VALUES (RCOUNTER.nextval, ?, ?, ?)");
        sqlQuery.setString(1, MID);
        sqlQuery.setString(2, BODY);
        sqlQuery.setInt(3, PID);

        // SQL query 실행
        return sqlQuery.executeUpdate();
    }

    public int UPDATE_reply(JSONObject jsonObject) throws SQLException {

        // JSON parse
        String BODY = jsonObject.getString("BODY");
        int RID = jsonObject.getInt("RID");

        // SQL query 생성
        PreparedStatement sqlQuery = HttpServer.getDatabaseConnection()
                .prepareStatement("UPDATE  REPLY SET BODY=? WHERE RID=?");
        sqlQuery.setString(1, BODY);
        sqlQuery.setInt(2, RID);

        // SQL query 실행
        return sqlQuery.executeUpdate();

    }

    public int DELETE_reply(JSONObject jsonObject) throws SQLException {

        // JSON parse
        int RID = jsonObject.getInt("RID");

        // SQL query 생성
        PreparedStatement sqlQuery = HttpServer.getDatabaseConnection()
                .prepareStatement("DELETE  FROM REPLY WHERE RID=?");
        sqlQuery.setInt(1, RID);

        // SQL query 실행
        return sqlQuery.executeUpdate();
    }

    public List<ReplyVO> SELECE_replyList(JSONObject jsonObject) throws SQLException {

        // JSON parse
        int PID = jsonObject.getInt("PID");

        // 댓글 리스트 생성
        List<ReplyVO> replyVOList = new LinkedList<>();

        // SQL query 생성
        PreparedStatement sqlQuery = HttpServer.getDatabaseConnection()
                .prepareStatement("SELECT * FROM REPLY WHERE PID=?");
        sqlQuery.setInt(1, PID);

        // SQL query 실행
        ResultSet resultSet = sqlQuery.executeQuery();
        while (resultSet.next()) {
            replyVOList.add(new ReplyVO(resultSet));
        }

        return replyVOList;
    }


}
