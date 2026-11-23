package gym.ui;

import gym.logic.TrainerService;
import gym.logic.ValidationException;
import gym.model.Trainer;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Trainer Record management screen. Owner: Jan Carlo Avelino. */
class TrainerPanel extends RecordPanel {
    private static final long serialVersionUID = 1L;

    private final TrainerService service = new TrainerService();
    private final JComboBox<String> specFilter = UI.filterBox(Trainer.SPECIALIZATIONS);
    private final JComboBox<String> statusFilter = UI.filterBox(Trainer.STATUSES);

    TrainerPanel() {
        super("Trainer Record", "Trainer ID", "Last Name", "First Name", "Specialization",
                "Contact Number", "Email", "Hire Date", "Status");
        addFilter("Specialization:", specFilter);
        addFilter("Status:", statusFilter);
        addFilterButtons();
    }

    @Override
    void reload() {
        try {
            List<Object[]> rows = new ArrayList<>();
            for (Trainer t : service.list(UI.filterValue(specFilter), UI.filterValue(statusFilter))) {
                rows.add(new Object[]{t.trainerId, t.lastName, t.firstName, t.specialization,
                        UI.fmt(t.contactNumber), UI.fmt(t.email), t.hireDate, t.status});
            }
            setRows(rows);
        } catch (SQLException e) {
            UI.error(this, e);
        }
    }

    @Override
    void clearFilters() {
        specFilter.setSelectedIndex(0);
        statusFilter.setSelectedIndex(0);
    }

    @Override
    void onAdd() {
        Trainer t = new Trainer();
        t.specialization = Trainer.SPECIALIZATIONS[0];
        t.hireDate = LocalDate.now();
        t.status = "Active";
        edit(t, true);
    }

    @Override
    void onUpdate() {
        Object key = selectedKey();
        if (key == null) {
            return;
        }
        try {
            Trainer t = service.get((Integer) key);
            if (t != null) {
                edit(t, false);
            }
        } catch (SQLException e) {
            UI.error(this, e);
        }
    }

    private void edit(Trainer t, boolean isNew) {
        JTextField last = new JTextField(t.lastName);
        JTextField first = new JTextField(t.firstName);
        JTextField contact = new JTextField(t.contactNumber);
        JTextField email = new JTextField(t.email);
        JComboBox<String> spec = new JComboBox<>(Trainer.SPECIALIZATIONS);
        spec.setSelectedItem(t.specialization);
        JSpinner hire = UI.dateSpinner(t.hireDate);
        JComboBox<String> status = new JComboBox<>(Trainer.STATUSES);
        status.setSelectedItem(t.status);

        JPanel form = UI.form("Last name", last, "First name", first, "Contact number", contact,
                "Email", email, "Specialization", spec, "Hire date", hire, "Status", status);
        String title = isNew ? "Add Trainer" : "Update Trainer " + t.trainerId;
        while (UI.showForm(this, title, form)) {
            t.lastName = last.getText();
            t.firstName = first.getText();
            t.contactNumber = contact.getText();
            t.email = email.getText();
            t.specialization = (String) spec.getSelectedItem();
            t.hireDate = UI.dateValue(hire);
            t.status = (String) status.getSelectedItem();
            try {
                if (isNew) {
                    t.trainerId = service.add(t);
                    UI.info(this, "Trainer " + t.trainerId + " (" + t.fullName() + ") was added.");
                } else {
                    service.update(t);
                    UI.info(this, "Trainer " + t.trainerId + " was updated.");
                }
                reload();
                selectKey(t.trainerId);
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
        if (!UI.confirm(this, "Delete trainer " + key + "?")) {
            return;
        }
        try {
            service.delete((Integer) key);
            UI.info(this, "Trainer " + key + " was deleted.");
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
            Trainer t = service.get((Integer) key);
            if (t == null) {
                return;
            }
            List<String> classes = service.classNames(t.trainerId);
            StringBuilder text = new StringBuilder()
                    .append("Trainer ID:      ").append(t.trainerId)
                    .append("\nName:            ").append(t.firstName).append(' ').append(t.lastName)
                    .append("\nSpecialization:  ").append(t.specialization)
                    .append("\nContact number:  ").append(UI.fmt(t.contactNumber))
                    .append("\nEmail:           ").append(UI.fmt(t.email))
                    .append("\nHire date:       ").append(t.hireDate)
                    .append("\nStatus:          ").append(t.status)
                    .append("\n\nAssigned classes (").append(classes.size()).append("):");
            if (classes.isEmpty()) {
                text.append("\n  none");
            }
            for (String c : classes) {
                text.append("\n  ").append(c);
            }
            UI.showDetails(this, "Trainer " + t.trainerId, text.toString());
        } catch (SQLException e) {
            UI.error(this, e);
        }
    }
}
