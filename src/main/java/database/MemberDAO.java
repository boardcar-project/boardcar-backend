package database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class MemberDAO {

    private final Connection connection;

    public MemberDAO() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            this.connection = DatabaseConnection.getDatabaseConnection();
        } catch (IOException | SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public MemberVO getMemberById(String MID) throws SQLException {

        PreparedStatement sqlQuery;

        sqlQuery = this.connection.prepareStatement("SELECT * FROM MEMBER WHERE MID = ?");
        sqlQuery.setString(1, MID);

        ResultSet resultSet = sqlQuery.executeQuery();
        resultSet.next();

        return new MemberVO(resultSet);


    }

    public List<MemberVO> getMemberVOList() throws SQLException {

        List<MemberVO> memberVOList = new LinkedList<>();

        PreparedStatement sqlQuery = this.connection.prepareStatement("SELECT * FROM MEMBER");

        ResultSet resultSet = sqlQuery.executeQuery();
        while (resultSet.next()) {
            memberVOList.add(new MemberVO(resultSet));
        }

        return memberVOList;

    }

    public int updateMemberPassword(String MID, String PASSWORD) throws SQLException {

        PreparedStatement sqlQuery = this.connection.prepareStatement("UPDATE MEMBER SET PASSWORD=? WHERE MID=?");
        sqlQuery.setString(1, PASSWORD);
        sqlQuery.setString(2, MID);

        return sqlQuery.executeUpdate();
    }
}
