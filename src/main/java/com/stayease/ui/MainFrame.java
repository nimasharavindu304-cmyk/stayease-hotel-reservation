package com.stayease.ui;

import com.stayease.model.User;
import com.stayease.observer.BookingSubject;

import javax.swing.*;
import java.awt.*;

/**
 * Application shell shown after a successful login. Hosts the Dashboard,
 * Room and Guest management (input UIs), the Booking transaction UI, and
 * the Reports screen as tabs sharing one {@link BookingSubject} so the
 * dashboard tab live-updates whenever a booking changes elsewhere.
 */
public class MainFrame extends JFrame {

    private final User currentUser;
    private final BookingSubject bookingSubject = new BookingSubject();

    public MainFrame(User currentUser) {
        super("StayEase Hotel Reservation & Booking Management System");
        this.currentUser = currentUser;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        buildUi();
    }

    private void buildUi() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        JLabel welcome = new JLabel("Signed in as " + currentUser.getFullName()
                + " (" + currentUser.getRole() + ")");
        header.add(welcome, BorderLayout.WEST);

        JButton logoutButton = new JButton("Log out");
        logoutButton.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        header.add(logoutButton, BorderLayout.EAST);

        DashboardPanel dashboardPanel = new DashboardPanel();
        bookingSubject.addObserver(dashboardPanel);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Dashboard", dashboardPanel);
        tabs.addTab("Bookings", new BookingPanel(currentUser, bookingSubject));
        tabs.addTab("Rooms", new RoomManagementPanel());
        tabs.addTab("Guests", new GuestManagementPanel());
        tabs.addTab("Reports", new ReportPanel());

        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        // Populate the dashboard immediately on open.
        dashboardPanel.onBookingChanged();
    }
}
