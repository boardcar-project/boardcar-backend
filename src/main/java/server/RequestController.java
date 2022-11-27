package server;

import database.MemberDAO;
import database.MemberVO;
import http.HttpRequest;
import http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class RequestController {
    public static Map<String, String> sessionContext = new HashMap<>();
    public static Map<String, String> headers = new HashMap<String, String>(){
        {
            put("Server", "boardcar-server");
        }
    };

    public static Function<HttpRequest, HttpResponse> httpTest = request -> {
        return HttpResponse.ok(headers, "httpTest Success");
    };
    public static Function<HttpRequest, HttpResponse> login = request -> {

        // HTTP request에서 body의 JSON을 parse
        String id, password;
        try {
            JSONObject jsonObject = new JSONObject(request.getBody());
            id = jsonObject.getString("id");
            password = jsonObject.getString("password");
        } catch (JSONException e) {
            // JSON가 잘못 되었을 때
            e.printStackTrace();
            return HttpResponse.badRequest(headers, "Invalid body (JSON format)");
        }

        // DB에서 회원 ID 찾기
        MemberDAO memberDAO = new MemberDAO();
        try {
            MemberVO requestMember = memberDAO.getMemberById(id);

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

    public static Function<HttpRequest, HttpResponse> member = request -> {

        // 세션 체크
        String sessionKey = request.getHeaders().get("Session-Key");
        if (!sessionContext.containsKey(sessionKey) || sessionKey == null) {
            return HttpResponse.badRequest(headers,"please login before access DB");
        }

        // DB에서 member 정보 가져오기
        MemberDAO memberDAO = new MemberDAO();
        List<MemberVO> memberVOList = memberDAO.getMemberVOList();

        // member 정보를 가진 body 만들기
        StringBuilder bodyBuilder = new StringBuilder();
        for (MemberVO memberVO : memberVOList) {
            bodyBuilder.append(memberVO.toString()).append("\n");
        }

        return HttpResponse.ok(headers, bodyBuilder.toString());
    };

    public static Function<HttpRequest, HttpResponse> other = request -> {
        return HttpResponse.notFound(headers, "Wrong API access");
    };
}
