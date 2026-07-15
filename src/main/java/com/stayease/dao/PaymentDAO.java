package com.stayease.dao;

import com.stayease.model.Payment;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface PaymentDAO {
    Payment create(Payment payment, Connection conn) throws SQLException;
    List<Payment> findByBookingId(int bookingId) throws SQLException;
}
