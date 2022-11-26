import database.MemberDAO;
import database.MemberVO;

public class DatabaseTestApp {

    public static void main(String[] args) {
        MemberDAO memberDAO = new MemberDAO();
        for(MemberVO mvo :  memberDAO.getMemberVOList()){
            System.out.println(mvo);
        }

    }

}
