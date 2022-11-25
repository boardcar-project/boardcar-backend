package database;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MemberVO {

    private final String MID;
    private final String PASSWORD;
    private final String MNAME;
    private final String EMAIL;

    public MemberVO(ResultSet resultSet) throws SQLException {
        this.MID = resultSet.getString("MID");
        this.PASSWORD = resultSet.getString("PASSWORD");
        this.MNAME = resultSet.getString("MNAME");
        this.EMAIL = resultSet.getString("EMAIL");
    }

    @Override
    public String toString() {
        return "MemberVO{" +
                "mid='" + MID + '\'' +
                ", pw='" + PASSWORD + '\'' +
                ", name='" + MNAME + '\'' +
                ", email='" + EMAIL + '\'' +
                '}';
    }

    public String getPassword() {
        return PASSWORD;
    }
}
