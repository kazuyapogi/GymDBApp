package gym.ui;

import gym.logic.MemberService;
import gym.logic.ValidationException;
import gym.model.Member;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Member Record management screen. Owner: Kazuya Niki. */
class MemberPanel extends RecordPanel {
    private static final long serialVersionUID = 1L;

    private final MemberService service = new MemberService();
    private final JComboBox<String> typeFilter = UI.filterBox(Member.TYPES);
    private final JComboBox<String> statusFilter = UI.filterBox(Member.STATUSES);

    MemberPanel() {
        super("Member Record", "Member ID", "Last Name", "First Name", "Sex", "Contact Number",
                "Membership Type", "Start", "End", "Status");
        addFilter("Membership type:", typeFilter);
        addFilter("Status:", statusFilter);
        addFilterButtons();
    }

    @Override
    void reload() {
        try {
            List<Object[]> rows = new ArrayList<>();
            for (Member m : service.list(UI.filterValue(typeFilter), UI.filterValue(statusFilter))) {
                rows.add(new Object[]{m.memberId, m.lastName, m.firstName, m.sex, UI.fmt(m.contactNumber),
                        m.membershipType, m.membershipStart, m.membershipEnd, m.status});
            }
            setRows(rows);
        } catch (SQLException e) {
            UI.error(this, e);
        }
    }

    @Override
    void clearFilters() {
        typeFilter.setSelectedIndex(0);
        statusFilter.setSelectedIndex(0);
    }

    @Override
    void onAdd() {
        Member m = new Member();
        m.membershipType = "Monthly";
        m.membershipStart = LocalDate.now();
        m.membershipEnd = MemberService.suggestedEndDate(m.membershipStart, m.membershipType);
        m.birthdate = LocalDate.now().minusYears(20);
        m.sex = "Male";
        m.status = "Active";
        edit(m, true);
    }

    @Override
    void onUpdate() {
        Object key = selectedKey();
        if (key == null) {
            return;
        }
        try {
            Member m = service.get((Integer) key);
            if (m != null) {
                edit(m, false);
            }
        } catch (SQLException e) {
            UI.error(this, e);
        }
    }

    /** Shows the form until the input is valid or the user cancels; typed values are kept on errors. */
    private void edit(Member m, boolean isNew) {
        JTextField last = new JTextField(m.lastName);
        JTextField first = new JTextField(m.firstName);
        JSpinner birth = UI.dateSpinner(m.birthdate);
        JComboBox<String> sex = new JComboBox<>(Member.SEXES);
        sex.setSelectedItem(m.sex);
        JTextField contact = new JTextField(m.contactNumber);
        JTextField email = new JTextField(m.email);
        JComboBox<String> type = new JComboBox<>(Member.TYPES);
        type.setSelectedItem(m.membershipType);
        JSpinner start = UI.dateSpinner(m.membershipStart);
        JSpinner end = UI.dateSpinner(m.membershipEnd);
        JComboBox<String> status = new JComboBox<>(Member.STATUSES);
        status.setSelectedItem(m.status);

        // Suggest the end date whenever the type or start date changes.
        Runnable suggest = () -> UI.setDate(end,
                MemberService.suggestedEndDate(UI.dateValue(start), (String) type.getSelectedItem()));
        type.addActionListener(e -> suggest.run());
        start.addChangeListener(e -> suggest.run());

        JPanel form = UI.form(
                "Last name", last, "First name", first, "Birthdate", birth, "Sex", sex,
                "Contact number", contact, "Email", email, "Membership type", type,
                "Membership start", start, "Membership end", end, "Status", status);
        String title = isNew ? "Add Member" : "Update Member " + m.memberId;
        while (UI.showForm(this, title, form)) {
            m.lastName = last.getText();
            m.firstName = first.getText();
            m.birthdate = UI.dateValue(birth);
            m.sex = (String) sex.getSelectedItem();
            m.contactNumber = contact.getText();
            m.email = email.getText();
            m.membershipType = (String) type.getSelectedItem();
            m.membershipStart = UI.dateValue(start);
            m.membershipEnd = UI.dateValue(end);
            m.status = (String) status.getSelectedItem();
            try {
                if (isNew) {
                    m.memberId = service.add(m);
                    UI.info(this, "Member " + m.memberId + " (" + m.fullName() + ") was added.");
                } else {
                    service.update(m);
                    UI.info(this, "Member " + m.memberId + " was updated.");
                }
                reload();
                selectKey(m.memberId);
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
        if (!UI.confirm(this, "Delete member " + key + "?")) {
            return;
        }
        try {
            service.delete((Integer) key);
            UI.info(this, "Member " + key + " was deleted.");
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
            Member m = service.get((Integer) key);
            if (m == null) {
                return;
            }
            String text = "Member ID:         " + m.memberId
                    + "\nName:              " + m.firstName + " " + m.lastName
                    + "\nBirthdate:         " + m.birthdate
                    + "\nSex:               " + m.sex
                    + "\nContact number:    " + UI.fmt(m.contactNumber)
                    + "\nEmail:             " + UI.fmt(m.email)
                    + "\nMembership type:   " + m.membershipType
                    + "\nMembership period: " + m.membershipStart + " to " + m.membershipEnd
                    + "\nStatus:            " + m.status
                    + "\nAttendance records (all time): " + service.countAttendance(m.memberId);
            UI.showDetails(this, "Member " + m.memberId, text);
        } catch (SQLException e) {
            UI.error(this, e);
        }
    }
}
