package com.reservation.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Reservation(long id, String pnr, long userId, String passengerName, int trainNumber,
                          String trainName, String classType, LocalDate journeyDate, String source,
                          String destination, String status, LocalDateTime createdAt) { }
