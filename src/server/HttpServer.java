package server;

import database.MemberDAO;
import database.MemberVO;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class HttpServer {

    public static void main(String[] args) {
        // Host IP 출력
        try {
            System.out.println(InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        // IP, PORT 번호 읽기
        final int SERVER_PORT;
        final String SERVER_IP;
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(".properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        SERVER_IP = properties.getProperty("SERVER_IP");
        SERVER_PORT = Integer.parseInt(properties.getProperty("SERVER_PORT"));

        // 서버 시작
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT, 50, InetAddress.getByName(SERVER_IP))) {
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
            // Sender의 InputStream
            HttpRequest request = requestBuilder(connection.getInputStream());

            // Sender의 Request 처리
            HttpResponse response = requestDispatcher(request);

            // Sender에게 Request 응답
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
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


    private static HttpResponse requestDispatcher(HttpRequest httpRequest) {

        // PATH = "/member/"
        Function<HttpRequest, HttpResponse> member = s -> {

            // DB에서 member 정보 가져오기
            MemberDAO memberDAO = new MemberDAO();
            List<MemberVO> memberVOList = memberDAO.getMemberVOList();

            // body 만들기
            StringBuilder stringBuilder = new StringBuilder();
            for (MemberVO memberVO : memberVOList) {
                stringBuilder.append(memberVO.toString()).append("\r\n");
            }

            return new HttpResponse("200 OK", stringBuilder.toString());
        };

        // 등록되지 않은 PATH
        Function<HttpRequest, HttpResponse> other = s -> new HttpResponse("404 Not Found", "404 Not Found");

        // PATH MAP
        Map<String, Function<HttpRequest, HttpResponse>> dispatcherTable = new HashMap<String, Function<HttpRequest, HttpResponse>>() {
            {
                put("/member", member);
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
    String body;

    public HttpResponse(String status, String body) {
        this.status = status;
        this.body = body;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("HTTP/1.1 ").append(status).append(" \r\n");
        stringBuilder.append("Content-Type: text/html;charset=utf-8\r\n");
        stringBuilder.append("Content-Length: ").append(body.length()).append("\r\n");
        stringBuilder.append("\r\n");
        stringBuilder.append(body);

        return stringBuilder.toString();
    }
}