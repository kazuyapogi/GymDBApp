package gym.data;

import gym.model.ReportResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Data tier for the four monthly reports. Each report combines and aggregates
 * two or more tables. Only sessions with status 'Held' are counted; cancelled
 * sessions are excluded, and held sessions with zero attendees still count.
 *
 * monthStart and monthEnd are the first and last day of the selected month.
 */
public class ReportDAO {

    /**
     * Member Activity Report. Owner: Kazuya Niki.
     * All members (any status or type) with their attendance in held sessions that month,
     * sorted from most to least active.
     * Tables: member, class_attendance, class_session.
     */
    public ReportResult memberActivity(LocalDate monthStart, LocalDate monthEnd) throws SQLException {
        String sql =
                "SELECT m.member_id, CONCAT(m.last_name, ', ', m.first_name) AS member_name, "
              + "       m.membership_type, m.status, COUNT(s.session_id) AS total_attendances "
              + "FROM member m "
              + "LEFT JOIN class_attendance a ON a.member_id = m.member_id "
              + "LEFT JOIN class_session s ON s.session_id = a.session_id "
              + "      AND s.status = 'Held' AND s.session_date BETWEEN ? AND ? "
              + "GROUP BY m.member_id, m.last_name, m.first_name, m.membership_type, m.status "
              + "ORDER BY total_attendances DESC, member_name";
        ReportResult result = new ReportResult(
                "Member ID", "Member Name", "Membership Type", "Status", "Total Attendances");
        fill(result, sql, monthStart, monthEnd);
        return result;
    }

    /**
     * Trainer Workload Report. Owner: Jan Carlo Avelino.
     * Classes handled  = distinct classes of the trainer with at least one held session that month.
     * Sessions conducted = held sessions that month, including zero-attendee sessions.
     * Total attendees  = attendance records for those sessions.
     * Active trainers with no sessions are shown as zero. Sorted by total attendees.
     * Tables: trainer, gym_class, class_session, class_attendance.
     */
    public ReportResult trainerWorkload(LocalDate monthStart, LocalDate monthEnd) throws SQLException {
        String sql =
                "SELECT t.trainer_id, CONCAT(t.last_name, ', ', t.first_name) AS trainer_name, "
              + "       t.specialization, t.status, "
              + "       COUNT(DISTINCT s.class_code) AS classes_handled, "
              + "       COUNT(DISTINCT s.session_id) AS sessions_conducted, "
              + "       COUNT(a.attendance_id)       AS total_attendees "
              + "FROM trainer t "
              + "LEFT JOIN gym_class c ON c.trainer_id = t.trainer_id "
              + "LEFT JOIN class_session s ON s.class_code = c.class_code "
              + "      AND s.status = 'Held' AND s.session_date BETWEEN ? AND ? "
              + "LEFT JOIN class_attendance a ON a.session_id = s.session_id "
              + "GROUP BY t.trainer_id, t.last_name, t.first_name, t.specialization, t.status "
              + "HAVING t.status = 'Active' OR sessions_conducted > 0 "
              + "ORDER BY total_attendees DESC, trainer_name";
        ReportResult result = new ReportResult(
                "Trainer ID", "Trainer Name", "Specialization", "Status",
                "Classes Handled", "Sessions Conducted", "Total Attendees");
        fill(result, sql, monthStart, monthEnd);
        return result;
    }

