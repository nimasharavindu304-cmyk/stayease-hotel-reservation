package com.stayease.dao;

import com.stayease.model.Booking;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface BookingDAO {
    Booking create(Booking booking, Connection conn) throws SQLException;
    Booking findById(int bookingId) throws SQLException;
    List<Booking> findAll() throws SQLException;
    List<Booking> findByStatus(String status) throws SQLException;
    boolean hasOverlap(int roomId, LocalDate checkIn, LocalDate checkOut) throws SQLException;
    boolean updateStatus(int bookingId, String status) throws SQLException;
    int countActiveBookingsToday() throws SQLException;
    java.math.BigDecimal totalRevenueBetween(LocalDate from, LocalDate to) throws SQLException;

    /** Revenue per day for the last {@code days} days (zero-filled), oldest first. */
    java.util.LinkedHashMap<LocalDate, java.math.BigDecimal> revenueLastDays(int days) throws SQLException;
}
