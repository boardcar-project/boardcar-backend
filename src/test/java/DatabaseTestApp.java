import database.MemberDAO;
import database.MemberVO;

import java.sql.SQLException;

public class DatabaseTestApp {

    public static void main(String[] args) throws SQLException {
        MemberDAO memberDAO = new MemberDAO();
        for (MemberVO mvo : memberDAO.SELECT_memberList()) {
            System.out.println(mvo);
        }

    }

}
