package com.stayease.dao;

import com.stayease.db.DBConnection;
import com.stayease.model.Booking;
import com.stayease.model.BookingStatus;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingDAOImpl implements BookingDAO {

    private Connection getConnection() {
        return DBConnection.getInstance().getConnection();
    }

    /**
     * Accepts an explicit Connection so the caller (BookingService) can run the
     * booking insert and the payment insert as one atomic transaction.
     */
    @Override
    public Booking create(Booking b, Connection conn) throws SQLException {
        String sql = "INSERT INTO bookings (guest_id, room_id, check_in_date, check_out_date, "
                + "num_guests, status, total_amount, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, b.getGuestId());
            ps.setInt(2, b.getRoomId());
            ps.setDate(3, Date.valueOf(b.getCheckInDate()));
            ps.setDate(4, Date.valueOf(b.getCheckOutDate()));
            ps.setInt(5, b.getNumGuests());
            ps.setString(6, b.getStatus().name());
            ps.setBigDecimal(7, b.getTotalAmount());
            ps.setInt(8, b.getCreatedBy());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    b.setBookingId(keys.getInt(1));
                }
            }
        }
        return b;
    }

    @Override
    public Booking findById(int bookingId) throws SQLException {
        String sql = joinSql() + " WHERE b.booking_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Booking> findAll() throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = joinSql() + " ORDER BY b.booking_id DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    @Override
    public List<Booking> findByStatus(String status) throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = joinSql() + " WHERE b.status = ? ORDER BY b.booking_id DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    @Override
    public boolean hasOverlap(int roomId, LocalDate checkIn, LocalDate checkOut) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE room_id = ? "
                + "AND status IN ('CONFIRMED','CHECKED_IN') "
                + "AND check_in_date < ? AND check_out_date > ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ps.setDate(2, Date.valueOf(checkOut));
            ps.setDate(3, Date.valueOf(checkIn));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    @Override
    public boolean updateStatus(int bookingId, String status) throws SQLException {
        String sql = "UPDATE bookings SET status = ? WHERE booking_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public int countActiveBookingsToday() throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE status = 'CHECKED_IN' "
                + "AND CURDATE() BETWEEN check_in_date AND check_out_date";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    @Override
    public BigDecimal totalRevenueBetween(LocalDate from, LocalDate to) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE payment_date BETWEEN ? AND ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }

    /** Joins bookings with guests and rooms so callers get human-readable names for free. */
    private String joinSql() {
        return "SELECT b.*, CONCAT(g.first_name, ' ', g.last_name) AS guest_name, r.room_number "
                + "FROM bookings b "
                + "JOIN guests g ON b.guest_id = g.guest_id "
                + "JOIN rooms r ON b.room_id = r.room_id";
    }

    private Booking map(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setBookingId(rs.getInt("booking_id"));
        b.setGuestId(rs.getInt("guest_id"));
        b.setRoomId(rs.getInt("room_id"));
        b.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
        b.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
        b.setNumGuests(rs.getInt("num_guests"));
        b.setStatus(BookingStatus.valueOf(rs.getString("status")));
        b.setTotalAmount(rs.getBigDecimal("total_amount"));
        b.setCreatedBy(rs.getInt("created_by"));
        if (rs.getTimestamp("created_at") != null) {
            b.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        b.setGuestName(rs.getString("guest_name"));
        b.setRoomNumber(rs.getString("room_number"));
        return b;
    }
}