    /**
     * Monthly Class Attendance Summary. Owner: Charlotte Mae Ojeda.
     * Capacity used (%) = total attendance / (sessions held x maximum capacity) x 100.
     * Shows active classes, plus inactive classes that still had held sessions that month.
     * Tables: gym_class, trainer, class_session, class_attendance.
     */
    public ReportResult classAttendanceSummary(LocalDate monthStart, LocalDate monthEnd) throws SQLException {
        String sql =
                "SELECT c.class_code, c.class_name, CONCAT(t.last_name, ', ', t.first_name) AS trainer_name, "
              + "       c.day_of_week, c.max_capacity, "
              + "       COUNT(DISTINCT s.session_id) AS sessions_held, "
              + "       COUNT(a.attendance_id)       AS total_attendance, "
              + "       IFNULL(ROUND(COUNT(a.attendance_id) / NULLIF(COUNT(DISTINCT s.session_id), 0), 2), 0) "
              + "                                    AS avg_per_session, "
              + "       IFNULL(ROUND(100 * COUNT(a.attendance_id) "
              + "                    / NULLIF(COUNT(DISTINCT s.session_id) * c.max_capacity, 0), 2), 0) "
              + "                                    AS capacity_used_pct "
              + "FROM gym_class c "
              + "JOIN trainer t ON t.trainer_id = c.trainer_id "
              + "LEFT JOIN class_session s ON s.class_code = c.class_code "
              + "      AND s.status = 'Held' AND s.session_date BETWEEN ? AND ? "
              + "LEFT JOIN class_attendance a ON a.session_id = s.session_id "
              + "GROUP BY c.class_code, c.class_name, t.last_name, t.first_name, c.day_of_week, c.max_capacity, c.status "
              + "HAVING c.status = 'Active' OR sessions_held > 0 "
              + "ORDER BY FIELD(c.day_of_week, 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'), "
              + "         c.class_code";
        ReportResult result = new ReportResult(
                "Class Code", "Class Name", "Trainer", "Day", "Max Capacity",
                "Sessions Held", "Total Attendance", "Avg per Session", "Capacity Used (%)");
        fill(result, sql, monthStart, monthEnd);
        return result;
    }

    /**
     * Membership Engagement Report. Owner: Matthew Mari Manzano.
     * Members of the selected type whose membership overlaps any part of the month
     * (starts on or before the last day and ends on or after the first day).
     * Counts attendance in held sessions within the month AND within the membership period.
     * Tables: member, class_attendance, class_session.
     */
    public ReportResult membershipEngagement(LocalDate monthStart, LocalDate monthEnd, String membershipType)
            throws SQLException {
        String sql =
                "SELECT m.member_id, CONCAT(m.last_name, ', ', m.first_name) AS member_name, "
              + "       m.membership_type, m.membership_start, m.membership_end, "
              + "       COUNT(s.session_id) AS total_attendances "
              + "FROM member m "
              + "LEFT JOIN class_attendance a ON a.member_id = m.member_id "
              + "LEFT JOIN class_session s ON s.session_id = a.session_id "
              + "      AND s.status = 'Held' "
              + "      AND s.session_date BETWEEN ? AND ? "
              + "      AND s.session_date BETWEEN m.membership_start AND m.membership_end "
              + "WHERE m.membership_type = ? "
              + "  AND m.membership_start <= ? "
              + "  AND m.membership_end >= ? "
              + "GROUP BY m.member_id, m.last_name, m.first_name, m.membership_type, m.membership_start, m.membership_end "
              + "ORDER BY total_attendances DESC, member_name";
        ReportResult result = new ReportResult(
                "Member ID", "Member Name", "Membership Type", "Membership Start", "Membership End", "Total Attendances");
        fill(result, sql, monthStart, monthEnd, membershipType, monthEnd, monthStart);
        return result;
    }

    private static void fill(ReportResult result, String sql, Object... params) throws SQLException {
        List<Object> list = Arrays.asList(params);
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            Sql.bind(ps, list);
            try (ResultSet rs = ps.executeQuery()) {
                int n = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    Object[] row = new Object[n];
                    for (int i = 0; i < n; i++) {
                        Object value = rs.getObject(i + 1);
                        if (value instanceof java.sql.Date) {
                            value = ((java.sql.Date) value).toLocalDate();
                        }
                        row[i] = value;
                    }
                    result.rows.add(row);
                }
            }
        }
    }
}
