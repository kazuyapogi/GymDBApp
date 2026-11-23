package gym.ui;

import gym.logic.ClassService;
import gym.logic.TrainerService;
import gym.logic.ValidationException;
import gym.model.GymClass;
import gym.model.Trainer;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Class Record management screen. Owner: Charlotte Mae Ojeda. */
class ClassPanel extends RecordPanel {
    private static final long serialVersionUID = 1L;

    private final ClassService service = new ClassService();
    private final TrainerService trainers = new TrainerService();
    private final JComboBox<String> dayFilter = UI.filterBox(GymClass.DAYS);
    private final JComboBox<UI.Option> trainerFilter = UI.optionBox(new ArrayList<>());

    ClassPanel() {
        super("Class Record", "Class Code", "Class Name", "Trainer", "Day", "Start", "End",
                "Max Capacity", "Status");
        addFilter("Day:", dayFilter);
        addFilter("Trainer:", trainerFilter);
        addFilterButtons();
    }

    @Override
    void reloadChoices() {
        try {
            List<UI.Option> options = new ArrayList<>();
            options.add(new UI.Option(null, "All"));
            for (Trainer t : trainers.listAll()) {
                options.add(new UI.Option(t.trainerId, t.fullName()));
            }
            UI.setOptions(trainerFilter, options);
        } catch (SQLException e) {
            UI.error(this, e);
        }
    }

    @Override
    void reload() {
        try {
            List<Object[]> rows = new ArrayList<>();
            for (GymClass c : service.list(UI.filterValue(dayFilter), (Integer) UI.selectedValue(trainerFilter))) {
                rows.add(new Object[]{c.classCode, c.className, c.trainerName, c.dayOfWeek,
                        UI.fmt(c.startTime), UI.fmt(c.endTime), c.maxCapacity, c.status});
            }
            setRows(rows);
        } catch (SQLException e) {
            UI.error(this, e);
        }
    }

    @Override
    void clearFilters() {
        dayFilter.setSelectedIndex(0);
        trainerFilter.setSelectedIndex(0);
    }

    @Override
    void onAdd() {
        GymClass c = new GymClass();
        c.dayOfWeek = "Monday";
        c.startTime = LocalTime.of(18, 0);
        c.endTime = LocalTime.of(19, 0);
        c.maxCapacity = 10;
        c.status = "Active";
        edit(c, true);
    }

    @Override
    void onUpdate() {
        Object key = selectedKey();
        if (key == null) {
            return;
        }
        try {
            GymClass c = service.get((String) key);
            if (c != null) {
                edit(c, false);
            }
        } catch (SQLException e) {
            UI.error(this, e);
        }
    }

    private void edit(GymClass c, boolean isNew) {
        List<UI.Option> trainerOptions = new ArrayList<>();
        try {
            // Every trainer is listed; ClassService rejects an Inactive one with a clear message.
            for (Trainer t : trainers.listAll()) {
                trainerOptions.add(new UI.Option(t.trainerId,
                        t.fullName() + " - " + t.specialization + ("Active".equals(t.status) ? "" : " (Inactive)")));
            }
        } catch (SQLException e) {
            UI.error(this, e);
            return;
        }
        JTextField code = new JTextField(c.classCode);
        code.setEditable(isNew);
        JTextField name = new JTextField(c.className);
        JTextField desc = new JTextField(c.description);
        JComboBox<UI.Option> trainer = UI.optionBox(trainerOptions);
        if (!isNew) {
            UI.selectValue(trainer, c.trainerId);
        }
        JComboBox<String> day = new JComboBox<>(GymClass.DAYS);
        day.setSelectedItem(c.dayOfWeek);
        JSpinner start = UI.timeSpinner(c.startTime);
        JSpinner end = UI.timeSpinner(c.endTime);
        JSpinner cap = new JSpinner(new SpinnerNumberModel(c.maxCapacity, 0, 500, 1));
        JComboBox<String> status = new JComboBox<>(GymClass.STATUSES);
        status.setSelectedItem(c.status);

        JPanel form = UI.form("Class code", code, "Class name", name, "Description", desc, "Trainer", trainer,
                "Day of week", day, "Start time", start, "End time", end, "Max capacity", cap, "Status", status);
        String title = isNew ? "Add Class" : "Update Class " + c.classCode;
        while (UI.showForm(this, title, form)) {
            c.classCode = code.getText();
            c.className = name.getText();
            c.description = desc.getText();
            Object tid = UI.selectedValue(trainer);
            c.trainerId = tid == null ? 0 : (Integer) tid;
            c.dayOfWeek = (String) day.getSelectedItem();
            c.startTime = UI.timeValue(start);
            c.endTime = UI.timeValue(end);
            c.maxCapacity = (Integer) cap.getValue();
            c.status = (String) status.getSelectedItem();
            try {
                if (isNew) {
                    service.add(c);
                    UI.info(this, "Class " + c.classCode + " was added.");
                } else {
                    service.update(c);
                    UI.info(this, "Class " + c.classCode + " was updated.");
                }
                reload();
                selectKey(c.classCode);
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
        if (!UI.confirm(this, "Delete class " + key + "?")) {
            return;
        }
        try {
            service.delete((String) key);
            UI.info(this, "Class " + key + " was deleted.");
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
            GymClass c = service.get((String) key);
            if (c == null) {
                return;
            }
            String text = "Class code:    " + c.classCode
                    + "\nClass name:    " + c.className
                    + "\nDescription:   " + UI.fmt(c.description)
                    + "\nTrainer:       " + c.trainerName + " (ID " + c.trainerId + ")"
                    + "\nSchedule:      " + c.dayOfWeek + "s, " + UI.fmt(c.startTime) + " to " + UI.fmt(c.endTime)
                    + "\nMax capacity:  " + c.maxCapacity
                    + "\nStatus:        " + c.status
                    + "\nSession records: " + service.countSessions(c.classCode);
            UI.showDetails(this, "Class " + c.classCode, text);
        } catch (SQLException e) {
            UI.error(this, e);
        }
    }
}
