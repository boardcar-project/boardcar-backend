package server;

import database.MemberDAO;
import database.MemberVO;
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


    public static Function<HttpRequest, HttpResponse> httpTest = request ->{

        return HttpResponse.builder()
                .statusCode("200")
                .statusText("OK")
                .body("httpTest Success")
                .build();
    };
    public static Function<HttpRequest, HttpResponse> login = request -> {

        // HTTP request에서 body의 JSON을 parse
        String id, password;
        try {
            JSONObject jsonObject = new JSONObject(request.body);
            id = jsonObject.getString("id");
            password = jsonObject.getString("password");
        } catch (JSONException e) {
            // body가 잘못 되었을 때
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        // DB에서 회원 ID 찾기
        MemberDAO memberDAO = new MemberDAO();
        try {
            MemberVO requestMember = memberDAO.getMemberById(id);

            // PW가 틀린 경우
            if (!requestMember.getPassword().equals(password)) {
                return HttpResponse.builder()
                        .statusCode("400")
                        .statusText("Bad Request")
                        .body("login failed (mismatch PASSWORD)")
                        .build();
            }
        } catch (SQLException e) {
            // ID를 찾지 못했을 때
            e.printStackTrace();
            return HttpResponse.builder()
                    .statusCode("400")
                    .statusText("Bad Request")
                    .body("login failed (not found ID)")
                    .build();
        }

        // 로그인 성공! -> 세션 생성
        HttpResponse httpResponse = HttpResponse.builder()
                .statusCode("200")
                .statusText("OK")
                .body("login success")
                .build();
        UUID uuid = UUID.randomUUID();
        sessionContext.put(uuid.toString(), id);

        // Response header에 세션 키 추가
        httpResponse.setHeaders("Session-Key", uuid.toString());

        return httpResponse;
    };

    public static Function<HttpRequest, HttpResponse> member = request -> {

        // 세션 체크
        String sessionKey = request.headers.get("Session-Key");
        if(!sessionContext.containsKey(sessionKey) || sessionKey == null){
            return HttpResponse.builder()
                    .statusCode("400")
                    .statusText("Bad Request")
                    .body("please login before access DB")
                    .build();
        }

        // DB에서 member 정보 가져오기
        MemberDAO memberDAO = new MemberDAO();
        List<MemberVO> memberVOList = memberDAO.getMemberVOList();

        // body 만들기
        StringBuilder bodyBuilder = new StringBuilder();
        for (MemberVO memberVO : memberVOList) {
            bodyBuilder.append(memberVO.toString()).append(System.lineSeparator());
        }

        return HttpResponse.builder()
                .statusCode("200")
                .statusText("OK")
                .body(bodyBuilder.toString())
                .build();
    };


    // 등록되지 않은 PATH
    public static Function<HttpRequest, HttpResponse> other = request -> {
        return HttpResponse.builder()
                .statusCode("404")
                .statusText("Not found")
                .body("Wrong API access")
                .build();
    };
}
