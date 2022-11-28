package database;

import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PostVO {

    private final int PID;
    private final String MID;
    private final String TITLE;
    private final String BODY;
    private final int UPVOTE;
    private final int DOWNVOTE;
    private final String TYPE;
    private final String PDATE;

    public PostVO(ResultSet resultSet) throws SQLException {
        this.PID = resultSet.getInt("PID");
        this.MID = resultSet.getString("MID");
        this.TITLE = resultSet.getString("TITLE");
        this.BODY = resultSet.getString("BODY");
        this.UPVOTE = resultSet.getInt("UPVOTE");
        this.DOWNVOTE = resultSet.getInt("DOWNVOTE");
        this.TYPE = resultSet.getString("TYPE");
        this.PDATE = resultSet.getString("PDATE");
    }

    @Override
    public String toString() {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("PID", PID);
        jsonObject.put("MID", MID);
        jsonObject.put("TITLE", TITLE);
        jsonObject.put("BODY", BODY);
        jsonObject.put("UPVOTE", UPVOTE);
        jsonObject.put("DOWNVOTE", DOWNVOTE);
        jsonObject.put("TYPE", TYPE);
        jsonObject.put("PDATE", PDATE);

        return jsonObject.toString();
    }

}
