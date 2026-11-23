package gym.data;

import gym.model.Trainer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Data tier for the trainer table. Owner: Jan Carlo Avelino. */
public class TrainerDAO {
    private static final String SELECT =
            "SELECT trainer_id, last_name, first_name, contact_number, email, specialization, hire_date, status FROM trainer";

    /** List/filter: pass null for a filter to skip it. */
    public List<Trainer> findAll(String specialization, String status) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT).append(" WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        if (specialization != null) {
            sql.append(" AND specialization = ?");
            params.add(specialization);
        }
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        sql.append(" ORDER BY trainer_id");
        return Sql.query(sql.toString(), params, TrainerDAO::map);
    }

    public Trainer findById(int trainerId) throws SQLException {
        return Sql.queryOne(SELECT + " WHERE trainer_id = ?", TrainerDAO::map, trainerId);
    }

    public int insert(Trainer t) throws SQLException {
        return Sql.insert(
                "INSERT INTO trainer (last_name, first_name, contact_number, email, specialization, hire_date, status) "
              + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                t.lastName, t.firstName, Sql.nullIfBlank(t.contactNumber), Sql.nullIfBlank(t.email),
                t.specialization, t.hireDate, t.status);
    }

    public void update(Trainer t) throws SQLException {
        Sql.update(
                "UPDATE trainer SET last_name = ?, first_name = ?, contact_number = ?, email = ?, "
              + "specialization = ?, hire_date = ?, status = ? WHERE trainer_id = ?",
                t.lastName, t.firstName, Sql.nullIfBlank(t.contactNumber), Sql.nullIfBlank(t.email),
                t.specialization, t.hireDate, t.status, t.trainerId);
    }

    public void delete(int trainerId) throws SQLException {
        Sql.update("DELETE FROM trainer WHERE trainer_id = ?", trainerId);
    }

    public int countClasses(int trainerId) throws SQLException {
        return Sql.count("SELECT COUNT(*) FROM gym_class WHERE trainer_id = ?", trainerId);
    }

    /** Class codes and names assigned to a trainer, for the View screen. */
    public List<String> findClassNames(int trainerId) throws SQLException {
        List<Object> params = new ArrayList<>();
        params.add(trainerId);
        return Sql.query("SELECT CONCAT(class_code, ' - ', class_name, ' (', day_of_week, ', ', status, ')') AS label "
                       + "FROM gym_class WHERE trainer_id = ? ORDER BY class_code", params, rs -> rs.getString("label"));
    }

    private static Trainer map(ResultSet rs) throws SQLException {
        Trainer t = new Trainer();
        t.trainerId = rs.getInt("trainer_id");
        t.lastName = rs.getString("last_name");
        t.firstName = rs.getString("first_name");
        t.contactNumber = rs.getString("contact_number");
        t.email = rs.getString("email");
        t.specialization = rs.getString("specialization");
        t.hireDate = Sql.date(rs, "hire_date");
        t.status = rs.getString("status");
        return t;
    }
}
