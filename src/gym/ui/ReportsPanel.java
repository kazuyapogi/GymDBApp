package gym.ui;

import gym.logic.ReportService;
import gym.logic.ValidationException;
import gym.model.Member;
import gym.model.ReportResult;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

/** The four monthly reports, one tab each. Each member owns one report. */
class ReportsPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final ReportService service = new ReportService();

    /** Runs one report for the selected month, year and (optionally) membership type. */
    private interface Runner {
        ReportResult run(int month, int year, String membershipType) throws ValidationException, SQLException;
    }

    ReportsPanel() {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        add(UI.heading("Reports"), BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Member Activity", new ReportView(
                "Attendance of every member in held sessions for the month. (Kazuya Niki)",
                false, (m, y, t) -> service.memberActivity(m, y)));
        tabs.addTab("Trainer Workload", new ReportView(
                "Classes handled, sessions conducted and attendees per trainer for the month. (Jan Carlo Avelino)",
                false, (m, y, t) -> service.trainerWorkload(m, y)));
        tabs.addTab("Monthly Class Attendance Summary", new ReportView(
                "Sessions held, attendance, average per session and capacity used per class. (Charlotte Mae Ojeda)",
                false, (m, y, t) -> service.classAttendanceSummary(m, y)));
        tabs.addTab("Membership Engagement", new ReportView(
                "Attendance of members of one membership type whose membership overlaps the month. (Matthew Mari Manzano)",
                true, service::membershipEngagement));
        add(tabs, BorderLayout.CENTER);
    }

    /** One report tab: month/year (and type) inputs, a Generate button and the result table. */
    private final class ReportView extends JPanel {
        private static final long serialVersionUID = 1L;

        private final JComboBox<String> month = new JComboBox<>();
        private final JSpinner year;
        private final JComboBox<String> type = new JComboBox<>(Member.TYPES);
        private final DefaultTableModel model = UI.tableModel();
        private final JTable table = UI.table(model);
        private final JLabel status = new JLabel(" ");
        private final Runner runner;

        ReportView(String description, boolean withType, Runner runner) {
            super(new BorderLayout(0, 6));
            this.runner = runner;
            setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));
            for (Month m : Month.values()) {
                month.addItem(m.getDisplayName(TextStyle.FULL, Locale.ENGLISH));
            }
            LocalDate now = LocalDate.now();
            month.setSelectedIndex(now.getMonthValue() - 1);
            year = new JSpinner(new SpinnerNumberModel(now.getYear(), 2000, 2100, 1));
            year.setEditor(new JSpinner.NumberEditor(year, "#"));

            JPanel inputs = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
            inputs.add(new JLabel("Month:"));
            inputs.add(month);
            inputs.add(new JLabel("Year:"));
            inputs.add(year);
            if (withType) {
                inputs.add(new JLabel("Membership type:"));
                inputs.add(type);
            }
            inputs.add(UI.button("Generate Report", this::generate));
            JPanel top = new JPanel(new BorderLayout());
            top.add(new JLabel(description), BorderLayout.NORTH);
            top.add(inputs, BorderLayout.CENTER);
            add(top, BorderLayout.NORTH);
            add(new JScrollPane(table), BorderLayout.CENTER);
            add(status, BorderLayout.SOUTH);
        }

        private void generate() {
            int m = month.getSelectedIndex() + 1;
            int y = (Integer) year.getValue();
            try {
                ReportResult r = runner.run(m, y, (String) type.getSelectedItem());
                model.setRowCount(0);
                model.setColumnIdentifiers(r.columns);
                for (Object[] row : r.rows) {
                    model.addRow(row);
                }
                model.fireTableStructureChanged();
                UI.fitColumns(table);
                status.setText(r.rows.size() + " row(s) for " + month.getSelectedItem() + " " + y);
            } catch (ValidationException | SQLException e) {
                UI.error(this, e);
            }
        }
    }
}
