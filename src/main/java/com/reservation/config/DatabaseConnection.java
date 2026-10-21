package com.reservation.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Opens JDBC connections. Credentials come from env vars or
 * config/db.properties (never from source code).
 */
public final class DatabaseConnection {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/online_reservation?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String url, user, password;
    private static boolean loaded;

    private DatabaseConnection() {
    }

    private static synchronized void load() throws SQLException {
        if (loaded)
            return;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found on the classpath.", e);
        }
        Properties p = new Properties();
        Path file = Path.of("config", "db.properties");
        if (Files.exists(file)) {
            try (InputStream in = Files.newInputStream(file)) {
                p.load(in);
            } catch (IOException e) {
                throw new SQLException("Cannot read config/db.properties", e);
            }
        }
        url = pick("DB_URL", "db.url", p, DEFAULT_URL);
        user = pick("DB_USER", "db.user", p, "root");
        password = pick("DB_PASSWORD", "db.password", p, "root");
        loaded = true;
    }

    private static String pick(String env, String key, Properties p, String def) {
        String v = System.getenv(env);
        return (v != null && !v.isBlank()) ? v : p.getProperty(key, def);
    }

    public static Connection getConnection() throws SQLException {
        load();
        return DriverManager.getConnection(url, user, password);
    }
}
