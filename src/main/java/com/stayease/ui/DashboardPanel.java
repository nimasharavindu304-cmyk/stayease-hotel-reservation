package com.stayease.ui;

import com.stayease.dao.BookingDAO;
import com.stayease.dao.BookingDAOImpl;
import com.stayease.dao.RoomDAO;
import com.stayease.dao.RoomDAOImpl;
import com.stayease.model.Room;
import com.stayease.model.RoomStatus;
import com.stayease.observer.DashboardObserver;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * The application's "Dashboard" input UI: at-a-glance occupancy and revenue
 * statistics. Implements {@link DashboardObserver} so it refreshes itself
 * automatically whenever {@link com.stayease.service.BookingService} notifies
 * its {@link com.stayease.observer.BookingSubject} (Observer pattern).
 */
public class DashboardPanel extends JPanel implements DashboardObserver {

    private final RoomDAO roomDAO = new RoomDAOImpl();
    private final BookingDAO bookingDAO = new BookingDAOImpl();

    private final JLabel totalRoomsValue = new JLabel("-");
    private final JLabel availableValue = new JLabel("-");
    private final JLabel occupiedValue = new JLabel("-");
    private final JLabel maintenanceValue = new JLabel("-");
    private final JLabel activeStaysValue = new JLabel("-");
    private final JLabel revenueTodayValue = new JLabel("-");
    private final JLabel statusLabel = new JLabel(" ");

    public DashboardPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel grid = new JPanel(new GridLayout(2, 3, 16, 16));
        grid.add(statCard("Total Rooms", totalRoomsValue));
        grid.add(statCard("Available Rooms", availableValue));
        grid.add(statCard("Occupied Rooms", occupiedValue));
        grid.add(statCard("Under Maintenance", maintenanceValue));
        grid.add(statCard("Active Stays Today", activeStaysValue));
        grid.add(statCard("Revenue Today (Rs.)", revenueTodayValue));

        add(grid, BorderLayout.NORTH);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> onBookingChanged());
        JPanel south = new JPanel(new BorderLayout());
        south.add(refreshButton, BorderLayout.WEST);
        south.add(statusLabel, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
    }

    private JPanel statCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN, 12f));
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 28f));
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    /** Re-queries the database and updates every stat card. Called on open and after any booking change. */
    @Override
    public void onBookingChanged() {
        try {
            List<Room> rooms = roomDAO.findAll();
            long available = rooms.stream().filter(r -> r.getStatus() == RoomStatus.AVAILABLE).count();
            long occupied = rooms.stream().filter(r -> r.getStatus() == RoomStatus.OCCUPIED).count();
            long maintenance = rooms.stream().filter(r -> r.getStatus() == RoomStatus.MAINTENANCE).count();

            totalRoomsValue.setText(String.valueOf(rooms.size()));
            availableValue.setText(String.valueOf(available));
            occupiedValue.setText(String.valueOf(occupied));
            maintenanceValue.setText(String.valueOf(maintenance));

            int activeToday = bookingDAO.countActiveBookingsToday();
            activeStaysValue.setText(String.valueOf(activeToday));

            LocalDate today = LocalDate.now();
            BigDecimal revenueToday = bookingDAO.totalRevenueBetween(today, today.plusDays(1));
            revenueTodayValue.setText(revenueToday == null ? "0.00" : revenueToday.toPlainString());

            statusLabel.setText("Last updated: " + java.time.LocalTime.now().withNano(0));
        } catch (SQLException e) {
            statusLabel.setText("Failed to refresh dashboard: " + e.getMessage());
        }
    }
}
