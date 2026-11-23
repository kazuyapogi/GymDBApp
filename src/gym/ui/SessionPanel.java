package gym.ui;

import gym.logic.ClassService;
import gym.logic.SessionService;
import gym.logic.ValidationException;
import gym.model.ClassSession;
import gym.model.GymClass;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/** Class Session Record management screen. */
class SessionPanel extends RecordPanel {
    private static final long serialVersionUID = 1L;

    private final SessionService service = new SessionService();
    private final ClassService classes = new ClassService();
    private final JCheckBox useDates = new JCheckBox("Date range");
    private final JSpinner fromDate = UI.dateSpinner(LocalDate.now().withDayOfMonth(1));
    private final JSpinner toDate = UI.dateSpinner(LocalDate.now());
    private final JComboBox<UI.Option> classFilter = UI.optionBox(new ArrayList<>());
    private final JComboBox<String> statusFilter = UI.filterBox(ClassSession.STATUSES);

    SessionPanel() {
        super("Class Session Record", "Session ID", "Date", "Day", "Class Code", "Class Name", "Trainer",
                "Status", "Attendees");
        addFilter("", useDates);
        addFilter("From:", fromDate);
        addFilter("To:", toDate);
        addFilter("Class:", classFilter);
        addFilter("Status:", statusFilter);
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
            for (ClassSession s : service.list(from, to, (String) UI.selectedValue(classFilter), UI.filterValue(statusFilter))) {
                rows.add(new Object[]{s.sessionId, s.sessionDate, SessionService.dayName(s.sessionDate), s.classCode,
                        s.className, s.trainerName, s.status, s.attendeeCount});
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
        statusFilter.setSelectedIndex(0);
    }

    @Override
    void onAdd() {
        List<UI.Option> options = new ArrayList<>();
        List<GymClass> all = new ArrayList<>();
        try {
            // Every class is listed; SessionService rejects an Inactive one with a clear message.
            for (GymClass c : classes.listAll()) {
                all.add(c);
                options.add(new UI.Option(c.classCode, c.classCode + " - " + c.className + " (" + c.dayOfWeek + " "
                        + UI.fmt(c.startTime) + ("Active".equals(c.status) ? "" : ", Inactive") + ")"));
            }
        } catch (SQLException e) {
            UI.error(this, e);
            return;
        }
        JComboBox<UI.Option> cls = UI.optionBox(options);
        JSpinner date = UI.dateSpinner(LocalDate.now());
        JComboBox<String> status = new JComboBox<>(ClassSession.STATUSES);

        // Picking a class moves the date to the next day that class meets.
        Runnable jump = () -> {
            Object code = UI.selectedValue(cls);
            for (GymClass c : all) {
                if (c.classCode.equals(code)) {
                    DayOfWeek d = DayOfWeek.valueOf(c.dayOfWeek.toUpperCase());
                    UI.setDate(date, LocalDate.now().with(TemporalAdjusters.nextOrSame(d)));
                }
            }
        };
        cls.addActionListener(e -> jump.run());
        jump.run();

        JPanel form = UI.form("Class", cls, "Session date", date, "Status", status);
        ClassSession s = new ClassSession();
        while (UI.showForm(this, "Add Class Session", form)) {
            s.classCode = (String) UI.selectedValue(cls);
            s.sessionDate = UI.dateValue(date);
            s.status = (String) status.getSelectedItem();
            try {
                s.sessionId = service.add(s);
                UI.info(this, "Session #" + s.sessionId + " (" + s.classCode + ", " + s.sessionDate + ") was added.");
                reload();
                selectKey(s.sessionId);
                return;
            } catch (ValidationException | SQLException e) {
                UI.error(this, e);
            }
        }
    }

    @Override
    void onUpdate() {
        Object key = selectedKey();
        if (key == null) {
            return;
        }
        ClassSession s;
        try {
            s = service.get((Integer) key);
        } catch (SQLException e) {
            UI.error(this, e);
            return;
        }
        if (s == null) {
            return;
        }
        JTextField cls = new JTextField(s.classCode + " - " + s.className + " (" + s.dayOfWeek + "s)");
        cls.setEditable(false);
        JSpinner date = UI.dateSpinner(s.sessionDate);
        JComboBox<String> status = new JComboBox<>(ClassSession.STATUSES);
        status.setSelectedItem(s.status);
        JPanel form = UI.form("Class", cls, "Session date", date, "Status", status);
        while (UI.showForm(this, "Update Session #" + s.sessionId, form)) {
            s.sessionDate = UI.dateValue(date);
            s.status = (String) status.getSelectedItem();
            try {
                service.update(s);
                UI.info(this, "Session #" + s.sessionId + " was updated.");
                reload();
                selectKey(s.sessionId);
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
        if (!UI.confirm(this, "Delete session #" + key + "?")) {
            return;
        }
        try {
            service.delete((Integer) key);
            UI.info(this, "Session #" + key + " was deleted.");
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
            ClassSession s = service.get((Integer) key);
            if (s == null) {
                return;
            }
            String text = "Session ID:   " + s.sessionId
                    + "\nClass:        " + s.classCode + " - " + s.className + " (class is " + s.classStatus + ")"
                    + "\nTrainer:      " + s.trainerName
                    + "\nDate:         " + s.sessionDate + " (" + SessionService.dayName(s.sessionDate) + ")"
                    + "\nStatus:       " + s.status
                    + "\nAttendees:    " + s.attendeeCount;
            UI.showDetails(this, "Session #" + s.sessionId, text);
        } catch (SQLException e) {
            UI.error(this, e);
        }
    }
}
