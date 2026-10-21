package com.reservation;

import com.reservation.config.DatabaseConnection;
import com.reservation.ui.AppFrame;
import com.reservation.ui.Theme;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public final class Main {
    private static final Logger LOG = Logger.getLogger("reservation");

    private Main() { }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Theme.install();
            try (Connection c = DatabaseConnection.getConnection()) {
                LOG.info("Database connection OK");
            } catch (SQLException e) {
                LOG.log(Level.SEVERE, "Startup database check failed", e);
                JOptionPane.showMessageDialog(null,
                        "Cannot connect to the database.\n\nCheck that MySQL is running, the schema is installed,\n"
                        + "and config/db.properties (or DB_URL / DB_USER / DB_PASSWORD) is correct.",
                        "Database unavailable", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            new AppFrame().setVisible(true);
        });
    }
}
