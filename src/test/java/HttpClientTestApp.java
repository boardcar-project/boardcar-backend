import lombok.Builder;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class HttpClientTestApp {

    public static String sessionKey = null;

    public static void main(String[] args) {

        TestMethod.getTest_httpTest();
        TestMethod.postTest_login();
        TestMethod.getTest_member();

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
            socket.getOutputStream().write(httpRequest.toString().getBytes(StandardCharsets.UTF_8));
            socket.getOutputStream().flush();

            // 서버로부터 Response 받기
            HttpResponse httpResponse = responseBuilder(socket.getInputStream());

            // 클라이언트 소켓 닫기
            socket.close();
            return httpResponse;

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static HttpResponse responseBuilder(InputStream inputStream) throws IOException {

        // HTTP 요청 전체 읽기
        StringBuilder stringBuilder = new StringBuilder();
        String inputLine;
        while (!(inputLine = myReadLine(inputStream)).equals("")) {
            stringBuilder.append(inputLine).append(System.lineSeparator()); // sb : HTTP 요청 전체
        }

        // HTTP 요청 한 줄씩 처리
        String response = stringBuilder.toString();
        String[] responseArr = response.split(System.lineSeparator());

        // 헤더 - 요청 부분 parse
        String[] statusLine = responseArr[0].split(" ");
        String version = statusLine[0];
        String statusCode = statusLine[1];
        String statusText = statusLine[2];

        // 남은 헤더 parse
        Map<String, String> headers = new HashMap<>();
        for (int i = 1; i < responseArr.length; ++i) {
            if (responseArr[i].equals("")) { // 아무것도 없는 줄을 만난다 -> 헤더의 끝을 만남
                break;
            }

            // 헤더의 (키:값)을 해시맵에 저장
            String[] temp = responseArr[i].split(":");
            headers.put(temp[0].trim(), temp[1].trim());
        }

        // 바디 parse
        String body = null;
        int contentLength = Integer.parseInt(headers.getOrDefault("Content-Length", "0"));
        if (contentLength > 0) {

            ByteArrayOutputStream tmp = new ByteArrayOutputStream();
            for (int i = 0; i < contentLength; i++) {
                int b = inputStream.read();
                tmp.write(b);
            }
            body = new String(tmp.toByteArray(), StandardCharsets.UTF_8);
        }

        return HttpResponse.builder()
                .version(version)
                .statusCode(statusCode)
                .statusText(statusText)
                .headers(headers)
                .body(body)
                .build();
    }

    private static String myReadLine(InputStream inputStream) throws IOException {
        byte[] bytes = new byte[2048];
        int idx = 0;
        while (true) {
            bytes[idx] = (byte) inputStream.read();

            if (bytes[idx] == '\n' || bytes[idx] == '\0') {
                break;
            }

            idx++;
        }
        return new String(bytes, StandardCharsets.UTF_8).trim();
    }

}

@Builder
class HttpRequest {
    String method;
    String path;
    @Builder.Default
    String version = "HTTP/1.1";
    @Builder.Default
    Map<String, String> headers = new HashMap<>();
    @Builder.Default
    String body = null;


    public void setHeaders(String header, String value) {
        headers.put(header, value);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        // start-line
        stringBuilder.append(method).append(" ").append(path).append(" ").append(version).append(System.lineSeparator());

        // headers
        if (body != null) {
            headers.put("Content-Length", String.valueOf(body.getBytes(StandardCharsets.UTF_8).length));
        }
        if (headers != null) {
            headers.forEach((header, value) -> {
                stringBuilder.append(header).append(": ").append(value).append(System.lineSeparator());
            });
        }
        stringBuilder.append(System.lineSeparator());

        // body
        if (!method.equals("GET")) { // GET은 body가 없음
            stringBuilder.append(body).append("\r\n");
        }

        return stringBuilder.toString();
    }
}

@Builder
class HttpResponse {
    String version;
    String statusCode;
    String statusText;
    Map<String, String> headers;
    String body;
}

class TestMethod{
    public static void getTest_httpTest() {
        /* GET TEST */
        HttpRequest testRequest = HttpRequest.builder()
                .method("GET")
                .path("/httpTest")
                .version("HTTP/1.1")
                .build();

        HttpResponse testResponse = HttpClientTestApp.sendHttpRequest(testRequest);
        System.out.println("Response Status : " + testResponse.statusCode);
        System.out.println(testResponse.body);
        System.out.println();
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

        HttpResponse loginResponse = HttpClientTestApp.sendHttpRequest(loginRequest);
        if (loginResponse.statusCode.equals("200")) {
            HttpClientTestApp.sessionKey = loginResponse.headers.get("Session-Key");
        }

        System.out.println("Response Status : " + loginResponse.statusCode);
        System.out.println(loginResponse.body);
        System.out.println("Session-Key is " + HttpClientTestApp.sessionKey);
        System.out.println();
    }

    public static void getTest_member() {
        /* GET TEST - 멤버 테이블 전체 가져오기 */

        HttpRequest memberListRequest = HttpRequest.builder()
                .method("GET")
                .path("/member")
                .version("HTTP/1.1")
                .build();
        memberListRequest.setHeaders("Session-Key", HttpClientTestApp.sessionKey);

        HttpResponse memberListResponse = HttpClientTestApp.sendHttpRequest(memberListRequest);

        System.out.println("Response Status : " + memberListResponse.statusCode);
        System.out.println(memberListResponse.body);
        System.out.println();
    }

}

