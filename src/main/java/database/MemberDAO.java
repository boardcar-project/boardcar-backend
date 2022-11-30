package database;

import oracle.jdbc.proxy.annotation.Pre;
import org.json.JSONObject;
import server.HttpServer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class MemberDAO {

    public List<MemberVO> SELECT_memberList() throws SQLException {

        // 멤버 리스트 생성
        List<MemberVO> memberVOList = new LinkedList<>();

        // SQL query 생성
        PreparedStatement sqlQuery = HttpServer.getDatabaseConnection().prepareStatement("SELECT * FROM MEMBER");

        ResultSet resultSet = sqlQuery.executeQuery();
        while (resultSet.next()) {
            memberVOList.add(new MemberVO(resultSet));
        }

        return memberVOList; // 멤버 리스트 반환
    }

    public MemberVO SELECT_memberByMid(String MID) throws SQLException {

        // SQL query 생성
        PreparedStatement sqlQuery = HttpServer.getDatabaseConnection().prepareStatement("SELECT * FROM MEMBER WHERE MID = ?");
        sqlQuery.setString(1, MID);

        // SQL query 실행
        ResultSet resultSet = sqlQuery.executeQuery();
        resultSet.next();

        return new MemberVO(resultSet); // 멤버 반환
    }

    public int UPDATE_memberPassword(String MID, JSONObject jsonObject) throws SQLException {

        // JSON parse
        String newPassword = jsonObject.getString("password");

        // SQL 생성
        PreparedStatement sqlQuery = HttpServer.getDatabaseConnection().prepareStatement("UPDATE MEMBER SET PASSWORD=? WHERE MID=?");
        sqlQuery.setString(1, newPassword);
        sqlQuery.setString(2, MID);

        // SQL query 실행
        return sqlQuery.executeUpdate(); // UPDATE 된 레코드 수 반환
    }

    public int INSERT_register(JSONObject jsonObject) throws SQLException {

        // JSON parse
        String MID = jsonObject.getString("MID");
        String PASSWORD = jsonObject.getString("PASSWORD");
        String NAME = jsonObject.getString("NAME");
        String EMAIL = jsonObject.getString("EMAIL");
        String CNAME = jsonObject.getString("CNAME");

        // SQL 생성
        PreparedStatement sqlQuery = HttpServer.getDatabaseConnection()
                .prepareStatement("INSERT INTO MEMBER(MID, PASSWORD, NAME, EMAIL, CID)" +
                        " VALUES (?, ?, ?, ?, (SELECT CID FROM CAR WHERE NAME=?))");
        sqlQuery.setString(1, MID);
        sqlQuery.setString(2, PASSWORD);
        sqlQuery.setString(3, NAME);
        sqlQuery.setString(4, EMAIL);
        sqlQuery.setString(5, CNAME);

        // SQL query 실행
        return sqlQuery.executeUpdate();
    }
}
