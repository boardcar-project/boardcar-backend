package database;

import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MemberVO {

    private final String MID;
    private final String PASSWORD;
    private final String NAME;
    private final String EMAIL;

    public MemberVO(ResultSet resultSet) throws SQLException {
        this.MID = resultSet.getString("MID");
        this.PASSWORD = resultSet.getString("PASSWORD");
        this.NAME = resultSet.getString("NAME");
        this.EMAIL = resultSet.getString("EMAIL");
    }

    public String toJSON() {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("MID", MID);
        jsonObject.put("PASSWORD", PASSWORD);
        jsonObject.put("NAME", NAME);
        jsonObject.put("EMAIL", EMAIL);

        return jsonObject.toString();
    }

    public String getPassword() {
        return PASSWORD;
    }
}
