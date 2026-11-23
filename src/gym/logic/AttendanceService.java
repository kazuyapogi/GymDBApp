package gym.logic;

import gym.data.AttendanceDAO;
import gym.data.ClassDAO;
import gym.data.MemberDAO;
import gym.data.SessionDAO;
import gym.model.Attendance;
import gym.model.ClassSession;
import gym.model.GymClass;
import gym.model.Member;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Logic tier for the Class Attendance record. Owner: Matthew Mari Manzano.
 * Follows the approved proposal: the session must be Held, the class Active,
 * and the session date within the member's membership period.
 */
public class AttendanceService {
    private final AttendanceDAO dao = new AttendanceDAO();
    private final MemberDAO memberDao = new MemberDAO();
    private final SessionDAO sessionDao = new SessionDAO();
    private final ClassDAO classDao = new ClassDAO();

    public List<Attendance> list(LocalDate from, LocalDate to, String classCode) throws SQLException {
        return dao.findAll(from, to, classCode);
    }

    public Attendance get(int attendanceId) throws SQLException {
        return dao.findById(attendanceId);
    }

    public int add(Attendance a) throws ValidationException, SQLException {
        validate(a);
        return dao.insert(a);
    }

    public void update(Attendance a) throws ValidationException, SQLException {
        ValidationException.check(dao.findById(a.attendanceId) != null, "Attendance #" + a.attendanceId + " was not found.");
        validate(a);
        dao.update(a);
    }

    public void delete(int attendanceId) throws ValidationException, SQLException {
        ValidationException.check(dao.findById(attendanceId) != null, "Attendance #" + attendanceId + " was not found.");
        dao.delete(attendanceId);
    }

    private void validate(Attendance a) throws ValidationException, SQLException {
        ValidationException.check(a.timeIn != null, "Time in is required.");
        ClassSession s = sessionDao.findById(a.sessionId);
        ValidationException.check(s != null, "Session #" + a.sessionId + " was not found.");
        ValidationException.check("Held".equals(s.status),
                "Session #" + s.sessionId + " (" + s.classCode + ", " + s.sessionDate + ") was Cancelled, so attendance cannot be recorded.");
        Member m = memberDao.findById(a.memberId);
        ValidationException.check(m != null, "Member " + a.memberId + " was not found.");
        ValidationException.check(!s.sessionDate.isBefore(m.membershipStart) && !s.sessionDate.isAfter(m.membershipEnd),
                m.fullName() + "'s membership (" + m.membershipStart + " to " + m.membershipEnd
                        + ") does not cover the session date " + s.sessionDate + ".");
        GymClass c = classDao.findByCode(s.classCode);
        ValidationException.check(c != null && "Active".equals(c.status),
                s.classCode + " is Inactive, so attendance cannot be recorded for its sessions.");
    }
}
