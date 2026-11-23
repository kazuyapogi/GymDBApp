package gym.data;

import gym.model.Member;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Data tier for the member table. Owner: Kazuya Niki. */
public class MemberDAO {
    private static final String SELECT =
            "SELECT member_id, last_name, first_name, birthdate, sex, contact_number, email, "
          + "membership_type, membership_start, membership_end, status FROM member";

    /** List/filter: pass null for a filter to skip it. */
    public List<Member> findAll(String membershipType, String status) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT).append(" WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        if (membershipType != null) {
            sql.append(" AND membership_type = ?");
            params.add(membershipType);
        }
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        sql.append(" ORDER BY member_id");
        return Sql.query(sql.toString(), params, MemberDAO::map);
    }

    public Member findById(int memberId) throws SQLException {
        return Sql.queryOne(SELECT + " WHERE member_id = ?", MemberDAO::map, memberId);
    }

    public int insert(Member m) throws SQLException {
        return Sql.insert(
                "INSERT INTO member (last_name, first_name, birthdate, sex, contact_number, email, "
              + "membership_type, membership_start, membership_end, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                m.lastName, m.firstName, m.birthdate, m.sex, Sql.nullIfBlank(m.contactNumber), Sql.nullIfBlank(m.email),
                m.membershipType, m.membershipStart, m.membershipEnd, m.status);
    }

    public void update(Member m) throws SQLException {
        Sql.update(
                "UPDATE member SET last_name = ?, first_name = ?, birthdate = ?, sex = ?, contact_number = ?, email = ?, "
              + "membership_type = ?, membership_start = ?, membership_end = ?, status = ? WHERE member_id = ?",
                m.lastName, m.firstName, m.birthdate, m.sex, Sql.nullIfBlank(m.contactNumber), Sql.nullIfBlank(m.email),
                m.membershipType, m.membershipStart, m.membershipEnd, m.status, m.memberId);
    }

    public void delete(int memberId) throws SQLException {
        Sql.update("DELETE FROM member WHERE member_id = ?", memberId);
    }

    public int countAttendance(int memberId) throws SQLException {
        return Sql.count("SELECT COUNT(*) FROM class_attendance WHERE member_id = ?", memberId);
    }

    /** A member is Expired once the membership end date has passed. */
    public int expirePastMemberships() throws SQLException {
        return Sql.update("UPDATE member SET status = 'Expired' WHERE status = 'Active' AND membership_end < CURDATE()");
    }

    private static Member map(ResultSet rs) throws SQLException {
        Member m = new Member();
        m.memberId = rs.getInt("member_id");
        m.lastName = rs.getString("last_name");
        m.firstName = rs.getString("first_name");
        m.birthdate = Sql.date(rs, "birthdate");
        m.sex = rs.getString("sex");
        m.contactNumber = rs.getString("contact_number");
        m.email = rs.getString("email");
        m.membershipType = rs.getString("membership_type");
        m.membershipStart = Sql.date(rs, "membership_start");
        m.membershipEnd = Sql.date(rs, "membership_end");
        m.status = rs.getString("status");
        return m;
    }
}
