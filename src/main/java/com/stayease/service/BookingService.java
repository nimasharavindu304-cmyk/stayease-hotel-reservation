package com.stayease.service;

import com.stayease.dao.BookingDAO;
import com.stayease.dao.BookingDAOImpl;
import com.stayease.dao.GuestDAO;
import com.stayease.dao.GuestDAOImpl;
import com.stayease.dao.PaymentDAO;
import com.stayease.dao.PaymentDAOImpl;
import com.stayease.dao.RoomDAO;
import com.stayease.dao.RoomDAOImpl;
import com.stayease.db.DBConnection;
import com.stayease.exception.InvalidBookingException;
import com.stayease.exception.RoomNotAvailableException;
import com.stayease.model.Booking;
import com.stayease.model.BookingStatus;
import com.stayease.model.Guest;
import com.stayease.model.Payment;
import com.stayease.model.Room;
import com.stayease.model.RoomStatus;
import com.stayease.observer.BookingSubject;
import com.stayease.util.EmailUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Business/service layer for the Booking transaction — the "major
 * functionality" of the application named in the coursework brief.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Validate the requested stay (dates, guest count vs. capacity).</li>
 *   <li>Check room availability using {@link RoomNotAvailableException}.</li>
 *   <li>Calculate the total amount from the room's nightly rate.</li>
 *   <li>Persist the Booking and its initial Payment as a single JDBC
 *       transaction (commit only if both inserts succeed).</li>
 *   <li>Notify dashboard observers so occupancy/revenue widgets refresh.</li>
 * </ul>
 */
public class BookingService {

    private final BookingDAO bookingDAO = new BookingDAOImpl();
    private final RoomDAO roomDAO = new RoomDAOImpl();
    private final PaymentDAO paymentDAO = new PaymentDAOImpl();
    private final GuestDAO guestDAO = new GuestDAOImpl();
    private final BookingSubject bookingSubject;

    public BookingService(BookingSubject bookingSubject) {
        this.bookingSubject = bookingSubject;
    }

    /**
     * Creates a booking and records its initial payment in one atomic transaction.
     *
     * @throws InvalidBookingException  if the stay dates / guest count are invalid
     * @throws RoomNotAvailableException if the room is under maintenance or already
     *                                    booked for an overlapping date range
     * @throws SQLException              on any database error
     */
    public Booking createBooking(int guestId, int roomId, LocalDate checkIn, LocalDate checkOut,
                                  int numGuests, Payment.Method paymentMethod, int staffUserId)
            throws InvalidBookingException, RoomNotAvailableException, SQLException {

        validateStay(checkIn, checkOut, numGuests, roomId);

        Room room = roomDAO.findById(roomId);
        if (room == null) {
            throw new InvalidBookingException("Selected room no longer exists.");
        }
        if (room.getStatus() == RoomStatus.MAINTENANCE) {
            throw new RoomNotAvailableException(
                    "Room " + room.getRoomNumber() + " is currently under maintenance.");
        }
        if (bookingDAO.hasOverlap(roomId, checkIn, checkOut)) {
            throw new RoomNotAvailableException(
                    "Room " + room.getRoomNumber() + " is already booked for an overlapping period.");
        }

        long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
        BigDecimal total = room.getRatePerNight().multiply(BigDecimal.valueOf(nights));

        Booking booking = new Booking();
        booking.setGuestId(guestId);
        booking.setRoomId(roomId);
        booking.setCheckInDate(checkIn);
        booking.setCheckOutDate(checkOut);
        booking.setNumGuests(numGuests);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setTotalAmount(total);
        booking.setCreatedBy(staffUserId);

        Connection conn = DBConnection.getInstance().getConnection();
        boolean originalAutoCommit = true;
        try {
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            bookingDAO.create(booking, conn);

            Payment payment = new Payment(booking.getBookingId(), total, paymentMethod, staffUserId);
            paymentDAO.create(payment, conn);

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(originalAutoCommit);
        }

        bookingSubject.notifyObservers();
        sendConfirmationEmailQuietly(booking, room);
        return booking;
    }

    /**
     * Optional API integration (per the coursework brief): emails the guest a
     * confirmation. Any failure here (SMTP not configured, network issue, etc.)
     * is swallowed so it never breaks the booking transaction that already
     * committed successfully.
     */
    private void sendConfirmationEmailQuietly(Booking booking, Room room) {
        if (!EmailUtil.isEnabled()) {
            return;
        }
        try {
            Guest guest = guestDAO.findById(booking.getGuestId());
            if (guest == null || guest.getEmail() == null || guest.getEmail().isBlank()) {
                return;
            }
            EmailUtil.sendBookingConfirmation(
                    guest.getEmail(), guest.getFullName(), booking.getBookingId(),
                    room.getRoomNumber(), booking.getCheckInDate().toString(),
                    booking.getCheckOutDate().toString(), booking.getTotalAmount().toString());
        } catch (Exception e) {
            System.err.println("Booking confirmation email could not be sent: " + e.getMessage());
        }
    }

    /** Marks a confirmed booking as checked-in and flips the room to OCCUPIED. */
    public void checkIn(int bookingId, int roomId) throws SQLException {
        bookingDAO.updateStatus(bookingId, BookingStatus.CHECKED_IN.name());
        roomDAO.updateStatus(roomId, RoomStatus.OCCUPIED);
        bookingSubject.notifyObservers();
    }

    /** Marks a booking as checked-out and frees the room. */
    public void checkOut(int bookingId, int roomId) throws SQLException {
        bookingDAO.updateStatus(bookingId, BookingStatus.CHECKED_OUT.name());
        roomDAO.updateStatus(roomId, RoomStatus.AVAILABLE);
        bookingSubject.notifyObservers();
    }

    public void cancelBooking(int bookingId) throws SQLException {
        bookingDAO.updateStatus(bookingId, BookingStatus.CANCELLED.name());
        bookingSubject.notifyObservers();
    }

    private void validateStay(LocalDate checkIn, LocalDate checkOut, int numGuests, int roomId)
            throws InvalidBookingException, SQLException {
        if (checkIn == null || checkOut == null) {
            throw new InvalidBookingException("Check-in and check-out dates are required.");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new InvalidBookingException("Check-out date must be after the check-in date.");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new InvalidBookingException("Check-in date cannot be in the past.");
        }
        if (numGuests <= 0) {
            throw new InvalidBookingException("Number of guests must be at least 1.");
        }
        Room room = roomDAO.findById(roomId);
        if (room != null && numGuests > room.getCapacity()) {
            throw new InvalidBookingException(
                    "Room capacity is " + room.getCapacity() + "; reduce the number of guests.");
        }
    }

    public BookingDAO getBookingDAO() {
        return bookingDAO;
    }
}
