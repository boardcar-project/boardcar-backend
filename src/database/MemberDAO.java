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

    public MemberVO getMember(String mid) {

        PreparedStatement sqlQuery;
        try {
            sqlQuery = this.connection.prepareStatement("select * from member where mid = ?");
            sqlQuery.setString(1, mid);

            ResultSet resultSet = sqlQuery.executeQuery();
            resultSet.next();

            return new MemberVO(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    public List<MemberVO> getMemberVOList() {

        List<MemberVO> memberVOList = new LinkedList<>();

        try {
            PreparedStatement sqlQuery = this.connection.prepareStatement("select * from member");

            ResultSet resultSet = sqlQuery.executeQuery();
            while (resultSet.next()) {
                memberVOList.add(new MemberVO(resultSet));
            }

            return memberVOList;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }
}
