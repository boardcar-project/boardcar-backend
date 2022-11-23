package server;

import database.MemberDAO;
import database.MemberVO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class Controller {
    public static Map<String, String> sessionContext = new HashMap<>();

    public static Function<HttpRequest, HttpResponse> httpTest = request -> new HttpResponse("200 OK", "httpTest Success");

    public static Function<HttpRequest, HttpResponse> login = request -> {
        HttpResponse httpResponse = new HttpResponse("200 OK", "");

        UUID uuid = UUID.randomUUID();

//        request.body;
//        MemberDAO memberDAO = new MemberDAO();
//        MemberVO memberVO = memberDAO.getMember("yu2022");


        sessionContext.put(uuid.toString(), "user1");


        httpResponse.setCookie("id", uuid.toString());

        return httpResponse;
    };

    public static Function<HttpRequest, HttpResponse> member = request -> {

        // DB에서 member 정보 가져오기
        MemberDAO memberDAO = new MemberDAO();
        List<MemberVO> memberVOList = memberDAO.getMemberVOList();

        String id = request.getCookie("id");

        if (!sessionContext.containsKey(id))
            return new HttpResponse("400 Bad Request", "");

        System.out.println(sessionContext.get(id));

//        String userState = sessionContext.get(id);
//        if (userState.expires <= LocalDateTime.now()){
//            return new HttpResponse("400 Bad Request", "");
//        }

        // body 만들기
        StringBuilder stringBuilder = new StringBuilder();
        for (MemberVO memberVO : memberVOList) {
            stringBuilder.append(memberVO.toString()).append("\r\n");
        }

        return new HttpResponse("200 OK", stringBuilder.toString());
    };


    // 등록되지 않은 PATH
    public static Function<HttpRequest, HttpResponse> other = request -> new HttpResponse("404 Not Found", "404 Not Found");
}
