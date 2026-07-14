package com.stayease.dao;

import com.stayease.model.Payment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAOImpl implements PaymentDAO {

    @Override
    public Payment create(Payment p, Connection conn) throws SQLException {
        String sql = "INSERT INTO payments (booking_id, amount, payment_method, received_by) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getBookingId());
            ps.setBigDecimal(2, p.getAmount());
            ps.setString(3, p.getPaymentMethod().name());
            ps.setInt(4, p.getReceivedBy());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    p.setPaymentId(keys.getInt(1));
                }
            }
        }
        return p;
    }

    @Override
    public List<Payment> findByBookingId(int bookingId) throws SQLException {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE booking_id = ? ORDER BY payment_id";
        try (PreparedStatement ps = com.stayease.db.DBConnection.getInstance()
                .getConnection().prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Payment p = new Payment();
                    p.setPaymentId(rs.getInt("payment_id"));
                    p.setBookingId(rs.getInt("booking_id"));
                    p.setAmount(rs.getBigDecimal("amount"));
                    p.setPaymentMethod(Payment.Method.valueOf(rs.getString("payment_method")));
                    p.setReceivedBy(rs.getInt("received_by"));
                    if (rs.getTimestamp("payment_date") != null) {
                        p.setPaymentDate(rs.getTimestamp("payment_date").toLocalDateTime());
                    }
                    list.add(p);
                }
            }
        }
        return list;
    }
}
