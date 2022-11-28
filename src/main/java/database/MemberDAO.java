package database;

import org.json.JSONObject;
import server.HttpServer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class MemberDAO {

    public MemberVO SELECT_memberById(String MID) throws SQLException {

        PreparedStatement sqlQuery;

        sqlQuery = HttpServer.getDatabaseConnection().prepareStatement("SELECT * FROM MEMBER WHERE MID = ?");
        sqlQuery.setString(1, MID);

        ResultSet resultSet = sqlQuery.executeQuery();
        resultSet.next();

        return new MemberVO(resultSet);
    }

    public List<MemberVO> SELECT_memberList() throws SQLException {

        List<MemberVO> memberVOList = new LinkedList<>();

        PreparedStatement sqlQuery = HttpServer.getDatabaseConnection().prepareStatement("SELECT * FROM MEMBER");

        ResultSet resultSet = sqlQuery.executeQuery();
        while (resultSet.next()) {
            memberVOList.add(new MemberVO(resultSet));
        }

        return memberVOList;
    }

    public int UPDATE_memberPassword(String MID, JSONObject jsonObject) throws SQLException {

        // body JSON에서 비밀번호 추출
        String newPassword = jsonObject.getString("password");

        // SQL 생성
        PreparedStatement sqlQuery = HttpServer.getDatabaseConnection().prepareStatement("UPDATE MEMBER SET PASSWORD=? WHERE MID=?");
        sqlQuery.setString(1, newPassword);
        sqlQuery.setString(2, MID);

        return sqlQuery.executeUpdate();
    }
}
