package gym.logic;

import gym.data.TrainerDAO;
import gym.model.Trainer;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Logic tier for the Trainer record. Owner: Jan Carlo Avelino. */
public class TrainerService {
    private final TrainerDAO dao = new TrainerDAO();

    public List<Trainer> list(String specialization, String status) throws SQLException {
        return dao.findAll(specialization, status);
    }

    public List<Trainer> listAll() throws SQLException {
        return dao.findAll(null, null);
    }

    public Trainer get(int trainerId) throws SQLException {
        return dao.findById(trainerId);
    }

    public List<String> classNames(int trainerId) throws SQLException {
        return dao.findClassNames(trainerId);
    }

    public int add(Trainer t) throws ValidationException, SQLException {
        validate(t);
        return dao.insert(t);
    }

    public void update(Trainer t) throws ValidationException, SQLException {
        validate(t);
        dao.update(t);
    }

    /** A trainer assigned to any class cannot be deleted. */
    public void delete(int trainerId) throws ValidationException, SQLException {
        Trainer t = dao.findById(trainerId);
        ValidationException.check(t != null, "Trainer " + trainerId + " was not found.");
        int n = dao.countClasses(trainerId);
        ValidationException.check(n == 0, t.fullName() + " is assigned to " + n + " class(es), so the deletion was blocked.\n"
                + "Reassign the classes first, or set the trainer's status to Inactive.");
        dao.delete(trainerId);
    }

    private void validate(Trainer t) throws ValidationException {
        ValidationException.required(t.lastName, "Last name");
        ValidationException.required(t.firstName, "First name");
        t.lastName = t.lastName.trim();
        t.firstName = t.firstName.trim();
        ValidationException.required(t.specialization, "Specialization");
        ValidationException.check(t.hireDate != null, "Hire date is required.");
        ValidationException.check(!t.hireDate.isAfter(LocalDate.now()), "Hire date cannot be in the future.");
        if (t.email != null && !t.email.trim().isEmpty()) {
            ValidationException.check(t.email.contains("@"), "Email address is not valid.");
        }
        if (t.status == null) {
            t.status = "Active";
        }
    }
}
