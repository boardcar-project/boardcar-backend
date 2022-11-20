package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class HttpServer {
    private static final int PORT = 59699;

    public static void main(String[] args) {
        System.out.println("Server Online (PORT : " + PORT + ")");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            Socket connection;
            ExecutorService executorService = Executors.newFixedThreadPool(10);
            while ((connection = serverSocket.accept()) != null) {
                Socket finalConnection = connection;
                executorService.submit(() -> requestHandler(finalConnection));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static void requestHandler(Socket connection) {
        try {
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            HttpRequest request = requestBuilder(connection.getInputStream());

            HttpResponse response = dispatcher(request);

            out.write(response.toString().getBytes());
            out.flush();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static HttpRequest requestBuilder(InputStream inputStream) throws IOException {


        // HTTP 요청 전체 읽기
        StringBuilder sb = new StringBuilder();
        String inputLine;
        while (!(inputLine = myReadLine(inputStream)).equals("")) {
            sb.append(inputLine).append("\n"); // sb : HTTP 요청 전체
        }


        // HTTP 요청 한 줄씩 처리
        String request = sb.toString();
        String[] requestArr = request.split("\n"); // \n으로 split -> 한 줄씩 나눔


        // 헤더 - 요청 부분 parse
        String requestInfo = requestArr[0]; // 요청
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

        System.out.println(body);
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


    private static HttpResponse dispatcher(HttpRequest httpRequest) {
        Function<HttpRequest, HttpResponse> user = s -> {
            return new HttpResponse("200 OK", "user");
        };
        Function<HttpRequest, HttpResponse> other = s -> {
            return new HttpResponse("404 Not Found", "others");
        };

        Map<String, Function<HttpRequest, HttpResponse>> dispatcherTable = new HashMap<String, Function<HttpRequest, HttpResponse>>() {
            {
                put("/user", user);
                put("/", other);
            }
        };

        return dispatcherTable.getOrDefault(httpRequest.path, other).apply(httpRequest);

    }
}

class HttpRequest {
    String method;
    String path;
    String version;
    Map<String, String> headers;
    String body;

    public HttpRequest(String method, String path, String version, Map<String, String> headers, String body) {
        this.method = method;
        this.path = path;
        this.version = version;
        this.headers = headers;
        this.body = body;
    }
}

class HttpResponse {
    String status;
    Map<String, String> headers = new HashMap<>();
    String body;

    public HttpResponse(String status, String body) {
        this.status = status;
        this.body = body;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 " + status + " \r\n");
        sb.append("Content-Type: text/html;charset=utf-8\r\n");
        sb.append("Content-Length: " + body.length() + "\r\n");
        sb.append("\r\n");
        sb.append(body);
        return sb.toString();
    }
}