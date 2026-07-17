package com.stayease.ui;

import com.stayease.model.User;
import com.stayease.observer.BookingSubject;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Application shell shown after a successful login.
 * <p>
 * A fixed, floating rounded sidebar on the left (a navy card with soft
 * shadow, like modern dashboard designs) switches between the five content
 * screens using a {@link CardLayout}. All screens share one
 * {@link BookingSubject} so the Dashboard live-updates whenever a booking
 * changes elsewhere (Observer pattern).
 */
public class MainFrame extends JFrame {

    private static final int SIDEBAR_WIDTH = 218;

    private final User currentUser;
    private final BookingSubject bookingSubject = new BookingSubject();

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentArea = new JPanel(cardLayout);
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();

    public MainFrame(User currentUser) {
        super("StayEase Hotel Reservation & Booking Management System");
        this.currentUser = currentUser;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 730);
        setMinimumSize(new Dimension(980, 630));
        setLocationRelativeTo(null);
        buildUi();
    }

    private void buildUi() {
        getContentPane().setBackground(UITheme.BG);
        setLayout(new BorderLayout());

        DashboardPanel dashboardPanel = new DashboardPanel();
        bookingSubject.addObserver(dashboardPanel);

        contentArea.setBackground(UITheme.BG);
        contentArea.add(wrap(dashboardPanel), "Dashboard");
        contentArea.add(wrap(new BookingPanel(currentUser, bookingSubject)), "Bookings");
        contentArea.add(wrap(new RoomManagementPanel()), "Rooms");
        contentArea.add(wrap(new GuestManagementPanel()), "Guests");
        contentArea.add(wrap(new ReportPanel()), "Reports");

        add(buildSidebarWrapper(), BorderLayout.WEST);
        add(buildMainArea(), BorderLayout.CENTER);

        selectScreen("Dashboard");
        dashboardPanel.onBookingChanged();
    }

    private JComponent wrap(JComponent inner) {
        inner.setOpaque(false);
        return inner;
    }

    /* ------------------------------- Sidebar -------------------------------- */

    /** Transparent margin panel so the rounded navy card appears to float. */
    private JPanel buildSidebarWrapper() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBackground(UITheme.BG);
        wrapper.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 4));
        wrapper.add(buildSidebar(), BorderLayout.CENTER);
        wrapper.setPreferredSize(new Dimension(SIDEBAR_WIDTH + 18, 0));
        return wrapper;
    }

    /** The floating rounded navy card itself. */
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                int arc = 26;
                for (int i = 3; i >= 1; i--) {
                    g2.setColor(new Color(0, 0, 0, 8));
                    g2.fillRoundRect(3 - i + 2, 3 - i + 4, w - 6 + 2 * i - 4, h - 6 + 2 * i - 4,
                            arc + i, arc + i);
                }
                g2.setColor(UITheme.NAVY);
                g2.fillRoundRect(3, 3, w - 6, h - 6, arc, arc);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        sidebar.setOpaque(false);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(26, 14, 20, 14));

        JLabel brand = new JLabel("StayEase");
        brand.setFont(UITheme.font(Font.BOLD, 22));
        brand.setForeground(Color.WHITE);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        JLabel tag = new JLabel("Front Desk");
        tag.setFont(UITheme.FONT_SMALL);
        tag.setForeground(UITheme.GOLD);
        tag.setAlignmentX(Component.LEFT_ALIGNMENT);
        tag.setBorder(BorderFactory.createEmptyBorder(2, 10, 0, 0));

        sidebar.add(brand);
        sidebar.add(tag);
        sidebar.add(Box.createVerticalStrut(24));

        for (String item : new String[]{"Dashboard", "Bookings", "Rooms", "Guests", "Reports"}) {
            JButton navButton = createNavButton(item);
            navButtons.put(item, navButton);
            sidebar.add(navButton);
            sidebar.add(Box.createVerticalStrut(6));
        }

        sidebar.add(Box.createVerticalGlue());

        JLabel userName = new JLabel(currentUser.getFullName());
        userName.setFont(UITheme.FONT_BODY_BOLD);
        userName.setForeground(Color.WHITE);
        userName.setAlignmentX(Component.LEFT_ALIGNMENT);
        userName.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        JLabel userRole = new JLabel(currentUser.getRole().toString());
        userRole.setFont(UITheme.FONT_SMALL);
        userRole.setForeground(UITheme.SIDEBAR_TEXT);
        userRole.setAlignmentX(Component.LEFT_ALIGNMENT);
        userRole.setBorder(BorderFactory.createEmptyBorder(2, 10, 0, 0));

        sidebar.add(userName);
        sidebar.add(userRole);

        return sidebar;
    }

    private JButton createNavButton(String label) {
        // The rounded pill is painted by hand so it renders identically on
        // every look-and-feel (FlatLaf's roundRect hint alone is unreliable
        // when a custom border replaces the default button border).
        JButton b = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g) {
                Color bg = getBackground();
                if (bg != null && !bg.equals(UITheme.NAVY)) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(bg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFont(UITheme.FONT_BODY);
        b.setForeground(UITheme.SIDEBAR_TEXT);
        b.setBackground(UITheme.NAVY);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorder(BorderFactory.createEmptyBorder(11, 16, 11, 12));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.addActionListener(e -> selectScreen(label));

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!Boolean.TRUE.equals(b.getClientProperty("active"))) {
                    b.setBackground(UITheme.NAVY_HOVER);
                    b.repaint();
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!Boolean.TRUE.equals(b.getClientProperty("active"))) {
                    b.setBackground(UITheme.NAVY);
                    b.repaint();
                }
            }
        });
        return b;
    }

    private void selectScreen(String name) {
        cardLayout.show(contentArea, name);
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            boolean active = entry.getKey().equals(name);
            JButton b = entry.getValue();
            b.putClientProperty("active", active);
            // Active item becomes a gold rounded pill with navy text.
            b.setBackground(active ? UITheme.GOLD : UITheme.NAVY);
            b.setForeground(active ? UITheme.NAVY : UITheme.SIDEBAR_TEXT);
            b.setFont(active ? UITheme.FONT_BODY_BOLD : UITheme.FONT_BODY);
            b.repaint();
        }
    }

    /* ------------------------------ Main area ------------------------------- */

    private JPanel buildMainArea() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UITheme.BG);
        main.add(buildTopBar(), BorderLayout.NORTH);
        main.add(contentArea, BorderLayout.CENTER);
        return main;
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(18, 24, 0, 24));

        JLabel title = new JLabel("Welcome back, " + currentUser.getFullName().split(" ")[0]);
        title.setFont(UITheme.FONT_H2);
        title.setForeground(UITheme.TEXT);
        bar.add(title, BorderLayout.WEST);

        JButton logout = new JButton("Log out");
        UITheme.secondary(logout);
        logout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(logout);
        bar.add(right, BorderLayout.EAST);

        return bar;
    }
}
