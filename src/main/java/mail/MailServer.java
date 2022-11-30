package mail;

import org.json.JSONObject;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Random;

public class MailServer {

    private final String MAIL_HOST;
    private final String MAIL_USER;
    private final String MAIL_PASSWORD;

    private final Session session;
    private int authNumber;

    private final static Random random = new Random();

    public MailServer() {

        // 메일 서버 정보 읽기
        Properties properties = new Properties();
        try {
            properties.load(Files.newInputStream(Paths.get(".properties")));

            MAIL_HOST = properties.getProperty("MAIL_HOST");
            MAIL_USER = properties.getProperty("MAIL_USER");
            MAIL_PASSWORD = properties.getProperty("MAIL_PASSWORD");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        // 메일 서버
        Properties props = new Properties();
        props.put("mail.smtp.host", MAIL_HOST);
        props.put("mail.smtp.port", 587);
        props.put("mail.smtp.auth", "true");

        session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(MAIL_USER, MAIL_PASSWORD);
            }
        });
    }

    public void sendAuthMail(JSONObject jsonObject) throws MessagingException {

        // JSON parse
        String targetMailAddress = jsonObject.getString("EMAIL");


        // 메일 생성
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(MAIL_USER));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(targetMailAddress));

        // 인증 번호 생성
        authNumber = 100000 + random.nextInt(900000);

        // 제목 설정
        message.setSubject("Boardcar 인증 메일");

        // 바디 설정
        String body = "인증 번호는 " + authNumber + "입니다.";
        message.setText(body);

        // 메일 전송
        Transport.send(message);

    }

    public boolean isMatchAuthNumber(JSONObject jsonObject) {

        int inputAuthNumber = jsonObject.getInt("NUMBER");

        return authNumber == inputAuthNumber;
    }

    public int getAuthNumber() {
        return authNumber;
    }

}
