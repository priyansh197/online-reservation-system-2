package com.reservation.dao;

import com.reservation.config.DatabaseConnection;
import com.reservation.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserDAO {
    public record Credentials(User user, String passwordHash) { }

    public Optional<Credentials> findCredentials(String username) throws SQLException {
        String sql = "SELECT user_id, full_name, username, password_hash, role FROM users WHERE username = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                User u = new User(rs.getLong("user_id"), rs.getString("full_name"),
                        rs.getString("username"), rs.getString("role"));
                return Optional.of(new Credentials(u, rs.getString("password_hash")));
            }
        }
    }

    /** Throws SQLIntegrityConstraintViolationException if the username already exists. */
    public void insert(String fullName, String username, String passwordHash, String role) throws SQLException {
        String sql = "INSERT INTO users (full_name, username, password_hash, role) VALUES (?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, username);
            ps.setString(3, passwordHash);
            ps.setString(4, role);
            ps.executeUpdate();
        }
    }
}
