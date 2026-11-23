package gym.data;

import gym.model.GymClass;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Data tier for the gym_class table. Owner: Charlotte Mae Ojeda. */
public class ClassDAO {
    private static final String SELECT =
            "SELECT c.class_code, c.class_name, c.description, c.trainer_id, c.day_of_week, c.start_time, c.end_time, "
          + "c.max_capacity, c.status, CONCAT(t.last_name, ', ', t.first_name) AS trainer_name "
          + "FROM gym_class c JOIN trainer t ON t.trainer_id = c.trainer_id";

    /** List/filter: pass null for a filter to skip it. */
    public List<GymClass> findAll(String dayOfWeek, Integer trainerId) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT).append(" WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        if (dayOfWeek != null) {
            sql.append(" AND c.day_of_week = ?");
            params.add(dayOfWeek);
        }
        if (trainerId != null) {
            sql.append(" AND c.trainer_id = ?");
            params.add(trainerId);
        }
        sql.append(" ORDER BY FIELD(c.day_of_week, 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'), "
                 + "c.start_time, c.class_code");
        return Sql.query(sql.toString(), params, ClassDAO::map);
    }

    public GymClass findByCode(String classCode) throws SQLException {
        return Sql.queryOne(SELECT + " WHERE c.class_code = ?", ClassDAO::map, classCode);
    }

    public void insert(GymClass c) throws SQLException {
        Sql.insert(
                "INSERT INTO gym_class (class_code, class_name, description, trainer_id, day_of_week, start_time, end_time, "
              + "max_capacity, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                c.classCode, c.className, Sql.nullIfBlank(c.description), c.trainerId, c.dayOfWeek,
                c.startTime, c.endTime, c.maxCapacity, c.status);
    }

    public void update(GymClass c) throws SQLException {
        Sql.update(
                "UPDATE gym_class SET class_name = ?, description = ?, trainer_id = ?, day_of_week = ?, start_time = ?, "
              + "end_time = ?, max_capacity = ?, status = ? WHERE class_code = ?",
                c.className, Sql.nullIfBlank(c.description), c.trainerId, c.dayOfWeek, c.startTime,
                c.endTime, c.maxCapacity, c.status, c.classCode);
    }

    public void delete(String classCode) throws SQLException {
        Sql.update("DELETE FROM gym_class WHERE class_code = ?", classCode);
    }

    public int countSessions(String classCode) throws SQLException {
        return Sql.count("SELECT COUNT(*) FROM class_session WHERE class_code = ?", classCode);
    }

    private static GymClass map(ResultSet rs) throws SQLException {
        GymClass c = new GymClass();
        c.classCode = rs.getString("class_code");
        c.className = rs.getString("class_name");
        c.description = rs.getString("description");
        c.trainerId = rs.getInt("trainer_id");
        c.dayOfWeek = rs.getString("day_of_week");
        c.startTime = Sql.time(rs, "start_time");
        c.endTime = Sql.time(rs, "end_time");
        c.maxCapacity = rs.getInt("max_capacity");
        c.status = rs.getString("status");
        c.trainerName = rs.getString("trainer_name");
        return c;
    }
}
