package gym.logic;

import gym.data.ClassDAO;
import gym.data.SessionDAO;
import gym.model.ClassSession;
import gym.model.GymClass;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

/** Logic tier for the Class Session record. */
public class SessionService {
    private final SessionDAO dao = new SessionDAO();
    private final ClassDAO classDao = new ClassDAO();

    public List<ClassSession> list(LocalDate from, LocalDate to, String classCode, String status) throws SQLException {
        return dao.findAll(from, to, classCode, status);
    }

    public List<ClassSession> listAll() throws SQLException {
        return dao.findAll(null, null, null, null);
    }

    public ClassSession get(int sessionId) throws SQLException {
        return dao.findById(sessionId);
    }

    /** Adds a session for an Active class on a date that falls on the class's day of the week. */
    public int add(ClassSession s) throws ValidationException, SQLException {
        ValidationException.check(s.classCode != null, "Class is required.");
        GymClass c = classDao.findByCode(s.classCode);
        ValidationException.check(c != null, "Class " + s.classCode + " was not found.");
        ValidationException.check("Active".equals(c.status), c.classCode + " is Inactive, so no new sessions can be added.");
        checkDate(s, c);
        checkStatus(s);
        return dao.insert(s);
    }

    /** Only the date and status can change; the class stays the same. */
    public void update(ClassSession s) throws ValidationException, SQLException {
        ClassSession old = dao.findById(s.sessionId);
        ValidationException.check(old != null, "Session " + s.sessionId + " was not found.");
        s.classCode = old.classCode;
        GymClass c = classDao.findByCode(s.classCode);
        checkDate(s, c);
        checkStatus(s);
        dao.update(s);
    }

    /** A session with attendance records cannot be deleted. */
    public void delete(int sessionId) throws ValidationException, SQLException {
        ClassSession s = dao.findById(sessionId);
        ValidationException.check(s != null, "Session " + sessionId + " was not found.");
        int n = dao.countAttendance(sessionId);
        ValidationException.check(n == 0, "Session #" + sessionId + " has " + n + " attendance record(s), so it cannot be deleted.\n"
                + "Set the session status to Cancelled instead if it did not happen.");
        dao.delete(sessionId);
    }

    public static String dayName(LocalDate date) {
        return date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }

    private void checkDate(ClassSession s, GymClass c) throws ValidationException, SQLException {
        ValidationException.check(s.sessionDate != null, "Session date is required.");
        ValidationException.check(dayName(s.sessionDate).equals(c.dayOfWeek),
                c.classCode + " meets on " + c.dayOfWeek + "s, but " + s.sessionDate + " is a " + dayName(s.sessionDate) + ".");
        ValidationException.check(!dao.existsForClassOnDate(s.classCode, s.sessionDate, s.sessionId),
                c.classCode + " already has a session on " + s.sessionDate + ".");
    }

    private void checkStatus(ClassSession s) throws ValidationException {
        if (s.status == null) {
            s.status = "Held";
        }
        ValidationException.check("Held".equals(s.status) || "Cancelled".equals(s.status), "Status must be Held or Cancelled.");
    }
}
