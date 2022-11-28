package server;

import database.MemberDAO;
import database.MemberVO;
import database.PostDAO;
import database.PostVO;
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

    public static Map<String, String> sessionContext = new HashMap<>();
    public static Map<String, String> headers = new HashMap<String, String>() {
        {
            put("Server", "boardcar-server");
        }
    };

    public static Function<HttpRequest, HttpResponse> httpTest = request -> HttpResponse.ok(headers, "httpTest Success");
    public static Function<HttpRequest, HttpResponse> login = request -> {

        // HTTP request body에서 JSON parse
        String id, password;
        try {
            JSONObject jsonObject = new JSONObject(request.getBody());
            id = jsonObject.getString("id");
            password = jsonObject.getString("password");
        } catch (JSONException e) {
            // JSON이 잘못 되었을 때
            e.printStackTrace();
            return HttpResponse.badRequest(headers, "Invalid body (JSON format)");
        }

        // DB에서 회원 ID 찾기
        try {
            MemberVO requestMember = memberDAO.SELECT_memberByMid(id);

            // PW가 틀린 경우
            if (!requestMember.getPassword().equals(password)) {
                return HttpResponse.badRequest(headers, "login failed (mismatch PASSWORD)");
            }
        } catch (SQLException e) {
            // ID를 찾지 못했을 때
            e.printStackTrace();
            return HttpResponse.badRequest(headers, "login failed (not found ID)");
        }

        // 로그인 성공! -> 세션 생성
        UUID uuid = UUID.randomUUID();
        sessionContext.put(uuid.toString(), id);

        // 헤더에 추가
        headers.put("Session-Key", uuid.toString());

        return HttpResponse.ok(headers, "login success");
    };

    public static Function<HttpRequest, HttpResponse> myInfo = request -> {

        // 세션 체크
        String targetId;
        if ((targetId = getIdFromSessionContext(request)) == null) {
            return HttpResponse.badRequest(headers, "please login before access DB");
        }

        // DB에서 Member 레코드 가져와서 JSON 형식으로 body에 저장
        try {
            // SQL 실행
            MemberVO targetMember = memberDAO.SELECT_memberByMid(targetId);

            return HttpResponse.ok(headers, targetMember.toJSON());

        } catch (SQLException e) {
            return HttpResponse.badRequest(headers, e.toString());
        }

    };

    public static Function<HttpRequest, HttpResponse> members = request -> {

        // 세션 체크
        if (getIdFromSessionContext(request) == null) {
            return HttpResponse.badRequest(headers, "please login before access DB");
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

            return HttpResponse.ok(headers, jsonArray.toString());

        } catch (SQLException e) {
            return HttpResponse.badRequest(headers, e.toString());
        }
    };

    public static Function<HttpRequest, HttpResponse> changePassword = request -> {

        // 세션 체크
        String targetId;
        if ((targetId = getIdFromSessionContext(request)) == null) {
            return HttpResponse.badRequest(headers, "please login before access DB");
        }

        // 비밀번호 변경
        try {
            int sqlResult = memberDAO.UPDATE_memberPassword(targetId, new JSONObject(request.getBody()));

            return HttpResponse.ok(headers, "Password is changed successfully (Changed record : " + sqlResult + ")");
        } catch (SQLException e) {
            return HttpResponse.badRequest(headers, e.toString());
        }
    };

    public static Function<HttpRequest, HttpResponse> uploadPost = request -> {

        // 세션 체크
        if (getIdFromSessionContext(request) == null) {
            return HttpResponse.badRequest(headers, "please login before access DB");
        }

        // 게시글 업로드
        try {
            int sqlResult = postDAO.INSERT_post(new JSONObject(request.getBody()));

            return HttpResponse.ok(headers, "Post is uploaded successfully (Inserted record : " + sqlResult + ")");

        } catch (SQLException e) {
            return HttpResponse.badRequest(headers, e.toString());
        }

    };

    public static Function<HttpRequest, HttpResponse> openPostList = request -> {

        // 세션 체크
        if (getIdFromSessionContext(request) == null) {
            return HttpResponse.badRequest(headers, "please login before access DB");
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

            return HttpResponse.ok(headers, jsonArray.toString());

        } catch (SQLException e) {
            return HttpResponse.badRequest(headers, e.toString());
        }
    };

    public static Function<HttpRequest, HttpResponse> openPost = request -> {

        // 세션 체크
        if (getIdFromSessionContext(request) == null) {
            return HttpResponse.badRequest(headers, "please login before access DB");
        }

        // DB에서 PID로 게시글 가져오기
        try {
            PostVO postVO = postDAO.SELECT_postByPid(new JSONObject(request.getBody()));

            return HttpResponse.ok(headers, postVO.toJSON());

        } catch (SQLException e) {
            return HttpResponse.badRequest(headers, e.toString());
        }
    };


    public static Function<HttpRequest, HttpResponse> other = request -> HttpResponse.notFound(headers, "Wrong API access");

    public static String getIdFromSessionContext(HttpRequest request) {
        String sessionKey = request.getHeaders().getOrDefault("Session-Key", null);

        return sessionContext.getOrDefault(sessionKey, null);
    }


}
