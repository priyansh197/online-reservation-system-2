package com.reservation.dao;

import com.reservation.config.DatabaseConnection;
import com.reservation.model.Train;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TrainDAO {
    private static final String COLS = "train_number, train_name, source_station, destination_station, active";

    private Train map(ResultSet rs) throws SQLException {
        return new Train(rs.getInt("train_number"), rs.getString("train_name"),
                rs.getString("source_station"), rs.getString("destination_station"), rs.getBoolean("active"));
    }

    private List<Train> query(String sql, String param) throws SQLException {
        List<Train> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (param != null) {
                ps.setString(1, param);
                ps.setString(2, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public Optional<Train> findByNumber(int number) throws SQLException {
        String sql = "SELECT " + COLS + " FROM trains WHERE train_number = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, number);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public List<Train> listActive() throws SQLException {
        return query("SELECT " + COLS + " FROM trains WHERE active = TRUE ORDER BY train_number", null);
    }

    public List<Train> listAll() throws SQLException {
        return query("SELECT " + COLS + " FROM trains ORDER BY train_number", null);
    }

    public List<Train> search(String text) throws SQLException {
        String like = "%" + text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
        return query("SELECT " + COLS + " FROM trains WHERE CAST(train_number AS CHAR) LIKE ? "
                + "OR train_name LIKE ? ORDER BY train_number", like);
    }

    /** Throws SQLIntegrityConstraintViolationException for a duplicate train number. */
    public void insert(Train t) throws SQLException {
        String sql = "INSERT INTO trains (" + COLS + ") VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, t.number());
            ps.setString(2, t.name());
            ps.setString(3, t.source());
            ps.setString(4, t.destination());
            ps.setBoolean(5, t.active());
            ps.executeUpdate();
        }
    }

    public boolean update(Train t) throws SQLException {
        String sql = "UPDATE trains SET train_name = ?, source_station = ?, destination_station = ?, active = ? "
                + "WHERE train_number = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, t.name());
            ps.setString(2, t.source());
            ps.setString(3, t.destination());
            ps.setBoolean(4, t.active());
            ps.setInt(5, t.number());
            return ps.executeUpdate() == 1;
        }
    }

    public boolean setActive(int number, boolean active) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE trains SET active = ? WHERE train_number = ?")) {
            ps.setBoolean(1, active);
            ps.setInt(2, number);
            return ps.executeUpdate() == 1;
        }
    }
}
