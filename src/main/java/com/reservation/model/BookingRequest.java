package com.reservation.model;

import java.time.LocalDate;

public record BookingRequest(String passengerName, String trainNumberText, String classType,
                             LocalDate journeyDate, String source, String destination) { }
