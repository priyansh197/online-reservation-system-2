package com.reservation.service;

import java.util.logging.Level;
import java.util.logging.Logger;

/** A failure whose message is safe to show to the end user. */
public class ServiceException extends Exception {
    private static final Logger LOG = Logger.getLogger("reservation");

    public ServiceException(String message) { super(message); }

    /** Logs technical details and returns a user-friendly exception (no SQL text, no credentials). */
    public static ServiceException database(Exception cause) {
        LOG.log(Level.SEVERE, "Database error", cause);
        return new ServiceException(
                "A database error occurred. Please check that MySQL is running and the configuration is correct.");
    }
}
