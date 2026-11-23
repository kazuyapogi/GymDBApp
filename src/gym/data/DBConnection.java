package gym.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Data tier: opens connections to the MySQL database.
 * The URL, user and password are read from config/db.properties so each
 * machine can use its own MySQL password without changing the code.
 */
public final class DBConnection {
    private static final String CONFIG_FILE = "config/db.properties";
    private static Properties settings;

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        Properties p = settings();
        return DriverManager.getConnection(
                p.getProperty("db.url"),
                p.getProperty("db.user"),
                p.getProperty("db.password", ""));
    }

    public static String configPath() {
        return new File(CONFIG_FILE).getAbsolutePath();
    }

    private static synchronized Properties settings() {
        if (settings == null) {
            settings = new Properties();
            settings.setProperty("db.url",
                    "jdbc:mysql://localhost:3306/gym_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Manila");
            settings.setProperty("db.user", "root");
            settings.setProperty("db.password", "");
            try (InputStream in = new FileInputStream(CONFIG_FILE)) {
                settings.load(in);
            } catch (IOException e) {
                System.err.println("Could not read " + configPath() + "; using default settings.");
            }
        }
        return settings;
    }
}
