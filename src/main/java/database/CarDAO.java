package database;

import org.json.JSONObject;
import server.HttpServer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class CarDAO {

    public List<CarVO> SELECT_carList() throws SQLException {

        // 차량 리스트 생성
        List<CarVO> carVOList = new LinkedList<>();

        // SQL query 생성
        PreparedStatement sqlQuery = HttpServer.getDatabaseConnection().prepareStatement("SELECT * FROM CAR");
        // SQL query 실행
        ResultSet resultSet = sqlQuery.executeQuery();
        while(resultSet.next()){
            carVOList.add(new CarVO(resultSet));
        }

        return carVOList; // 차 리스트 반환
    }

    public CarVO SELECT_car(JSONObject jsonObject) throws SQLException {

        // JSON parse
        int CID = jsonObject.getInt("CID");

        // SQL query 생성
        PreparedStatement sqlQuery = HttpServer.getDatabaseConnection().prepareStatement("SELECT * FROM CAR WHERE CID = ?");
        sqlQuery.setInt(1, CID);

        // SQL query 실행
        ResultSet resultSet = sqlQuery.executeQuery();
        resultSet.next();

        return new CarVO(resultSet); // 차 반환
    }



}
