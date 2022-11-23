package server;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.logging.Logger;


public class HttpServer {

    private static final Logger logger = Logger.getLogger("ServerLogger");

    public static void main(String[] args) {

        // PORT 번호 읽기
        final int SERVER_PORT;
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(".properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        SERVER_PORT = Integer.parseInt(properties.getProperty("SERVER_PORT"));

        logger.info("Server start (PORT : " + SERVER_PORT + ")");

        // 서버 시작
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            Socket connection;

            ExecutorService executorService = Executors.newFixedThreadPool(10);
            while ((connection = serverSocket.accept()) != null) {
                Socket finalConnection = connection;

                logger.info("Client Connected");

                executorService.submit(() -> requestHandler(finalConnection));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static void requestHandler(Socket connection) {
        try {
            // Sender의 InputStream
            HttpRequest request = requestBuilder(connection.getInputStream());

            // Sender의 Request 처리
            HttpResponse response = requestDispatcher(request);

            // Sender에게 Request 응답
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.write(response.toString().getBytes(StandardCharsets.UTF_8));
            out.flush();

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                connection.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private static HttpRequest requestBuilder(InputStream inputStream) throws IOException {

        // HTTP 요청 전체 읽기
        StringBuilder sb = new StringBuilder();
        String inputLine;
        while (!(inputLine = myReadLine(inputStream)).equals("")) {
            sb.append(inputLine).append("\r\n"); // sb : HTTP 요청 전체
        }

        // HTTP 요청 한 줄씩 처리
        String request = sb.toString();
        String[] requestArr = request.split("\r\n"); // \n으로 split -> 한 줄씩 나눔

        // 헤더 - 요청 부분 parse
        String requestInfo = requestArr[0]; // requestArr[0] : 요청 전체
        String[] requestInfoArr = requestInfo.split(" ");
        String method = requestInfoArr[0];
        String path = requestInfoArr[1];
        String version = requestInfoArr[1];

        // 남은 헤더 parse
        Map<String, String> headers = new HashMap<>();
        for (int i = 1; i < requestArr.length; ++i) {
            if (requestArr[i].equals("")) { // 아무것도 없는 줄을 만난다 -> 헤더의 끝을 만남
                break;
            }

            // 헤더의 (키:값)을 해시맵에 저장
            String[] temp = requestArr[i].split(":");
            headers.put(temp[0].trim(), temp[1].trim());
        }

        // 바디 parse
        int contentLength = Integer.parseInt(headers.getOrDefault("Content-Length", "0"));

        ByteBuffer byteBuffer = ByteBuffer.allocate(512);
        for (int i = 0; i < contentLength; i++) {
            byteBuffer.put((byte) inputStream.read());
        }
        String body = new String(byteBuffer.array(), StandardCharsets.UTF_8).trim();

        return new HttpRequest(method, path, version, headers, body);
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

// 서버와 클라 사이에 약속된 id로 서버에 원하는 상태를 저장한다.
// /login api로 id와 pw를 넘겨주면 우리가 클라이언트 쿠키에 서버에서 생성한 UID를 내려준다.
// api 요청(/member)마다 쿠키 값을 넘겨받아서 세션에 등록된 유저면 원하는 동작을 수행한다
// 세션이 없다면 로그인 페이지로 리다이렉트(/login) 한다.

    private static HttpResponse requestDispatcher(HttpRequest httpRequest) {

        // PATH MAP
        Map<String, Function<HttpRequest, HttpResponse>> dispatcherTable = new HashMap<String, Function<HttpRequest, HttpResponse>>() {
            {
                put("/httpTest", Controller.httpTest);
                put("/login", Controller.login);
                put("/member", Controller.member);
            }
        };

        return dispatcherTable.getOrDefault(httpRequest.path, Controller.other).apply(httpRequest);

    }
}

class HttpRequest {
    String method;
    String path;
    String version;
    Map<String, String> headers;
    Map<String, String> cookie;
    String body;

    public HttpRequest(String method, String path, String version, Map<String, String> headers, String body) {
        this.method = method;
        this.path = path;
        this.version = version;
        this.headers = headers;
        this.cookie = new HashMap<>();
//        Optionof 알아보기

        String cookieStr = headers.getOrDefault("Cookie", null);
        if (cookieStr != null && !cookieStr.trim().isEmpty()) {
            for (String property : cookieStr.split(";")) {
                String[] keyAndValue = property.split("=");
                cookie.put(keyAndValue[0], keyAndValue[1]);
            }
        }

        this.body = body;
    }

    public String getCookie(String key) {
        return cookie.getOrDefault(key, null);
    }
}

class HttpResponse {
    String status;
    String body;
    Map<String, String> cookie = new HashMap<>();

    public HttpResponse(String status, String body) {
        this.status = status;
        this.body = body;
    }

    public void setCookie(String key, String value) {
        cookie.put(key, value);
    }

    private String cookieToString() {
        StringBuilder sb = new StringBuilder();
        cookie.forEach((k, v) -> {
            sb.append(k).append("=").append(v).append(";");
        });

        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("HTTP/1.1 ").append(status).append(" \r\n");
        stringBuilder.append("Content-Type: text/html;charset=utf-8\r\n");
        stringBuilder.append("Content-Length: ").append(body.length()).append("\r\n");

        if (!cookie.isEmpty()) {
            stringBuilder.append("Set-Cookie:").append(cookieToString()).append("\r\n");
        }

        stringBuilder.append("\r\n");
        stringBuilder.append(body);

        return stringBuilder.toString();
    }
}