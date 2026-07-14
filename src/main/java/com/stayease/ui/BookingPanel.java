package com.stayease.ui;

import com.stayease.dao.BookingDAO;
import com.stayease.dao.BookingDAOImpl;
import com.stayease.dao.GuestDAO;
import com.stayease.dao.GuestDAOImpl;
import com.stayease.dao.RoomDAO;
import com.stayease.dao.RoomDAOImpl;
import com.stayease.exception.InvalidBookingException;
import com.stayease.exception.RoomNotAvailableException;
import com.stayease.model.Booking;
import com.stayease.model.Guest;
import com.stayease.model.Payment;
import com.stayease.model.Room;
import com.stayease.model.User;
import com.stayease.observer.BookingSubject;
import com.stayease.service.BookingService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * Transaction UI (the coursework's "major functionality"): create a room
 * reservation for a guest, checking live availability before it is
 * confirmed, then manage its lifecycle (check-in / check-out / cancel).
 */
public class BookingPanel extends JPanel {

    private final User currentUser;
    private final BookingService bookingService;
    private final GuestDAO guestDAO = new GuestDAOImpl();
    private final RoomDAO roomDAO = new RoomDAOImpl();
    private final BookingDAO bookingDAO = new BookingDAOImpl();

    private final JComboBox<Guest> guestCombo = new JComboBox<>();
    private final JSpinner checkInSpinner = new JSpinner(new SpinnerDateModel());
    private final JSpinner checkOutSpinner = new JSpinner(new SpinnerDateModel());
    private final JSpinner numGuestsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
    private final JComboBox<Room> roomCombo = new JComboBox<>();
    private final JComboBox<Payment.Method> paymentMethodCombo = new JComboBox<>(Payment.Method.values());

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Guest", "Room", "Check-in", "Check-out", "Status", "Total (Rs.)"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable bookingsTable = new JTable(tableModel);

    public BookingPanel(User currentUser, BookingSubject bookingSubject) {
        this.currentUser = currentUser;
        this.bookingService = new BookingService(bookingSubject);

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(buildForm(), BorderLayout.NORTH);
        add(new JScrollPane(bookingsTable), BorderLayout.CENTER);
        add(buildActionBar(), BorderLayout.SOUTH);

        checkInSpinner.setEditor(new JSpinner.DateEditor(checkInSpinner, "dd-MM-yyyy"));
        checkOutSpinner.setEditor(new JSpinner.DateEditor(checkOutSpinner, "dd-MM-yyyy"));
        checkInSpinner.setValue(toDate(LocalDate.now()));
        checkOutSpinner.setValue(toDate(LocalDate.now().plusDays(1)));

        loadGuests();
        refreshBookingsTable();
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("New Booking"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;

        int row = 0;
        c.gridx = 0;
        c.gridy = row;
        form.add(new JLabel("Guest:"), c);
        c.gridx = 1;
        form.add(guestCombo, c);
        JButton newGuestHint = new JButton("Manage Guests →");
        newGuestHint.setToolTipText("Add new guests from the Guests tab");
        newGuestHint.setEnabled(false);
        c.gridx = 2;
        form.add(newGuestHint, c);

        row++;
        c.gridx = 0;
        c.gridy = row;
        form.add(new JLabel("Check-in date:"), c);
        c.gridx = 1;
        form.add(checkInSpinner, c);

        row++;
        c.gridx = 0;
        c.gridy = row;
        form.add(new JLabel("Check-out date:"), c);
        c.gridx = 1;
        form.add(checkOutSpinner, c);

        row++;
        c.gridx = 0;
        c.gridy = row;
        form.add(new JLabel("Number of guests:"), c);
        c.gridx = 1;
        form.add(numGuestsSpinner, c);

        row++;
        c.gridx = 0;
        c.gridy = row;
        form.add(new JLabel("Available room:"), c);
        c.gridx = 1;
        form.add(roomCombo, c);
        JButton searchButton = new JButton("Search Availability");
        searchButton.addActionListener(e -> searchAvailableRooms());
        c.gridx = 2;
        form.add(searchButton, c);

        row++;
        c.gridx = 0;
        c.gridy = row;
        form.add(new JLabel("Payment method:"), c);
        c.gridx = 1;
        form.add(paymentMethodCombo, c);

        row++;
        JButton createButton = new JButton("Confirm Booking");
        createButton.addActionListener(e -> createBooking());
        c.gridx = 1;
        c.gridy = row;
        form.add(createButton, c);

        return form;
    }

    private JPanel buildActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton checkInButton = new JButton("Check-in Selected");
        checkInButton.addActionListener(e -> withSelectedBooking(this::doCheckIn));
        JButton checkOutButton = new JButton("Check-out Selected");
        checkOutButton.addActionListener(e -> withSelectedBooking(this::doCheckOut));
        JButton cancelButton = new JButton("Cancel Selected");
        cancelButton.addActionListener(e -> withSelectedBooking(this::doCancel));
        JButton refreshButton = new JButton("Refresh List");
        refreshButton.addActionListener(e -> refreshBookingsTable());

        bar.add(checkInButton);
        bar.add(checkOutButton);
        bar.add(cancelButton);
        bar.add(refreshButton);
        return bar;
    }

    private void loadGuests() {
        guestCombo.removeAllItems();
        try {
            List<Guest> guests = guestDAO.findAll();
            for (Guest g : guests) {
                guestCombo.addItem(g);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load guests: " + e.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchAvailableRooms() {
        LocalDate checkIn = toLocalDate((Date) checkInSpinner.getValue());
        LocalDate checkOut = toLocalDate((Date) checkOutSpinner.getValue());
        if (!checkOut.isAfter(checkIn)) {
            JOptionPane.showMessageDialog(this, "Check-out date must be after check-in date.",
                    "Invalid dates", JOptionPane.WARNING_MESSAGE);
            return;
        }
        roomCombo.removeAllItems();
        try {
            List<Room> available = roomDAO.findAvailableForDates(checkIn, checkOut);
            if (available.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No rooms are available for the selected dates.",
                        "No availability", JOptionPane.INFORMATION_MESSAGE);
            }
            for (Room r : available) {
                roomCombo.addItem(r);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to search rooms: " + e.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createBooking() {
        Guest guest = (Guest) guestCombo.getSelectedItem();
        Room room = (Room) roomCombo.getSelectedItem();
        if (guest == null) {
            JOptionPane.showMessageDialog(this, "Register a guest first (Guests tab).",
                    "Missing guest", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (room == null) {
            JOptionPane.showMessageDialog(this, "Click \"Search Availability\" and pick a room first.",
                    "Missing room", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate checkIn = toLocalDate((Date) checkInSpinner.getValue());
        LocalDate checkOut = toLocalDate((Date) checkOutSpinner.getValue());
        int numGuests = (Integer) numGuestsSpinner.getValue();
        Payment.Method method = (Payment.Method) paymentMethodCombo.getSelectedItem();

        try {
            Booking booking = bookingService.createBooking(
                    guest.getGuestId(), room.getRoomId(), checkIn, checkOut, numGuests, method, currentUser.getUserId());
            JOptionPane.showMessageDialog(this,
                    "Booking #" + booking.getBookingId() + " confirmed.\nTotal: Rs. " + booking.getTotalAmount(),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshBookingsTable();
        } catch (InvalidBookingException | RoomNotAvailableException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Booking rejected", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private interface BookingAction {
        void run(int bookingId, int roomId) throws SQLException;
    }

    private void withSelectedBooking(BookingAction action) {
        int row = bookingsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a booking in the table first.",
                    "Nothing selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int bookingId = (Integer) tableModel.getValueAt(row, 0);
        try {
            Booking booking = bookingDAO.findById(bookingId);
            if (booking == null) {
                return;
            }
            action.run(bookingId, booking.getRoomId());
            refreshBookingsTable();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Operation failed: " + e.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doCheckIn(int bookingId, int roomId) throws SQLException {
        bookingService.checkIn(bookingId, roomId);
    }

    private void doCheckOut(int bookingId, int roomId) throws SQLException {
        bookingService.checkOut(bookingId, roomId);
    }

    private void doCancel(int bookingId, int roomId) throws SQLException {
        bookingService.cancelBooking(bookingId);
    }

    private void refreshBookingsTable() {
        tableModel.setRowCount(0);
        try {
            List<Booking> bookings = bookingDAO.findAll();
            for (Booking b : bookings) {
                tableModel.addRow(new Object[]{
                        b.getBookingId(), b.getGuestName(), b.getRoomNumber(),
                        b.getCheckInDate(), b.getCheckOutDate(), b.getStatus(), b.getTotalAmount()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load bookings: " + e.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static Date toDate(LocalDate localDate) {
        return java.sql.Date.valueOf(localDate);
    }

    private static LocalDate toLocalDate(Date date) {
        return new java.sql.Date(date.getTime()).toLocalDate();
    }
}
