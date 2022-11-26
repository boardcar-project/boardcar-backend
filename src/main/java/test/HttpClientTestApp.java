package test;

import lombok.Builder;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class HttpClientTestApp {

    public static String sessionKey = null;

    public static void getTest_httpTest() {
        /* GET TEST */
        HttpRequest testRequest = HttpRequest.builder()
                .method("GET")
                .path("/httpTest")
                .version("HTTP/1.1")
                .build();
        HttpResponse testResponse = sendHttpRequest(testRequest);
        System.out.println("Response Status : " + testResponse.statusCode);
        System.out.println(testResponse.body);
    }

    public static void postTest_login() {
        /* POST TEST */
        // 로그인 정보 JSON 생성
        JSONObject loginJson = new JSONObject();
        loginJson.put("id", "testid");
        loginJson.put("password", "testpw");
        String body = loginJson.toString();

        HttpRequest loginRequest = HttpRequest.builder()
                .method("POST")
                .path("/login")
                .version("HTTP/1.1")
                .body(body)
                .build();
        HttpResponse loginResponse = sendHttpRequest(loginRequest);
        if (loginResponse.statusCode.equals("200")) {
            sessionKey = loginResponse.headers.get("Session-Key");
        }

        System.out.println("Response Status : " + loginResponse.statusCode);
        System.out.println(loginResponse.body);
        System.out.println("Session-Key is " + sessionKey);
    }

    public static void getTest_member() {
        /* GET TEST */
        // 멤버 테이블 전체 가져오기
        HttpRequest memberListRequest = HttpRequest.builder()
                .method("GET")
                .path("/member")
                .version("HTTP/1.1")
                .build();
//        memberListRequest.setSessionKey(sessionKey); // httpRequest에 Cookie 헤더 넣기

        HttpResponse memberListResponse = sendHttpRequest(memberListRequest);
        System.out.println("Response Status : " + memberListResponse.statusCode);
        System.out.println(memberListResponse.body);
    }

    public static void main(String[] args) {


        getTest_httpTest();
//        postTest_login();
//        getTest_member();

    }


    public static HttpResponse sendHttpRequest(HttpRequest httpRequest) {

        // 서버 정보 가져오기
        String SERVER_IP;
        final int SERVER_PORT;
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(".properties"));
//            SERVER_IP = properties.getProperty("SERVER_IP");
            SERVER_IP = "localhost";
            SERVER_PORT = Integer.parseInt(properties.getProperty("SERVER_PORT"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // HTTP 통신
        try {
            // 클라이언트 소켓 열기
            Socket socket = new Socket(SERVER_IP, SERVER_PORT); // 서버 연결

            // 서버에 Request 보내기
            String requestPacket = packetBuilder(httpRequest);
            socket.getOutputStream().write(requestPacket.getBytes(StandardCharsets.UTF_8));
            socket.getOutputStream().flush();

            // 서버로부터 Response 받기
            HttpResponse httpResponse = responseBuilder(socket.getInputStream());

            // 클라이언트 소켓 닫기
            socket.close();
            return httpResponse;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static HttpResponse responseBuilder(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

        // HTTP 패킷 전체 수신
        StringBuilder stringBuilder = new StringBuilder();
        String inputLine;
        while ((inputLine = bufferedReader.readLine()) != null) {
            stringBuilder.append(inputLine).append(System.lineSeparator());
        }

        // HTTP response packet parse
        String response = stringBuilder.toString();
        String[] responseArr = response.split(System.lineSeparator());

        // status line
        String[] statusLine = responseArr[0].split(" ");
        String version = statusLine[0];
        String statusCode = statusLine[1];
        String statusText = statusLine[2];

        // headers
        Map<String, String> headers = new HashMap<>();
        int i;
        for (i = 1; !responseArr[i].equals(""); i++) {
            String[] headerEntry = responseArr[i].split(":");
            headers.put(headerEntry[0].trim(), headerEntry[1].trim());
        }

        // body
        String body = responseArr[++i]; // i : headers body 구분자(\r\n)

        return HttpResponse.builder()
                .version(version)
                .statusCode(statusCode)
                .statusText(statusText)
                .headers(headers)
                .body(body)
                .build();
    }


    public static String packetBuilder(HttpRequest httpRequest) {
        StringBuilder stringBuilder = new StringBuilder();
        String version = "HTTP/1.1";

        // start-line
        stringBuilder.append(httpRequest.method).append(" ").append(httpRequest.path).append(" ").append(version).append(System.lineSeparator());

        // headers
        if(httpRequest.headers != null){
            httpRequest.headers.forEach((header, value) -> {
                stringBuilder.append(header).append(": ").append(value).append(System.lineSeparator());
            });
        }
        stringBuilder.append(System.lineSeparator());

        // body
        if (!httpRequest.method.equals("GET")) { // GET은 body가 없음
            stringBuilder.append(httpRequest.body).append("\r\n");
        }

        return stringBuilder.toString();
    }

}

@Builder
class HttpRequest {
    String method;
    String path;
    String version;
    @Builder.Default
    Map<String, String> headers = new HashMap<>();
    @Builder.Default
    String body = null;

//    public void setSessionKey(String sessionKey) {
//        headers.put("Session-Key", sessionKey);
//    }
}

@Builder
class HttpResponse {
    String version;
    String statusCode;
    String statusText;
    Map<String, String> headers;
    String body;
}

