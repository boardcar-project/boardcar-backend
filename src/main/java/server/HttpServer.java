package server;

import http.HttpRequest;
import http.HttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.logging.Logger;

public class HttpServer {

    private static final Logger logger = Logger.getLogger("Server Logger");

    private static Connection databaseConnection = null;

    private static final Map<String, Function<HttpRequest, HttpResponse>> dispatcherTable = new HashMap<String, Function<HttpRequest, HttpResponse>>() {
        {
            put("/httpTest", RequestController.httpTest);
            put("/login", RequestController.login);
            put("/myInfo", RequestController.myInfo);
            put("/members", RequestController.members);
            put("/changePassword", RequestController.changePassword);
            put("/uploadPost", RequestController.uploadPost);
            put("/openPostList", RequestController.openPostList);
            put("/openPost", RequestController.openPost);
            put("/updatePost", RequestController.updatePost);
            put("/deletePost", RequestController.deletePost);
        }
    };

    public static void main(String[] args) throws IOException, SQLException {
        // PORT 번호 읽기
        Properties properties = new Properties();
        properties.load(Files.newInputStream(Paths.get(".properties")));
        final int SERVER_PORT = Integer.parseInt(properties.getProperty("SERVER_PORT"));

        // DB 서버 연결
        databaseConnection = DriverManager.getConnection(properties.getProperty("DB_URL"), properties.getProperty("DB_ID"), properties.getProperty("DB_PW"));

        // 서버 시작
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            logger.info("Server start (PORT : " + SERVER_PORT + ")");

            // 클라이언트 연결 요청 대기
            Socket clientSocket;
            ExecutorService executorService = Executors.newFixedThreadPool(10); // newCachedThreadPool()? https://codechacha.com/ko/java-executors/
            while ((clientSocket = serverSocket.accept()) != null) {
                Socket connection = clientSocket;

                logger.info("[Client Connected] " + connection.toString());
                executorService.submit(() -> requestHandler(connection)); // 클라이언트와 통신 쓰레드
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
            logger.info("[Request] " + connection.toString());

            // Sender의 Request 처리
            HttpResponse response = requestDispatcher(request);
            logger.info("[Response] " + connection.toString());

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
            logger.info("[Socket close] " + connection.toString());
        }
    }

    private static HttpRequest requestBuilder(InputStream inputStream) throws IOException {

        // HTTP 요청 전체 읽기
        StringBuilder stringBuilder = new StringBuilder();
        String inputLine;
        while (!(inputLine = myReadLine(inputStream)).equals("")) {
            stringBuilder.append(inputLine).append("\n"); // stringBuilder : HTTP 요청 전체
        }

        // HTTP 요청 한 줄씩 처리
        String request = stringBuilder.toString();
        String[] requestArr = request.split("\n"); // \n으로 split -> 한 줄씩 나눔

        // 헤더 - 요청 부분 parse
        String[] requestInfo = requestArr[0].split(" ");
        String method = requestInfo[0];
        String path = requestInfo[1];
        String version = requestInfo[2];

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
        String body = "";
        int contentLength = Integer.parseInt(headers.getOrDefault("Content-Length", "0"));
        if (contentLength > 0) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            for (int i = 0; i < contentLength; i++) {
                byteArrayOutputStream.write(inputStream.read());
            }
            body = new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8);
        }

        return new HttpRequest(method, path, version, headers, body);
    }

    private static String myReadLine(InputStream inputStream) throws IOException {
        ByteArrayOutputStream tmp = new ByteArrayOutputStream();
        while (true) {
            int b = inputStream.read();

            if (b == '\n' || b == '\0') {
                break;
            }

            tmp.write(b);
        }
        return new String(tmp.toByteArray(), StandardCharsets.UTF_8).trim();
    }

    private static HttpResponse requestDispatcher(HttpRequest httpRequest) {

        return dispatcherTable.getOrDefault(httpRequest.getPath(), RequestController.other).apply(httpRequest);
    }

    public static Connection getDatabaseConnection() {
        return databaseConnection;
    }
}

