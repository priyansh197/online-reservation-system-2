package com.reservation.service;

import com.reservation.dao.TrainDAO;
import com.reservation.model.Train;
import com.reservation.model.User;
import com.reservation.util.ValidationUtil;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

public class TrainService {
    private final TrainDAO dao = new TrainDAO();

    public List<Train> listActive() throws ServiceException {
        try {
            return dao.listActive();
        } catch (SQLException e) {
            throw ServiceException.database(e);
        }
    }

    /** Looks up a bookable train from user-typed text. */
    public Train findActive(String numberText) throws ServiceException {
        int n = ValidationUtil.parseTrainNumber(numberText);
        try {
            Train t = dao.findByNumber(n).orElse(null);
            if (t == null) throw new ServiceException("Train number " + n + " does not exist.");
            if (!t.active()) throw new ServiceException("Train " + n + " is not available for booking.");
            return t;
        } catch (SQLException e) {
            throw ServiceException.database(e);
        }
    }

    // ---------- administrator operations ----------

    public List<Train> listAll(User actor) throws ServiceException {
        AuthenticationService.requireAdmin(actor);
        try {
            return dao.listAll();
        } catch (SQLException e) {
            throw ServiceException.database(e);
        }
    }

    public List<Train> search(User actor, String text) throws ServiceException {
        AuthenticationService.requireAdmin(actor);
        String q = ValidationUtil.clean(text);
        if (q.isEmpty()) return listAll(actor);
        try {
            return dao.search(q);
        } catch (SQLException e) {
            throw ServiceException.database(e);
        }
    }

    public void add(User actor, int number, String name, String source, String dest, boolean active)
            throws ServiceException {
        AuthenticationService.requireAdmin(actor);
        Train t = validate(number, name, source, dest, active);
        try {
            dao.insert(t);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new ServiceException("A train with number " + number + " already exists.");
        } catch (SQLException e) {
            throw ServiceException.database(e);
        }
    }

    public void update(User actor, int number, String name, String source, String dest, boolean active)
            throws ServiceException {
        AuthenticationService.requireAdmin(actor);
        Train t = validate(number, name, source, dest, active);
        try {
            if (!dao.update(t)) throw new ServiceException("Train " + number + " was not found.");
        } catch (SQLException e) {
            throw ServiceException.database(e);
        }
    }

    /** Trains are deactivated, never deleted, so historical reservations stay valid. */
    public void setActive(User actor, int number, boolean active) throws ServiceException {
        AuthenticationService.requireAdmin(actor);
        try {
            if (!dao.setActive(number, active)) throw new ServiceException("Train " + number + " was not found.");
        } catch (SQLException e) {
            throw ServiceException.database(e);
        }
    }

    private Train validate(int number, String name, String source, String dest, boolean active)
            throws ServiceException {
        if (number <= 0) throw new ServiceException("Train number must be greater than zero.");
        String n = ValidationUtil.requireNonBlank(name, "Train name");
        String s = ValidationUtil.requireNonBlank(source, "Source station");
        String d = ValidationUtil.requireNonBlank(dest, "Destination station");
        if (n.length() > 120 || s.length() > 100 || d.length() > 100)
            throw new ServiceException("Name or station text is too long.");
        if (s.equalsIgnoreCase(d)) throw new ServiceException("Source and destination must be different.");
        return new Train(number, n, s, d, active);
    }
}
