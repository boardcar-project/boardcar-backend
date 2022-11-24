package test;

import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

public class HttpClientTestApp {

    public static Map<String, String> cookieMap = new HashMap<>();

    public static void main(String[] args) {

//        /* GET TEST */
//        HttpRequest testRequest = new HttpRequest("GET", "/httpTest", "HTTP/1.1", null);
//        System.out.println("GET - /httpTest 시작");
//
//        HttpResponse testResponse = requestToServer(testRequest);
//        System.out.println(testResponse.status);
//
//        System.out.println("GET - /httpTest 종료");

        /* POST TEST */
        // 로그인 정보 JSON 생성
        JSONObject loginJson = new JSONObject();
        loginJson.put("id", "testid");
        loginJson.put("password", "testpw");

        HttpRequest loginRequest = new HttpRequest("POST", "/login", "HTTP/1.1", loginJson.toString());

        System.out.println("POST - /login(id=testid, password=testpw) 시작");

        HttpResponse loginResponse = requestToServer(loginRequest);
        // 클라이언트에 쿠키 저장
        String cookieHeader = loginResponse.headers.get("Set-Cookie");
        String[] cookies = cookieHeader.split(";");
        for (String cookie : cookies) {
            String[] cookieEntry = cookie.split("=");
            cookieMap.put(cookieEntry[0], cookieEntry[1]);
        }
        System.out.println(loginResponse.status);
        System.out.println(loginResponse.body);

        System.out.println("POST - /login 종료");
    }

    public static HttpResponse requestToServer(HttpRequest httpRequest) {

        // 서버 정보
        String SERVER_IP;
        final int SERVER_PORT;
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(".properties"));
            SERVER_IP = properties.getProperty("SERVER_IP");
//            SERVER_IP = "localhost";
            SERVER_PORT = Integer.parseInt(properties.getProperty("SERVER_PORT"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // HTTP 통신
        try {
            // 클라이언트 소켓 열기
            Socket socket = new Socket(SERVER_IP, SERVER_PORT); // 서버 연결

            // 서버에 Request
            String requestString = requestDispatcher(httpRequest);
            socket.getOutputStream().write(requestString.getBytes(StandardCharsets.UTF_8));

            // 서버로부터 Response
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

        // HTTP 패킷 수신
        StringBuilder stringBuilder = new StringBuilder();
        String inputLine;
        while ((inputLine = bufferedReader.readLine()) != null) {
            stringBuilder.append(inputLine).append("\r\n");
        }

        // HTTP 패킷 parse
        String response = stringBuilder.toString();
        String[] responseArr = response.split("\r\n");

        // header - 응답 정보
        String responseInfo = responseArr[0];
        String[] responseInfoArr = responseInfo.split(" ");
        String version = responseInfoArr[0];
        String status = responseInfoArr[1];

        // headers - 나머지 헤더
        Map<String, String> headers = new HashMap<>();
        int i;
        for (i = 1; !responseArr[i].equals(""); i++) {
            String[] mapEntry = responseArr[i].split(":");
            headers.put(mapEntry[0].trim(), mapEntry[1].trim());
        }

        // body
        String body = responseArr[++i];

        return new HttpResponse(version, status, headers, body);
    }


    public static String requestDispatcher(HttpRequest httpRequest) {

        Map<String, Function<HttpRequest, String>> dispatcherTable = new HashMap<String, Function<HttpRequest, String>>() {
            {
                put("GET", RequestController.GET);
                put("POST", RequestController.POST);
            }
        };

        return dispatcherTable.getOrDefault(httpRequest.method, null).apply(httpRequest);
    }
}

class HttpRequest {

    String method;
    String path;
    String version;

    Map<String, String> headers;
    Map<String, String> cookie;

    String body;

    public HttpRequest(String method, String path, String version, String body) {
        this.method = method;
        this.path = path;
        this.version = version;
        this.body = body;
    }
}

class HttpResponse {

    String version;
    String status;
    Map<String, String> headers;

    Map<String, String> cookie = new HashMap<>();

    String body;

    public HttpResponse(String version, String status, Map<String, String> headers, String body) {
        this.version = version;
        this.status = status;
        this.headers = headers;
        this.body = body;
    }

    public void setCookie(String key, String value) {
        cookie.put(key, value);
    }

}


class RequestController {

    public static Function<HttpRequest, String> GET = httpRequest -> {

        StringBuilder stringBuilder = new StringBuilder();

        // header 제작
        stringBuilder.append("GET ").append(httpRequest.path).append(" HTTP/1.1\r\n");
        stringBuilder.append("\r\n");

        return stringBuilder.toString();
    };

    public static Function<HttpRequest, String> POST = httpRequest -> {

        StringBuilder stringBuilder = new StringBuilder();

        // header 제작
        stringBuilder.append("POST ").append(httpRequest.path).append(" HTTP/1.1\r\n");
        stringBuilder.append("Content-Length: ").append(httpRequest.body.getBytes().length).append("\r\n");
        stringBuilder.append("\r\n");

        // body 제작
        stringBuilder.append(httpRequest.body);

        return stringBuilder.toString();
    };

}
