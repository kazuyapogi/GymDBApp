package gym.data;

import gym.model.Attendance;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Data tier for the class_attendance table. Owner: Matthew Mari Manzano. */
public class AttendanceDAO {
    private static final String SELECT =
            "SELECT a.attendance_id, a.member_id, a.session_id, a.time_in, "
          + "CONCAT(m.last_name, ', ', m.first_name) AS member_name, s.session_date, s.class_code, c.class_name, "
          + "CONCAT(t.last_name, ', ', t.first_name) AS trainer_name "
          + "FROM class_attendance a "
          + "JOIN member m ON m.member_id = a.member_id "
          + "JOIN class_session s ON s.session_id = a.session_id "
          + "JOIN gym_class c ON c.class_code = s.class_code "
          + "JOIN trainer t ON t.trainer_id = c.trainer_id";

    /** List/filter by session date range and/or class: pass null for a filter to skip it. */
    public List<Attendance> findAll(LocalDate from, LocalDate to, String classCode) throws SQLException {
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
        sql.append(" ORDER BY s.session_date DESC, s.class_code, a.time_in, a.attendance_id");
        return Sql.query(sql.toString(), params, AttendanceDAO::map);
    }

    public Attendance findById(int attendanceId) throws SQLException {
        return Sql.queryOne(SELECT + " WHERE a.attendance_id = ?", AttendanceDAO::map, attendanceId);
    }

    public int insert(Attendance a) throws SQLException {
        return Sql.insert("INSERT INTO class_attendance (member_id, session_id, time_in) VALUES (?, ?, ?)",
                a.memberId, a.sessionId, a.timeIn);
    }

    public void update(Attendance a) throws SQLException {
        Sql.update("UPDATE class_attendance SET member_id = ?, session_id = ?, time_in = ? WHERE attendance_id = ?",
                a.memberId, a.sessionId, a.timeIn, a.attendanceId);
    }

    public void delete(int attendanceId) throws SQLException {
        Sql.update("DELETE FROM class_attendance WHERE attendance_id = ?", attendanceId);
    }

    private static Attendance map(ResultSet rs) throws SQLException {
        Attendance a = new Attendance();
        a.attendanceId = rs.getInt("attendance_id");
        a.memberId = rs.getInt("member_id");
        a.sessionId = rs.getInt("session_id");
        a.timeIn = Sql.time(rs, "time_in");
        a.memberName = rs.getString("member_name");
        a.sessionDate = Sql.date(rs, "session_date");
        a.classCode = rs.getString("class_code");
        a.className = rs.getString("class_name");
        a.trainerName = rs.getString("trainer_name");
        return a;
    }
}
