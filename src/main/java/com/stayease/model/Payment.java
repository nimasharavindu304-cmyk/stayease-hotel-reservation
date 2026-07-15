package com.stayease.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** A payment recorded against a booking. */
public class Payment {

    public enum Method {
        CASH, CARD, ONLINE
    }

    private int paymentId;
    private int bookingId;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private Method paymentMethod;
    private int receivedBy;

    public Payment() {
    }

    public Payment(int bookingId, BigDecimal amount, Method paymentMethod, int receivedBy) {
        this.bookingId = bookingId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.receivedBy = receivedBy;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Method getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(Method paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public int getReceivedBy() {
        return receivedBy;
    }

    public void setReceivedBy(int receivedBy) {
        this.receivedBy = receivedBy;
    }
}
