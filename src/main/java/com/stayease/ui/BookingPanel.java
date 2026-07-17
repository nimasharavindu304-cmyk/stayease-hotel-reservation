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
import com.stayease.model.BookingStatus;
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
 * reservation for a guest with live availability checking, then manage its
 * lifecycle (check-in / check-out / cancel). Shows live status-count chips
 * above the bookings table.
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

    private final JLabel confirmedChip = chip(UITheme.PILL_BLUE_BG, UITheme.PILL_BLUE_FG);
    private final JLabel checkedInChip = chip(UITheme.PILL_GREEN_BG, UITheme.PILL_GREEN_FG);
    private final JLabel checkedOutChip = chip(UITheme.PILL_GRAY_BG, UITheme.PILL_GRAY_FG);
    private final JLabel cancelledChip = chip(UITheme.PILL_RED_BG, UITheme.PILL_RED_FG);

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

        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.BG);
        setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        JPanel north = new JPanel(new BorderLayout(0, 12));
        north.setOpaque(false);
        north.add(buildHeader(), BorderLayout.NORTH);
        north.add(buildForm(), BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);

        UITheme.RoundedCard tableCard = new UITheme.RoundedCard(20);
        tableCard.setLayout(new BorderLayout(0, 8));
        tableCard.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        chips.setOpaque(false);
        chips.add(confirmedChip);
        chips.add(checkedInChip);
        chips.add(checkedOutChip);
        chips.add(cancelledChip);
        tableCard.add(chips, BorderLayout.NORTH);

        UITheme.styleTable(bookingsTable);
        bookingsTable.getColumnModel().getColumn(5).setCellRenderer(new UITheme.StatusPillRenderer());
        JScrollPane scroll = new JScrollPane(bookingsTable);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UITheme.CARD);
        tableCard.add(scroll, BorderLayout.CENTER);
        add(tableCard, BorderLayout.CENTER);

        add(buildActionBar(), BorderLayout.SOUTH);

        checkInSpinner.setEditor(new JSpinner.DateEditor(checkInSpinner, "dd-MM-yyyy"));
        checkOutSpinner.setEditor(new JSpinner.DateEditor(checkOutSpinner, "dd-MM-yyyy"));
        checkInSpinner.setValue(toDate(LocalDate.now()));
        checkOutSpinner.setValue(toDate(LocalDate.now().plusDays(1)));

        loadGuests();
        refreshBookingsTable();
    }

    private static JLabel chip(Color bg, Color fg) {
        JLabel l = new JLabel(" ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setOpaque(false);
        l.setBackground(bg);
        l.setForeground(fg);
        l.setFont(UITheme.FONT_PILL);
        l.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        return l;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Bookings");
        title.setFont(UITheme.FONT_H1);
        title.setForeground(UITheme.TEXT);
        title.setAlignmentX(LEFT_ALIGNMENT);
        JLabel sub = new JLabel("Create reservations and manage their lifecycle");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.MUTED);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(2));
        header.add(sub);
        return header;
    }

    private JPanel buildForm() {
        UITheme.RoundedCard form = new UITheme.RoundedCard(20);
        form.setLayout(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0;
        c.gridy = 0;
        form.add(fieldLabel("Guest"), c);
        c.gridx = 1;
        form.add(guestCombo, c);
        c.gridx = 2;
        form.add(fieldLabel("Check-in"), c);
        c.gridx = 3;
        form.add(checkInSpinner, c);
        c.gridx = 4;
        form.add(fieldLabel("Check-out"), c);
        c.gridx = 5;
        form.add(checkOutSpinner, c);

        c.gridx = 0;
        c.gridy = 1;
        form.add(fieldLabel("Guests"), c);
        c.gridx = 1;
        form.add(numGuestsSpinner, c);
        c.gridx = 2;
        form.add(fieldLabel("Room"), c);
        c.gridx = 3;
        form.add(roomCombo, c);
        c.gridx = 4;
        form.add(fieldLabel("Payment"), c);
        c.gridx = 5;
        form.add(paymentMethodCombo, c);

        JButton searchButton = new JButton("Search Availability");
        UITheme.secondary(searchButton);
        searchButton.addActionListener(e -> searchAvailableRooms());
        JButton createButton = new JButton("Confirm Booking");
        UITheme.primary(createButton);
        createButton.addActionListener(e -> createBooking());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        buttons.setOpaque(false);
        buttons.add(searchButton);
        buttons.add(createButton);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 6;
        form.add(buttons, c);

        return form;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_SMALL);
        l.setForeground(UITheme.MUTED);
        return l;
    }

    private JPanel buildActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setOpaque(false);
        JButton checkInButton = new JButton("Check-in Selected");
        UITheme.primary(checkInButton);
        checkInButton.addActionListener(e -> withSelectedBooking(this::doCheckIn));
        JButton checkOutButton = new JButton("Check-out Selected");
        UITheme.secondary(checkOutButton);
        checkOutButton.addActionListener(e -> withSelectedBooking(this::doCheckOut));
        JButton cancelButton = new JButton("Cancel Selected");
        UITheme.danger(cancelButton);
        cancelButton.addActionListener(e -> withSelectedBooking(this::doCancel));
        JButton refreshButton = new JButton("Refresh List");
        UITheme.secondary(refreshButton);
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
            JOptionPane.showMessageDialog(this, "Register a guest first (Guests screen).",
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
        int modelRow = bookingsTable.convertRowIndexToModel(row);
        int bookingId = (Integer) tableModel.getValueAt(modelRow, 0);
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
        int confirmed = 0;
        int checkedIn = 0;
        int checkedOut = 0;
        int cancelled = 0;
        try {
            List<Booking> bookings = bookingDAO.findAll();
            for (Booking b : bookings) {
                tableModel.addRow(new Object[]{
                        b.getBookingId(), b.getGuestName(), b.getRoomNumber(),
                        b.getCheckInDate(), b.getCheckOutDate(), b.getStatus(), b.getTotalAmount()
                });
                if (b.getStatus() == BookingStatus.CONFIRMED) {
                    confirmed++;
                } else if (b.getStatus() == BookingStatus.CHECKED_IN) {
                    checkedIn++;
                } else if (b.getStatus() == BookingStatus.CHECKED_OUT) {
                    checkedOut++;
                } else if (b.getStatus() == BookingStatus.CANCELLED) {
                    cancelled++;
                }
            }
            confirmedChip.setText(confirmed + " CONFIRMED");
            checkedInChip.setText(checkedIn + " CHECKED-IN");
            checkedOutChip.setText(checkedOut + " CHECKED-OUT");
            cancelledChip.setText(cancelled + " CANCELLED");

            // Guests may have been added on the Guests screen since we opened.
            loadGuests();
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
