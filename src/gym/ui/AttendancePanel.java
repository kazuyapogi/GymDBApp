package gym.ui;

import gym.logic.AttendanceService;
import gym.logic.ClassService;
import gym.logic.MemberService;
import gym.logic.SessionService;
import gym.logic.ValidationException;
import gym.model.Attendance;
import gym.model.ClassSession;
import gym.model.GymClass;
import gym.model.Member;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Class Attendance Record management screen. Owner: Matthew Mari Manzano. */
class AttendancePanel extends RecordPanel {
    private static final long serialVersionUID = 1L;

    private final AttendanceService service = new AttendanceService();
    private final MemberService members = new MemberService();
    private final SessionService sessions = new SessionService();
    private final ClassService classes = new ClassService();
    private final JCheckBox useDates = new JCheckBox("Date range");
    private final JSpinner fromDate = UI.dateSpinner(LocalDate.now().withDayOfMonth(1));
    private final JSpinner toDate = UI.dateSpinner(LocalDate.now());
    private final JComboBox<UI.Option> classFilter = UI.optionBox(new ArrayList<>());

    AttendancePanel() {
        super("Class Attendance Record", "Attendance ID", "Session Date", "Class Code", "Class Name",
                "Member ID", "Member Name", "Time In", "Session ID");
        addFilter("", useDates);
        addFilter("From:", fromDate);
        addFilter("To:", toDate);
        addFilter("Class:", classFilter);
        addFilterButtons();
    }

    @Override
    void reloadChoices() {
        try {
            List<UI.Option> options = new ArrayList<>();
            options.add(new UI.Option(null, "All"));
            for (GymClass c : classes.listAll()) {
                options.add(new UI.Option(c.classCode, c.classCode + " - " + c.className));
            }
            UI.setOptions(classFilter, options);
        } catch (SQLException e) {
            UI.error(this, e);
        }
    }

    @Override
    void reload() {
        try {
            LocalDate from = useDates.isSelected() ? UI.dateValue(fromDate) : null;
            LocalDate to = useDates.isSelected() ? UI.dateValue(toDate) : null;
            List<Object[]> rows = new ArrayList<>();
            for (Attendance a : service.list(from, to, (String) UI.selectedValue(classFilter))) {
                rows.add(new Object[]{a.attendanceId, a.sessionDate, a.classCode, a.className, a.memberId,
                        a.memberName, UI.fmt(a.timeIn), a.sessionId});
            }
            setRows(rows);
        } catch (SQLException e) {
            UI.error(this, e);
        }
    }

    @Override
    void clearFilters() {
        useDates.setSelected(false);
        classFilter.setSelectedIndex(0);
    }

    @Override
    void onAdd() {
        Attendance a = new Attendance();
        a.timeIn = LocalTime.now().withSecond(0).withNano(0);
        edit(a, true);
    }

    @Override
    void onUpdate() {
        Object key = selectedKey();
        if (key == null) {
            return;
        }
        try {
            Attendance a = service.get((Integer) key);
            if (a != null) {
                edit(a, false);
            }
        } catch (SQLException e) {
            UI.error(this, e);
        }
    }

    /** Member and session lists show every record so the rules in the logic tier can be demonstrated. */
    private void edit(Attendance a, boolean isNew) {
        List<UI.Option> memberOptions = new ArrayList<>();
        List<UI.Option> sessionOptions = new ArrayList<>();
        try {
            for (Member m : members.listAll()) {
                memberOptions.add(new UI.Option(m.memberId, m.memberId + " - " + m.fullName()
                        + " (" + m.membershipType + ", " + m.status + ")"));
            }
            for (ClassSession s : sessions.listAll()) {
                sessionOptions.add(new UI.Option(s.sessionId, "#" + s.sessionId + "  " + s.sessionDate + "  "
                        + s.classCode + " - " + s.className + " (" + s.status + ")"));
            }
        } catch (SQLException e) {
            UI.error(this, e);
            return;
        }
        JComboBox<UI.Option> member = UI.optionBox(memberOptions);
        JComboBox<UI.Option> session = UI.optionBox(sessionOptions);
        member.setMaximumRowCount(15);
        session.setMaximumRowCount(15);
        if (!isNew) {
            UI.selectValue(member, a.memberId);
            UI.selectValue(session, a.sessionId);
        }
        JSpinner time = UI.timeSpinner(a.timeIn);

        JPanel form = UI.form("Member", member, "Session", session, "Time in", time);
        String title = isNew ? "Record Attendance" : "Update Attendance #" + a.attendanceId;
        while (UI.showForm(this, title, form)) {
            a.memberId = (Integer) UI.selectedValue(member);
            a.sessionId = (Integer) UI.selectedValue(session);
            a.timeIn = UI.timeValue(time);
            try {
                if (isNew) {
                    a.attendanceId = service.add(a);
                    UI.info(this, "Attendance #" + a.attendanceId + " was recorded.");
                } else {
                    service.update(a);
                    UI.info(this, "Attendance #" + a.attendanceId + " was updated.");
                }
                reload();
                selectKey(a.attendanceId);
                return;
            } catch (ValidationException | SQLException e) {
                UI.error(this, e);
            }
        }
    }

    @Override
    void onDelete() {
        Object key = selectedKey();
        if (key == null) {
            return;
        }
        if (!UI.confirm(this, "Delete attendance record #" + key + "?")) {
            return;
        }
        try {
            service.delete((Integer) key);
            UI.info(this, "Attendance #" + key + " was deleted.");
            reload();
        } catch (ValidationException | SQLException e) {
            UI.error(this, e);
        }
    }

    @Override
    void onView() {
        Object key = selectedKey();
        if (key == null) {
            return;
        }
        try {
            Attendance a = service.get((Integer) key);
            if (a == null) {
                return;
            }
            String text = "Attendance ID: " + a.attendanceId
                    + "\nMember:        " + a.memberName + " (ID " + a.memberId + ")"
                    + "\nSession:       #" + a.sessionId + " on " + a.sessionDate
                    + "\nClass:         " + a.classCode + " - " + a.className
                    + "\nTrainer:       " + a.trainerName
                    + "\nTime in:       " + UI.fmt(a.timeIn);
            UI.showDetails(this, "Attendance #" + a.attendanceId, text);
        } catch (SQLException e) {
            UI.error(this, e);
        }
    }
}
