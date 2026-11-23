package gym.ui;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Common layout for a record-management screen:
 * heading, filter bar, table, and Add / Update / Delete / View buttons.
 * Each subclass supplies its columns, filters and the four actions.
 */
abstract class RecordPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    protected final DefaultTableModel model;
    protected final JTable table;
    private final JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
    private final JLabel countLabel = new JLabel();

    RecordPanel(String title, String... columns) {
        super(new BorderLayout(0, 6));
        setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        model = UI.tableModel(columns);
        table = UI.table(model);

        filterBar.setBorder(BorderFactory.createTitledBorder("List / Filter"));
        JPanel top = new JPanel(new BorderLayout());
        top.add(UI.heading(title), BorderLayout.NORTH);
        top.add(filterBar, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        buttons.add(UI.button("Add", this::onAdd));
        buttons.add(UI.button("Update", this::onUpdate));
        buttons.add(UI.button("Delete", this::onDelete));
        buttons.add(UI.button("View", this::onView));
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(buttons, BorderLayout.WEST);
        bottom.add(countLabel, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    onView();
                }
            }
        });
    }

    /** Reloads the table using the current filters. */
    abstract void reload();

    /** Resets the filter controls to "All". */
    abstract void clearFilters();

    abstract void onAdd();

    abstract void onUpdate();

    abstract void onDelete();

    abstract void onView();

    /** Refreshes combo boxes that list other records (e.g. trainers, classes). */
    void reloadChoices() {
    }

    /** Called when the tab is opened. */
    void refresh() {
        reloadChoices();
        reload();
    }

    protected void addFilter(String label, JComponent field) {
        filterBar.add(new JLabel(label));
        filterBar.add(field);
    }

    protected void addFilterButtons() {
        filterBar.add(UI.button("Apply filter", this::reload));
        filterBar.add(UI.button("Show all", () -> {
            clearFilters();
            reload();
        }));
    }

    protected void setRows(List<Object[]> rows) {
        model.setRowCount(0);
        for (Object[] r : rows) {
            model.addRow(r);
        }
        model.fireTableStructureChanged();
        UI.fitColumns(table);
        countLabel.setText(rows.size() + " record(s)  ");
    }

    /** Value of the first column (the primary key) of the selected row, or null with a message. */
    protected Object selectedKey() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Select a row in the table first.", "No row selected",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return model.getValueAt(table.convertRowIndexToModel(viewRow), 0);
    }

    /** Selects the row whose first column equals key (after adding or updating). */
    protected void selectKey(Object key) {
        for (int r = 0; r < model.getRowCount(); r++) {
            if (String.valueOf(model.getValueAt(r, 0)).equals(String.valueOf(key))) {
                int v = table.convertRowIndexToView(r);
                if (v >= 0) {
                    table.setRowSelectionInterval(v, v);
                    table.scrollRectToVisible(table.getCellRect(v, 0, true));
                }
                return;
            }
        }
    }
}
