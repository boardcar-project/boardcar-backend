package database;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MemberVO {

    private final String mid;
    private final String pw;
    private final String name;
    private final String email;

    public MemberVO(ResultSet resultSet) throws SQLException {
        this.mid = resultSet.getString("mid");
        this.pw = resultSet.getString("pw");
        this.name = resultSet.getString("name");
        this.email = resultSet.getString("email");
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
