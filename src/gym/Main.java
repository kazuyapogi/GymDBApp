package gym;

import gym.data.DBConnection;
import gym.ui.MainFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Gym Class and Attendance Management System (martial arts gym).
 * CCINFOM S40F Group 5: Avelino, Manzano, Niki, Ojeda.
 *
 * Three tiers:
 *   gym.ui    - presentation (Swing windows and forms)
 *   gym.logic - business rules and input validation
 *   gym.data  - SQL statements run against MySQL through JDBC
 */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            // fall back to the default look and feel
        }
        SwingUtilities.invokeLater(() -> {
            if (testConnection()) {
                new MainFrame().setVisible(true);
            }
        });
    }

    /** Checks the database connection first so setup problems show a clear message. */
    private static boolean testConnection() {
        try (Connection ignored = DBConnection.getConnection()) {
            return true;
        } catch (SQLException e) {
            String hint = e.getMessage() != null && e.getMessage().contains("No suitable driver")
                    ? "The MySQL Connector/J jar is missing. Copy mysql-connector-j-*.jar into the lib folder and run again."
                    : "Check that:\n"
                    + "  1. MySQL is running.\n"
                    + "  2. sql/gym_db.sql was run in MySQL Workbench (it creates gym_db).\n"
                    + "  3. db.user and db.password in " + DBConnection.configPath() + " are correct.";
            JOptionPane.showMessageDialog(null, "Could not connect to the database.\n\n" + e.getMessage() + "\n\n" + hint,
                    "Database connection failed", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}
