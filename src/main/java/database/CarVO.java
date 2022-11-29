package database;

import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CarVO {

    private final int CID;
    private final String NAME;

    public CarVO(ResultSet resultSet) throws SQLException {
        this.CID = resultSet.getInt("CID");
        this.NAME = resultSet.getString("NAME");
    }

    public String toJSON() {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("CID", CID);
        jsonObject.put("NAME", NAME);

        return jsonObject.toString();
    }

    public int getCID() {
        return CID;
    }

    public String getNAME() {
        return NAME;
    }
}
