package gym.ui;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

/** Main window: one tab per record plus the Reports tab. */
public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    public MainFrame() {
        super("Gym Class and Attendance Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UI.ACCENT);
        header.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JLabel title = new JLabel("Gym Class and Attendance Management System");
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        JLabel sub = new JLabel("Martial arts gym  |  CCINFOM S40F Group 5");
        sub.setForeground(new Color(0xDD, 0xE4, 0xFF));
        header.add(title, BorderLayout.NORTH);
        header.add(sub, BorderLayout.SOUTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Members", new MemberPanel());
        tabs.addTab("Trainers", new TrainerPanel());
        tabs.addTab("Classes", new ClassPanel());
        tabs.addTab("Class Sessions", new SessionPanel());
        tabs.addTab("Attendance", new AttendancePanel());
        tabs.addTab("Reports", new ReportsPanel());
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedComponent() instanceof RecordPanel) {
                ((RecordPanel) tabs.getSelectedComponent()).refresh();
            }
        });
        ((RecordPanel) tabs.getComponentAt(0)).refresh();

        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        setSize(1200, 720);
        setLocationRelativeTo(null);
    }
}
