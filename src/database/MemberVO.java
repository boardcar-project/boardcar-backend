package database;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MemberVO {

    private final String mid;
    private final String pw;
    private final String name;
    private final String email;

    public MemberVO(ResultSet resultSet) throws SQLException {
        this.mid = resultSet.getString("MID");
        this.pw = resultSet.getString("PASSWORD");
        this.name = resultSet.getString("MNAME");
        this.email = resultSet.getString("EMAIL");
    }

    @Override
    public String toString() {
        return "MemberVO{" +
                "mid='" + mid + '\'' +
                ", pw='" + pw + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
