package gym.ui;

import gym.logic.ValidationException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/** Small Swing helpers shared by every screen so the panels stay short. */
final class UI {
    static final Color ACCENT = new Color(0x23, 0x47, 0xC8);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("h:mm a");

    private UI() {
    }

    // ---------- combo boxes ----------

    /** A combo box item: the value stored in the database plus the label shown to the user. */
    static final class Option {
        final Object value;
        final String label;

        Option(Object value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    static JComboBox<Option> optionBox(List<Option> options) {
        JComboBox<Option> box = new JComboBox<>();
        setOptions(box, options);
        return box;
    }

    static void setOptions(JComboBox<Option> box, List<Option> options) {
        Object keep = selectedValue(box);
        box.removeAllItems();
        for (Option o : options) {
            box.addItem(o);
        }
        if (keep != null) {
            selectValue(box, keep);
        }
    }

    static Object selectedValue(JComboBox<Option> box) {
        Option o = (Option) box.getSelectedItem();
        return o == null ? null : o.value;
    }

    static void selectValue(JComboBox<Option> box, Object value) {
        for (int i = 0; i < box.getItemCount(); i++) {
            Object v = box.getItemAt(i).value;
            if (v == null ? value == null : v.equals(value)) {
                box.setSelectedIndex(i);
                return;
            }
        }
    }

    /** A filter combo whose first item is "All" (meaning no filter). */
    static JComboBox<String> filterBox(String... values) {
        JComboBox<String> box = new JComboBox<>();
        box.addItem("All");
        for (String v : values) {
            box.addItem(v);
        }
        return box;
    }

    static String filterValue(JComboBox<String> box) {
        Object v = box.getSelectedItem();
        return v == null || "All".equals(v) ? null : v.toString();
    }

    // ---------- date and time inputs ----------

    static JSpinner dateSpinner(LocalDate value) {
        JSpinner s = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        s.setEditor(new JSpinner.DateEditor(s, "yyyy-MM-dd"));
        setDate(s, value == null ? LocalDate.now() : value);
        return s;
    }

    static LocalDate dateValue(JSpinner s) {
        commit(s);
        Date d = (Date) s.getValue();
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    static void setDate(JSpinner s, LocalDate value) {
        s.setValue(Date.from(value.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    static JSpinner timeSpinner(LocalTime value) {
        JSpinner s = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE));
        s.setEditor(new JSpinner.DateEditor(s, "HH:mm"));
        LocalTime t = value == null ? LocalTime.now().withSecond(0).withNano(0) : value;
        s.setValue(Date.from(t.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant()));
        return s;
    }

    static LocalTime timeValue(JSpinner s) {
        commit(s);
        Date d = (Date) s.getValue();
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);
    }

    /** Accepts what the user typed even if they did not press Enter. */
    private static void commit(JSpinner s) {
        try {
            s.commitEdit();
        } catch (ParseException ignored) {
            // keep the last valid value
        }
    }

    static String fmt(LocalTime t) {
        return t == null ? "" : t.format(TIME_FMT);
    }

    static String fmt(Object o) {
        return o == null ? "" : o.toString();
    }

    // ---------- tables ----------

    /** Read-only table model; Number columns are right-aligned by the default renderer. */
    static DefaultTableModel tableModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                for (int r = 0; r < getRowCount(); r++) {
                    Object v = getValueAt(r, c);
                    if (v != null) {
                        return v instanceof Number ? Number.class : Object.class;
                    }
                }
                return Object.class;
            }
        };
    }

    static JTable table(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setRowHeight(24);
        t.setAutoCreateRowSorter(true);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        t.getTableHeader().setReorderingAllowed(false);
        t.setFillsViewportHeight(true);
        DefaultTableCellRenderer num = new DefaultTableCellRenderer();
        num.setHorizontalAlignment(JLabel.RIGHT);
        t.setDefaultRenderer(Number.class, num);
        return t;
    }

    /** Sizes each column to fit its header and contents so long names are not cut off. */
    static void fitColumns(JTable table) {
        int rows = Math.min(table.getRowCount(), 300);
        for (int c = 0; c < table.getColumnCount(); c++) {
            TableColumn col = table.getColumnModel().getColumn(c);
            TableCellRenderer hr = col.getHeaderRenderer() != null ? col.getHeaderRenderer()
                    : table.getTableHeader().getDefaultRenderer();
            Component hc = hr.getTableCellRendererComponent(table, col.getHeaderValue(), false, false, 0, c);
            int width = hc.getPreferredSize().width;
            for (int r = 0; r < rows; r++) {
                Component cc = table.prepareRenderer(table.getCellRenderer(r, c), r, c);
                width = Math.max(width, cc.getPreferredSize().width);
            }
            col.setPreferredWidth(width + 16);
        }
    }

    // ---------- forms and dialogs ----------

    /** Two-column form: label on the left, field on the right. */
    static JPanel form(Object... labelAndField) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.anchor = GridBagConstraints.WEST;
        for (int i = 0; i < labelAndField.length; i += 2) {
            g.gridy = i / 2;
            g.gridx = 0;
            g.weightx = 0;
            g.fill = GridBagConstraints.NONE;
            p.add(new JLabel(labelAndField[i] + ":"), g);
            g.gridx = 1;
            g.weightx = 1;
            g.fill = GridBagConstraints.HORIZONTAL;
            Component field = (Component) labelAndField[i + 1];
            if (field instanceof JComponent) {
                Dimension d = field.getPreferredSize();
                ((JComponent) field).setPreferredSize(new Dimension(Math.max(d.width, 280), d.height));
            }
            p.add(field, g);
        }
        return p;
    }

    /** Shows a form in an OK/Cancel dialog. Returns true if OK was pressed. */
    static boolean showForm(Component parent, String title, JComponent form) {
        int r = JOptionPane.showConfirmDialog(parent, form, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return r == JOptionPane.OK_OPTION;
    }

    static void showDetails(Component parent, String title, String text) {
        JTextArea area = textArea(text);
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(520, Math.min(420, 40 + 20 * text.split("\n").length)));
        JOptionPane.showMessageDialog(parent, sp, title, JOptionPane.PLAIN_MESSAGE);
    }

    static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Done", JOptionPane.INFORMATION_MESSAGE);
    }

    static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Please confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /** Rule violations are warnings; database problems are errors. */
    static void error(Component parent, Exception e) {
        if (e instanceof ValidationException) {
            JOptionPane.showMessageDialog(parent, e.getMessage(), "Not allowed", JOptionPane.WARNING_MESSAGE);
        } else if (e instanceof SQLException) {
            JOptionPane.showMessageDialog(parent, "The database reported a problem:\n" + e.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(parent, String.valueOf(e), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------- small widgets ----------

    static JButton button(String text, Runnable action) {
        JButton b = new JButton(text);
        b.addActionListener(e -> action.run());
        return b;
    }

    static JTextArea textArea(String text) {
        JTextArea a = new JTextArea(text);
        a.setEditable(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        a.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        return a;
    }

    static JLabel heading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 18f));
        l.setForeground(ACCENT);
        l.setBorder(BorderFactory.createEmptyBorder(4, 2, 8, 2));
        return l;
    }
}
