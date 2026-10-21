package com.reservation.model;

public record User(long id, String fullName, String username, String role) {
    public boolean isAdmin() { return "ADMIN".equals(role); }
}
