package gym.logic;

import gym.data.ClassDAO;
import gym.data.TrainerDAO;
import gym.model.GymClass;
import gym.model.Trainer;

import java.sql.SQLException;
import java.util.List;

/** Logic tier for the Class record. Owner: Charlotte Mae Ojeda. */
public class ClassService {
    private final ClassDAO dao = new ClassDAO();
    private final TrainerDAO trainerDao = new TrainerDAO();

    public List<GymClass> list(String dayOfWeek, Integer trainerId) throws SQLException {
        return dao.findAll(dayOfWeek, trainerId);
    }

    public List<GymClass> listAll() throws SQLException {
        return dao.findAll(null, null);
    }

    public GymClass get(String classCode) throws SQLException {
        return dao.findByCode(classCode);
    }

    public int countSessions(String classCode) throws SQLException {
        return dao.countSessions(classCode);
    }

    /** New classes need a unique code and an Active trainer. */
    public void add(GymClass c) throws ValidationException, SQLException {
        ValidationException.required(c.classCode, "Class code");
        c.classCode = c.classCode.trim().toUpperCase();
        ValidationException.check(c.classCode.length() <= 10, "Class code can have at most 10 characters.");
        ValidationException.check(dao.findByCode(c.classCode) == null, "Class code " + c.classCode + " already exists.");
        validate(c);
        checkTrainer(c.trainerId);
        dao.insert(c);
    }

    /** The trainer is only re-checked when it was changed. */
    public void update(GymClass c) throws ValidationException, SQLException {
        GymClass old = dao.findByCode(c.classCode);
        ValidationException.check(old != null, "Class " + c.classCode + " was not found.");
        validate(c);
        if (old.trainerId != c.trainerId) {
            checkTrainer(c.trainerId);
        }
        dao.update(c);
    }

    /** A class with session records cannot be deleted. */
    public void delete(String classCode) throws ValidationException, SQLException {
        GymClass c = dao.findByCode(classCode);
        ValidationException.check(c != null, "Class " + classCode + " was not found.");
        int n = dao.countSessions(classCode);
        ValidationException.check(n == 0, c.classCode + " has " + n + " session record(s), so the deletion was cancelled.\n"
                + "Set the class status to Inactive instead.");
        dao.delete(classCode);
    }

    private void checkTrainer(int trainerId) throws ValidationException, SQLException {
        Trainer t = trainerDao.findById(trainerId);
        ValidationException.check(t != null, "Trainer " + trainerId + " was not found.");
        ValidationException.check("Active".equals(t.status), t.fullName() + " is Inactive and cannot be assigned to a class.");
    }

    private void validate(GymClass c) throws ValidationException {
        ValidationException.required(c.className, "Class name");
        c.className = c.className.trim();
        ValidationException.check(c.dayOfWeek != null, "Day of the week is required.");
        ValidationException.check(c.startTime != null && c.endTime != null, "Start and end times are required.");
        ValidationException.check(c.endTime.isAfter(c.startTime), "End time must be later than the start time.");
        ValidationException.check(c.maxCapacity > 0, "Maximum capacity must be greater than zero.");
        if (c.status == null) {
            c.status = "Active";
        }
    }
}
