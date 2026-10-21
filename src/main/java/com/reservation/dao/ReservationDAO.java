package com.reservation.dao;

import com.reservation.config.DatabaseConnection;
import com.reservation.model.Reservation;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ReservationDAO {
    private static final String SELECT =
            "SELECT r.reservation_id, r.pnr, r.user_id, r.passenger_name, r.train_number, t.train_name, "
          + "r.class_type, r.journey_date, r.source_station, r.destination_station, r.booking_status, r.created_at "
          + "FROM reservations r JOIN trains t ON t.train_number = r.train_number ";

    private Reservation map(ResultSet rs) throws SQLException {
        return new Reservation(rs.getLong("reservation_id"), rs.getString("pnr"), rs.getLong("user_id"),
                rs.getString("passenger_name"), rs.getInt("train_number"), rs.getString("train_name"),
                rs.getString("class_type"), rs.getDate("journey_date").toLocalDate(),
                rs.getString("source_station"), rs.getString("destination_station"),
                rs.getString("booking_status"), rs.getTimestamp("created_at").toLocalDateTime());
    }

    /** Inserts a booking. Throws SQLIntegrityConstraintViolationException on a duplicate PNR. */
    public long insert(Reservation r) throws SQLException {
        String sql = "INSERT INTO reservations (pnr, user_id, passenger_name, train_number, class_type, "
                + "journey_date, source_station, destination_station, booking_status) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.pnr());
            ps.setLong(2, r.userId());
            ps.setString(3, r.passengerName());
            ps.setInt(4, r.trainNumber());
            ps.setString(5, r.classType());
            ps.setDate(6, Date.valueOf(r.journeyDate()));
            ps.setString(7, r.source());
            ps.setString(8, r.destination());
            ps.setString(9, r.status());
            if (ps.executeUpdate() != 1) throw new SQLException("Insert affected no rows");
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("No generated key returned");
                return keys.getLong(1);
            }
        }
    }

    public Optional<Reservation> findByPnr(String pnr) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT + "WHERE r.pnr = ?")) {
            ps.setString(1, pnr);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public List<Reservation> findByUser(long userId) throws SQLException {
        return list(SELECT + "WHERE r.user_id = ? ORDER BY r.created_at DESC", userId);
    }

    public List<Reservation> findAll() throws SQLException {
        return list(SELECT + "ORDER BY r.created_at DESC", null);
    }

    private List<Reservation> list(String sql, Long userId) throws SQLException {
        List<Reservation> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (userId != null) ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        }
        return out;
    }

    /** True if this PNR exists in the cancellation archive (optionally restricted to one owner). */
    public boolean isCancelled(String pnr, Long ownerId) throws SQLException {
        String sql = "SELECT 1 FROM cancelled_reservations WHERE pnr = ?" + (ownerId != null ? " AND user_id = ?" : "");
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, pnr);
            if (ownerId != null) ps.setLong(2, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Cancels a booking: archives it, then DELETEs it, in ONE transaction.
     * ownerId == null means "any owner" (administrator). Returns false if nothing was cancelled.
     */
    public boolean cancel(String pnr, Long ownerId) throws SQLException {
        String owner = ownerId != null ? " AND user_id = ?" : "";
        String archive = "INSERT INTO cancelled_reservations (pnr, user_id, passenger_name, train_number, "
                + "class_type, journey_date, source_station, destination_station, booked_at) "
                + "SELECT pnr, user_id, passenger_name, train_number, class_type, journey_date, "
                + "source_station, destination_station, created_at FROM reservations WHERE pnr = ?" + owner;
        String delete = "DELETE FROM reservations WHERE pnr = ?" + owner;
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int archived;
                try (PreparedStatement ps = c.prepareStatement(archive)) {
                    ps.setString(1, pnr);
                    if (ownerId != null) ps.setLong(2, ownerId);
                    archived = ps.executeUpdate();
                }
                int deleted = 0;
                if (archived == 1) {
                    try (PreparedStatement ps = c.prepareStatement(delete)) {
                        ps.setString(1, pnr);
                        if (ownerId != null) ps.setLong(2, ownerId);
                        deleted = ps.executeUpdate();
                    }
                }
                if (archived == 1 && deleted == 1) {
                    c.commit();
                    return true;
                }
                c.rollback();
                return false;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public int countActive() throws SQLException {
        return count("SELECT COUNT(*) FROM reservations");
    }

    public int countCancelled() throws SQLException {
        return count("SELECT COUNT(*) FROM cancelled_reservations");
    }

    private int count(String sql) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public Map<String, Integer> bookingsPerTrain() throws SQLException {
        String sql = "SELECT t.train_number, t.train_name, COUNT(r.reservation_id) AS cnt FROM trains t "
                + "LEFT JOIN reservations r ON r.train_number = t.train_number "
                + "GROUP BY t.train_number, t.train_name ORDER BY t.train_number";
        Map<String, Integer> out = new LinkedHashMap<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.put(rs.getInt(1) + " - " + rs.getString(2), rs.getInt(3));
        }
        return out;
    }
}
