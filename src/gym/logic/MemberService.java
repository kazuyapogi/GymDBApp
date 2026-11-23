package gym.logic;

import gym.data.MemberDAO;
import gym.model.Member;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Logic tier for the Member record. Owner: Kazuya Niki. */
public class MemberService {
    private final MemberDAO dao = new MemberDAO();

    /** Lists members after marking memberships that already ended as Expired. */
    public List<Member> list(String membershipType, String status) throws SQLException {
        dao.expirePastMemberships();
        return dao.findAll(membershipType, status);
    }

    public List<Member> listAll() throws SQLException {
        return list(null, null);
    }

    public Member get(int memberId) throws SQLException {
        return dao.findById(memberId);
    }

    public int countAttendance(int memberId) throws SQLException {
        return dao.countAttendance(memberId);
    }

    public int add(Member m) throws ValidationException, SQLException {
        validate(m);
        return dao.insert(m);
    }

    public void update(Member m) throws ValidationException, SQLException {
        validate(m);
        dao.update(m);
    }

    /** A member with attendance history cannot be deleted; set them to Expired instead. */
    public void delete(int memberId) throws ValidationException, SQLException {
        Member m = dao.findById(memberId);
        ValidationException.check(m != null, "Member " + memberId + " was not found.");
        int n = dao.countAttendance(memberId);
        ValidationException.check(n == 0, m.fullName() + " has " + n + " attendance record(s), so the member cannot be deleted.\n"
                + "Set the member's status to Expired instead.");
        dao.delete(memberId);
    }

    /** End date that matches the membership type: 1, 3 or 12 months minus one day. */
    public static LocalDate suggestedEndDate(LocalDate start, String membershipType) {
        if (start == null || membershipType == null) {
            return null;
        }
        int months;
        switch (membershipType) {
            case "Quarterly": months = 3; break;
            case "Annual": months = 12; break;
            default: months = 1;
        }
        return start.plusMonths(months).minusDays(1);
    }

    private void validate(Member m) throws ValidationException {
        ValidationException.required(m.lastName, "Last name");
        ValidationException.required(m.firstName, "First name");
        m.lastName = m.lastName.trim();
        m.firstName = m.firstName.trim();
        ValidationException.check(m.birthdate != null, "Birthdate is required.");
        ValidationException.check(m.birthdate.isBefore(LocalDate.now()), "Birthdate must be in the past.");
        ValidationException.check(m.sex != null, "Sex is required.");
        ValidationException.check(m.membershipType != null, "Membership type is required.");
        ValidationException.check(m.membershipStart != null && m.membershipEnd != null, "Membership start and end dates are required.");
        ValidationException.check(m.membershipEnd.isAfter(m.membershipStart), "Membership end date must be later than the start date.");
        if (m.email != null && !m.email.trim().isEmpty()) {
            ValidationException.check(m.email.contains("@"), "Email address is not valid.");
        }
        if (m.status == null) {
            m.status = "Active";
        }
        if (m.membershipEnd.isBefore(LocalDate.now())) {
            m.status = "Expired";
        }
    }
}
