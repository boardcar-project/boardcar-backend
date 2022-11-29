package server;

import database.*;
import http.HttpRequest;
import http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class RequestController {

    private static final MemberDAO memberDAO = new MemberDAO();
    private static final PostDAO postDAO = new PostDAO();
    private static final CarDAO carDAO = new CarDAO();

    public static final Map<String, String> sessionContext = new HashMap<>();
    public static final Map<String, String> serverDefaultHeaders = new HashMap<String, String>() {
        {
            put("Server", "boardcar-server");
            put("Content-Type", "application/json");

        }
    };

    public static final Function<HttpRequest, HttpResponse> httpTest = request -> HttpResponse.ok(serverDefaultHeaders, "httpTest Success");
    public static final Function<HttpRequest, HttpResponse> login = request -> {

        // HTTP request body에서 JSON parse
        String id, password;
        try {
            JSONObject jsonObject = new JSONObject(request.getBody());
            id = jsonObject.getString("id");
            password = jsonObject.getString("password");
        } catch (JSONException e) {
            // JSON이 잘못 되었을 때
            e.printStackTrace();
            return HttpResponse.badRequest(serverDefaultHeaders, "Invalid body (JSON format)");
        }

        // DB에서 회원 ID 찾기
        try {
            MemberVO requestMember = memberDAO.SELECT_memberByMid(id);

            // PW가 틀린 경우
            if (!requestMember.getPassword().equals(password)) {
                return HttpResponse.badRequest(serverDefaultHeaders, "login failed (mismatch PASSWORD)");
            }
        } catch (SQLException e) {
            // ID를 찾지 못했을 때
            e.printStackTrace();
            return HttpResponse.badRequest(serverDefaultHeaders, "login failed (not found ID)");
        }

        // 로그인 성공! -> 세션 생성
        UUID uuid = UUID.randomUUID();
        sessionContext.put(uuid.toString(), id);

        // 헤더에 추가
        HttpResponse httpResponse = HttpResponse.ok(serverDefaultHeaders, "login success");
        httpResponse.putHeader("Session-Key", uuid.toString());

        return httpResponse;
    };

    public static final Function<HttpRequest, HttpResponse> myInfo = request -> {

        // 세션 체크
        String targetId;
        if ((targetId = getIdFromSessionContext(request)) == null) {
            return HttpResponse.badRequest(serverDefaultHeaders, "please login before access DB");
        }

        // DB에서 Member 레코드 가져와서 JSON 형식으로 body에 저장
        try {
            // SQL 실행
            MemberVO targetMember = memberDAO.SELECT_memberByMid(targetId);

            return HttpResponse.ok(serverDefaultHeaders, targetMember.toJSON());

        } catch (SQLException e) {
            return HttpResponse.badRequest(serverDefaultHeaders, e.toString());
        }

    };

    public static final Function<HttpRequest, HttpResponse> members = request -> {

        // 세션 체크
        if (getIdFromSessionContext(request) == null) {
            return HttpResponse.badRequest(serverDefaultHeaders, "please login before access DB");
        }

        // DB에서 member 정보 가져오기
        try {
            // SQL 실행
            List<MemberVO> memberVOList = memberDAO.SELECT_memberList();

            // MemberVO를 JSONObject로 만들어 JSONArray에 저장
            JSONArray jsonArray = new JSONArray();
            for (MemberVO memberVO : memberVOList) {
                jsonArray.put(memberVO.toJSON());
            }

            return HttpResponse.ok(serverDefaultHeaders, jsonArray.toString());

        } catch (SQLException e) {
            return HttpResponse.badRequest(serverDefaultHeaders, e.toString());
        }
    };

    public static final Function<HttpRequest, HttpResponse> changePassword = request -> {

        // 세션 체크
        String targetId;
        if ((targetId = getIdFromSessionContext(request)) == null) {
            return HttpResponse.badRequest(serverDefaultHeaders, "please login before access DB");
        }

        // 비밀번호 변경
        try {
            int sqlResult = memberDAO.UPDATE_memberPassword(targetId, new JSONObject(request.getBody()));

            return HttpResponse.ok(serverDefaultHeaders, "Password is changed successfully (Changed record : " + sqlResult + ")");
        } catch (SQLException e) {
            return HttpResponse.badRequest(serverDefaultHeaders, e.toString());
        }
    };

    public static final Function<HttpRequest, HttpResponse> uploadPost = request -> {

        // 세션 체크
        if (getIdFromSessionContext(request) == null) {
            return HttpResponse.badRequest(serverDefaultHeaders, "please login before access DB");
        }

        // 게시글 업로드
        try {
            int sqlResult = postDAO.INSERT_post(new JSONObject(request.getBody()));

            return HttpResponse.ok(serverDefaultHeaders, "Post is uploaded successfully (Inserted record : " + sqlResult + ")");

        } catch (SQLException e) {
            return HttpResponse.badRequest(serverDefaultHeaders, e.toString());
        }

    };

    public static final Function<HttpRequest, HttpResponse> openPostList = request -> {

        // 세션 체크
        if (getIdFromSessionContext(request) == null) {
            return HttpResponse.badRequest(serverDefaultHeaders, "please login before access DB");
        }

        // DB에서 게시글 리스트 가져오기
        try {
            // SQL 실행
            List<PostVO> postVOList = postDAO.SELECT_postList(new JSONObject(request.getBody()));

            // PostVO를 JSONObject로 만들어 JSONArray에 저장
            JSONArray jsonArray = new JSONArray();
            for (PostVO postVO : postVOList) {
                jsonArray.put(postVO.toJSON());
            }

            return HttpResponse.ok(serverDefaultHeaders, jsonArray.toString());

        } catch (SQLException e) {
            return HttpResponse.badRequest(serverDefaultHeaders, e.toString());
        }
    };

    public static final Function<HttpRequest, HttpResponse> openPost = request -> {

        // 세션 체크
        if (getIdFromSessionContext(request) == null) {
            return HttpResponse.badRequest(serverDefaultHeaders, "please login before access DB");
        }

        // DB에서 PID로 게시글 가져오기
        try {
            PostVO postVO = postDAO.SELECT_postByPid(new JSONObject(request.getBody()));

            return HttpResponse.ok(serverDefaultHeaders, postVO.toJSON());

        } catch (SQLException e) {
            return HttpResponse.badRequest(serverDefaultHeaders, e.toString());
        }
    };

    public static final Function<HttpRequest, HttpResponse> updatePost = request -> {

        // 세션 체크
        if (getIdFromSessionContext(request) == null) {
            return HttpResponse.badRequest(serverDefaultHeaders, "please login before access DB");
        }

        // 게시글 내용 변경
        try {
            int sqlResult = postDAO.UPDATE_post(new JSONObject(request.getBody()));

            return HttpResponse.ok(serverDefaultHeaders, "Post body is changed successfully (Changed record : " + sqlResult + ")");
        } catch (SQLException e) {
            return HttpResponse.badRequest(serverDefaultHeaders, e.toString());
        }

    };

    public static final Function<HttpRequest, HttpResponse> deletePost = request -> {

        // 세션 체크
        if (getIdFromSessionContext(request) == null) {
            return HttpResponse.badRequest(serverDefaultHeaders, "please login before access DB");
        }

        // 게시글 삭제
        try {
            int sqlResult = postDAO.DELETE_post(new JSONObject(request.getBody()));

            return HttpResponse.ok(serverDefaultHeaders, "Post is deleted successfully (Changed record : " + sqlResult + ")");
        } catch (SQLException e) {
            return HttpResponse.badRequest(serverDefaultHeaders, e.toString());
        }

    };

    public static final Function<HttpRequest, HttpResponse> getCarByCid = request -> {

        // 차량 레코드 가져오기
        try {
            CarVO carVO = carDAO.SELECT_car(new JSONObject(request.getBody()));

            return HttpResponse.ok(serverDefaultHeaders, carVO.toJSON());
        } catch (SQLException e) {
            return HttpResponse.badRequest(serverDefaultHeaders, e.toString());
        }

    };

    public static final Function<HttpRequest, HttpResponse> getCarList = request -> {

        // 차량 레코드 가져오기
        try {
            List<CarVO> carVOList = carDAO.SELECT_carList();

            JSONArray jsonArray = new JSONArray();
            for(CarVO carVO : carVOList){
                jsonArray.put(carVO.toJSON());
            }

            return HttpResponse.ok(serverDefaultHeaders, jsonArray.toString());
        } catch (SQLException e) {
            return HttpResponse.badRequest(serverDefaultHeaders, e.toString());
        }

    };


    public static final Function<HttpRequest, HttpResponse> other = request -> HttpResponse.notFound(serverDefaultHeaders, "Wrong API access");

    public static String getIdFromSessionContext(HttpRequest request) {
        String sessionKey = request.getHeaders().getOrDefault("Session-Key", null);

        return sessionContext.getOrDefault(sessionKey, null);
    }


}
