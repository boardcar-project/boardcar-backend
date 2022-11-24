package test;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class HttpClientTestApp {

    public static void main(String[] args) {
        httpRequest("/httpTestd");
    }


    public static void httpRequest(String path) {

        // 서버 정보
        String SERVER_IP;
        final int SERVER_PORT;
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(".properties"));
            SERVER_IP = properties.getProperty("SERVER_IP");
            SERVER_PORT = Integer.parseInt(properties.getProperty("SERVER_PORT"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        // HTTP 통신
        try {
            Socket socket = new Socket(SERVER_IP, SERVER_PORT); // 서버 연결

            request(socket.getOutputStream(), path); // 서버에 요청
            response(socket.getInputStream()); // 서버의 응답 처리

            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void request(OutputStream outputStream, String path) {

        StringBuilder stringBuilder = new StringBuilder();
        String body = "안녕하세요?";

        // header 제작
        stringBuilder.append("GET ").append(path).append(" HTTP/1.1\\r\\n");
        stringBuilder.append("Content-Length: ").append(body.getBytes().length).append("\r\n");
        stringBuilder.append("\r\n");

        // body 제작
        stringBuilder.append(body);

        try {
            outputStream.write(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void response(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

        StringBuilder stringBuilder = new StringBuilder();
        String inputLine;

        // HTTP 패킷 수신
        while ((inputLine = bufferedReader.readLine()) != null) {
            stringBuilder.append(inputLine).append("\r\n");
        }

        System.out.println(stringBuilder.toString());
    }
}
