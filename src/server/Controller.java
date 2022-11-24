package server;

import database.MemberDAO;
import database.MemberVO;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class Controller {
    public static Map<String, String> sessionContext = new HashMap<>();

    public static Function<HttpRequest, HttpResponse> httpTest = request -> new HttpResponse("200 OK", "httpTest Success");

    public static Function<HttpRequest, HttpResponse> login = request -> {

        // HTTP request에서 body의 JSON을 parse
        String requestId, requestPassword;
        JSONObject jsonObject = new JSONObject(request.body);
        requestId = jsonObject.getString("id");
        requestPassword = jsonObject.getString("password");

        // DB에서 회원 ID 찾기
        MemberDAO memberDAO = new MemberDAO();
        try {
            MemberVO requestMember = memberDAO.getMemberById(requestId);

            // PW가 틀린 경우
            if(!requestMember.getPASSWORD().equals(requestPassword)){
                return new HttpResponse("400 Bad Request", "login failed (mismatch PASSWORD)");
            }
        } catch (SQLException e) {
            // ID를 찾지 못했을 때
            e.printStackTrace();
            return new HttpResponse("400 Bad Request", "login failed (not found ID)");
        }

        // 로그인 성공! -> 세션 생성
        HttpResponse httpResponse = new HttpResponse("200 OK", "");
        UUID uuid = UUID.randomUUID();
        sessionContext.put(uuid.toString(), requestId);

        // 쿠키 생성
        httpResponse.setCookie("id", uuid.toString());

        return httpResponse;
    };

    public static Function<HttpRequest, HttpResponse> member = request -> {

        // 세션 체크
        String requestId = request.getCookie("id");
        if (!sessionContext.containsKey(requestId))
            return new HttpResponse("400 Bad Request", "please login");

        // DB에서 member 정보 가져오기
        MemberDAO memberDAO = new MemberDAO();
        List<MemberVO> memberVOList = memberDAO.getMemberVOList();

        // body 만들기
        StringBuilder stringBuilder = new StringBuilder();
        for (MemberVO memberVO : memberVOList) {
            stringBuilder.append(memberVO.toString()).append("\r\n");
        }


//        String userState = sessionContext.get(id);
//        if (userState.expires <= LocalDateTime.now()){
//            return new HttpResponse("400 Bad Request", "");
//        }

        return new HttpResponse("200 OK", stringBuilder.toString());
    };


    // 등록되지 않은 PATH
    public static Function<HttpRequest, HttpResponse> other = request -> new HttpResponse("404 Not Found", "404 Not Found");
}
