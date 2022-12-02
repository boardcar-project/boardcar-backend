package database;

import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ReplyVO {

    private final int RID;
    private final String MID;
    private final int PID;
    private final String BODY;

    public ReplyVO(ResultSet resultSet) throws SQLException {
        this.RID = resultSet.getInt("RID");
        this.MID = resultSet.getString("MID");
        this.PID = resultSet.getInt("PID");
        this.BODY = resultSet.getString("BODY");
    }


    public String toJSON() {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("RID", RID);
        jsonObject.put("PID", PID);
        jsonObject.put("MID", MID);
        jsonObject.put("BODY", BODY);

        return jsonObject.toString();
    }
}
