package gym.data;

import gym.model.ClassSession;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Data tier for the class_session table. */
public class SessionDAO {
    private static final String SELECT =
            "SELECT s.session_id, s.class_code, s.session_date, s.status, c.class_name, c.day_of_week, "
          + "c.status AS class_status, CONCAT(t.last_name, ', ', t.first_name) AS trainer_name, "
          + "(SELECT COUNT(*) FROM class_attendance a WHERE a.session_id = s.session_id) AS attendee_count "
          + "FROM class_session s "
          + "JOIN gym_class c ON c.class_code = s.class_code "
          + "JOIN trainer t ON t.trainer_id = c.trainer_id";

    /** List/filter by date range, class and/or status: pass null for a filter to skip it. */
    public List<ClassSession> findAll(LocalDate from, LocalDate to, String classCode, String status) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT).append(" WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        if (from != null) {
            sql.append(" AND s.session_date >= ?");
            params.add(from);
        }
        if (to != null) {
            sql.append(" AND s.session_date <= ?");
            params.add(to);
        }
        if (classCode != null) {
            sql.append(" AND s.class_code = ?");
            params.add(classCode);
        }
        if (status != null) {
            sql.append(" AND s.status = ?");
            params.add(status);
        }
        sql.append(" ORDER BY s.session_date DESC, c.start_time DESC, s.session_id DESC");
        return Sql.query(sql.toString(), params, SessionDAO::map);
    }

    public ClassSession findById(int sessionId) throws SQLException {
        return Sql.queryOne(SELECT + " WHERE s.session_id = ?", SessionDAO::map, sessionId);
    }

    public int insert(ClassSession s) throws SQLException {
        return Sql.insert("INSERT INTO class_session (class_code, session_date, status) VALUES (?, ?, ?)",
                s.classCode, s.sessionDate, s.status);
    }

    /** Only the date and status of a session can be edited. */
    public void update(ClassSession s) throws SQLException {
        Sql.update("UPDATE class_session SET session_date = ?, status = ? WHERE session_id = ?",
                s.sessionDate, s.status, s.sessionId);
    }

    public void delete(int sessionId) throws SQLException {
        Sql.update("DELETE FROM class_session WHERE session_id = ?", sessionId);
    }

    public int countAttendance(int sessionId) throws SQLException {
        return Sql.count("SELECT COUNT(*) FROM class_attendance WHERE session_id = ?", sessionId);
    }

    /** True if the class already has another session on that date (excludeSessionId is ignored, 0 for none). */
    public boolean existsForClassOnDate(String classCode, LocalDate date, int excludeSessionId) throws SQLException {
        return Sql.count("SELECT COUNT(*) FROM class_session WHERE class_code = ? AND session_date = ? AND session_id <> ?",
                classCode, date, excludeSessionId) > 0;
    }

    private static ClassSession map(ResultSet rs) throws SQLException {
        ClassSession s = new ClassSession();
        s.sessionId = rs.getInt("session_id");
        s.classCode = rs.getString("class_code");
        s.sessionDate = Sql.date(rs, "session_date");
        s.status = rs.getString("status");
        s.className = rs.getString("class_name");
        s.dayOfWeek = rs.getString("day_of_week");
        s.classStatus = rs.getString("class_status");
        s.trainerName = rs.getString("trainer_name");
        s.attendeeCount = rs.getInt("attendee_count");
        return s;
    }
}
