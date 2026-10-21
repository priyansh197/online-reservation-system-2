package com.reservation.service;

import com.reservation.dao.ReservationDAO;
import com.reservation.dao.TrainDAO;
import com.reservation.model.BookingRequest;
import com.reservation.model.Reservation;
import com.reservation.model.Train;
import com.reservation.model.User;
import com.reservation.util.ValidationUtil;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ReservationService {
    private static final int MAX_PNR_ATTEMPTS = 5;
    private final ReservationDAO dao = new ReservationDAO();
    private final TrainDAO trainDao = new TrainDAO();

    public record Report(int active, int cancelled, Map<String, Integer> perTrain) { }

    /** Validates, saves and returns the booking as read back from the database. */
    public Reservation book(User user, BookingRequest req) throws ServiceException {
        String passenger = ValidationUtil.validatePersonName(req.passengerName(), "Passenger name");
        int trainNo = ValidationUtil.parseTrainNumber(req.trainNumberText());
        String classType = req.classType();
        if (classType == null || !ValidationUtil.CLASS_TYPES.contains(classType))
            throw new ServiceException("Please select a supported class.");
        LocalDate date = req.journeyDate();
        if (date == null) throw new ServiceException("Journey date is required.");
        if (date.isBefore(LocalDate.now())) throw new ServiceException("Journey date cannot be in the past.");
        String src = ValidationUtil.requireNonBlank(req.source(), "Source station");
        String dst = ValidationUtil.requireNonBlank(req.destination(), "Destination station");
        if (src.equalsIgnoreCase(dst)) throw new ServiceException("Source and destination must be different.");

        try {
            Train train = trainDao.findByNumber(trainNo).orElse(null);
            if (train == null) throw new ServiceException("Train number " + trainNo + " does not exist.");
            if (!train.active()) throw new ServiceException("Train " + trainNo + " is not available for booking.");
            if (!isRouteStation(train, src) || !isRouteStation(train, dst))
                throw new ServiceException("Source and destination must be stations on this train's route ("
                        + train.source() + " - " + train.destination() + ").");

            for (int attempt = 0; attempt < MAX_PNR_ATTEMPTS; attempt++) {
                String pnr = generatePnr();
                Reservation r = new Reservation(0, pnr, user.id(), passenger, trainNo, train.name(), classType,
                        date, src, dst, "CONFIRMED", null);
                try {
                    dao.insert(r);
                    // Read back so the confirmation only shows what the database really stored.
                    return dao.findByPnr(pnr).orElseThrow(() -> new SQLException("Saved booking not found"));
                } catch (SQLIntegrityConstraintViolationException e) {
                    String msg = String.valueOf(e.getMessage());
                    if (!msg.contains("uk_pnr") && !msg.contains("'pnr'")) throw e; // not a PNR collision
                    // duplicate PNR: loop and try a new one
                }
            }
            throw new ServiceException("Could not generate a unique PNR. Please try again.");
        } catch (SQLException e) {
            throw ServiceException.database(e);
        }
    }

    public Reservation find(User user, String pnrText) throws ServiceException {
        String pnr = ValidationUtil.validatePnr(pnrText);
        Long owner = user.isAdmin() ? null : user.id();
        try {
            Reservation r = dao.findByPnr(pnr).orElse(null);
            if (r != null && (owner == null || r.userId() == owner)) return r;
            if (dao.isCancelled(pnr, owner))
                throw new ServiceException("Reservation " + pnr + " has already been cancelled.");
            throw new ServiceException("No reservation found for PNR " + pnr + ".");
        } catch (SQLException e) {
            throw ServiceException.database(e);
        }
    }

    public void cancel(User user, String pnrText) throws ServiceException {
        String pnr = ValidationUtil.validatePnr(pnrText);
        try {
            if (!dao.cancel(pnr, user.isAdmin() ? null : user.id()))
                throw new ServiceException("Reservation " + pnr + " could not be cancelled. "
                        + "It may not exist or may already be cancelled.");
        } catch (SQLException e) {
            throw ServiceException.database(e);
        }
    }

    public List<Reservation> listForUser(User user) throws ServiceException {
        try {
            return dao.findByUser(user.id());
        } catch (SQLException e) {
            throw ServiceException.database(e);
        }
    }

    public List<Reservation> listAll(User actor) throws ServiceException {
        AuthenticationService.requireAdmin(actor);
        try {
            return dao.findAll();
        } catch (SQLException e) {
            throw ServiceException.database(e);
        }
    }

    public Report report(User actor) throws ServiceException {
        AuthenticationService.requireAdmin(actor);
        try {
            return new Report(dao.countActive(), dao.countCancelled(), dao.bookingsPerTrain());
        } catch (SQLException e) {
            throw ServiceException.database(e);
        }
    }

    private static boolean isRouteStation(Train t, String station) {
        return station.equalsIgnoreCase(t.source()) || station.equalsIgnoreCase(t.destination());
    }

    private static String generatePnr() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}
