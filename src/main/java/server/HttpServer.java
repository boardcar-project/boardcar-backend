package server;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
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


        // 서버 시작
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            logger.info("Server start (PORT : " + SERVER_PORT + ")");

            // 클라이언트 연결 요청 대기
            Socket connection;
            ExecutorService executorService = Executors.newFixedThreadPool(10); // newCachedThreadPool()? https://codechacha.com/ko/java-executors/
            while ((connection = serverSocket.accept()) != null) {
                Socket clientConnection = connection;

                logger.info("Client Connected");
                executorService.submit(() -> requestHandler(clientConnection)); // 클라이언트와 통신 쓰레드
            }

        } catch (IOException e) {
            e.printStackTrace();
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
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

    }

    private static HttpRequest requestBuilder(InputStream inputStream) throws IOException {

        // HTTP 요청 전체 읽기
        StringBuilder sb = new StringBuilder();
        String inputLine;
        while (!(inputLine = myReadLine(inputStream)).equals("")) {
            sb.append(inputLine).append(System.lineSeparator()); // sb : HTTP 요청 전체
        }

        // HTTP 요청 한 줄씩 처리
        String request = sb.toString();
        String[] requestArr = request.split(System.lineSeparator()); // \n으로 split -> 한 줄씩 나눔

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

        return HttpRequest.builder()
                .method(method)
                .path(path)
                .version(version)
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

// 서버와 클라 사이에 약속된 id로 서버에 원하는 상태를 저장한다.
// /login api로 id와 pw를 넘겨주면 우리가 클라이언트 쿠키에 서버에서 생성한 UID를 내려준다.
// api 요청(/member)마다 쿠키 값을 넘겨받아서 세션에 등록된 유저면 원하는 동작을 수행한다
// 세션이 없다면 로그인 페이지로 리다이렉트(/login) 한다.

    private static HttpResponse requestDispatcher(HttpRequest httpRequest) {

        Map<String, Function<HttpRequest, HttpResponse>> dispatcherTable = new HashMap<String, Function<HttpRequest, HttpResponse>>() {
            {
                put("/httpTest", RequestController.httpTest);
                put("/login", RequestController.login);
                put("/member", RequestController.member);
            }
        };

        return dispatcherTable.getOrDefault(httpRequest.path, RequestController.other).apply(httpRequest);
    }
}
@Builder
class HttpRequest {
    String method;
    String path;
    String version;
    Map<String, String> headers;
    String body;
}

@Builder
@Getter
@Setter
class HttpResponse {
    @Builder.Default
    String version = "HTTP/1.1";
    String statusCode;
    String statusText;
    @Builder.Default
    Map<String, String> headers = new HashMap<String, String>(){
        {
            put("Content-Type", "text/html;charset=utf-8");
        }
    };
    @Builder.Default
    String body = null;

    public void setHeaders(String header, String value) {
        headers.put(header, value);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        // status line
        stringBuilder.append(version).append(" ").append(statusCode).append(" ").append(statusText).append(System.lineSeparator());

        // headers
        if(body != null){
            headers.put("Content-Length", String.valueOf(body.length()));
        }
        headers.forEach((header, value) -> {
            stringBuilder.append(header).append(": ").append(value).append(System.lineSeparator());
        });
        stringBuilder.append(System.lineSeparator());

        // body
        stringBuilder.append(body);

        return stringBuilder.toString();
    }
}

