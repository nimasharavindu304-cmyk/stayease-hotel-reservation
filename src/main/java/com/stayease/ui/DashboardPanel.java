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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The application's Dashboard: at-a-glance occupancy and revenue statistics,
 * a live occupancy bar, and a 7-day revenue trend chart. Implements
 * {@link DashboardObserver} so it refreshes automatically whenever
 * {@link com.stayease.service.BookingService} notifies its
 * {@link com.stayease.observer.BookingSubject} (Observer pattern).
 */
public class DashboardPanel extends JPanel implements DashboardObserver {

    private final RoomDAO roomDAO = new RoomDAOImpl();
    private final BookingDAO bookingDAO = new BookingDAOImpl();

    private final JLabel totalRoomsValue = statValue();
    private final JLabel availableValue = statValue();
    private final JLabel occupiedValue = statValue();
    private final JLabel maintenanceValue = statValue();
    private final JLabel activeStaysValue = statValue();
    private final JLabel revenueTodayValue = statValue();
    private final JLabel statusLabel = new JLabel(" ");
    private final OccupancyBar occupancyBar = new OccupancyBar();
    private final JLabel occupancyLabel = new JLabel("Occupancy -");
    private final RevenueChart revenueChart = new RevenueChart();

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 18, 24));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    private static JLabel statValue() {
        JLabel l = new JLabel("-");
        l.setFont(UITheme.font(Font.BOLD, 26));
        l.setForeground(UITheme.TEXT);
        return l;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 2, 14, 2));

        String today = LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH));

        JLabel title = new JLabel("Overview");
        title.setFont(UITheme.FONT_H1);
        title.setForeground(UITheme.TEXT);

        JLabel sub = new JLabel(today + "  |  Live snapshot of rooms and today's activity");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.MUTED);

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        title.setAlignmentX(LEFT_ALIGNMENT);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        titles.add(title);
        titles.add(Box.createVerticalStrut(3));
        titles.add(sub);
        header.add(titles, BorderLayout.WEST);
        return header;
    }

    private JPanel buildBody() {
        JPanel grid = new JPanel(new GridLayout(2, 3, 14, 14));
        grid.setOpaque(false);
        grid.add(statCard("Total rooms", totalRoomsValue, UITheme.ACCENT_BLUE));
        grid.add(statCard("Available rooms", availableValue, UITheme.ACCENT_GREEN));
        grid.add(statCard("Occupied rooms", occupiedValue, UITheme.ACCENT_AMBER));
        grid.add(statCard("Under maintenance", maintenanceValue, UITheme.ACCENT_RED));
        grid.add(statCard("Active stays today", activeStaysValue, UITheme.ACCENT_TEAL));
        grid.add(statCard("Revenue today (Rs.)", revenueTodayValue, UITheme.ACCENT_PURPLE));

        UITheme.RoundedCard chartCard = new UITheme.RoundedCard(20);
        chartCard.setLayout(new BorderLayout(0, 8));
        JLabel chartTitle = new JLabel("Revenue analyser - last 7 days");
        chartTitle.setFont(UITheme.FONT_H2);
        chartTitle.setForeground(UITheme.TEXT);
        chartCard.add(chartTitle, BorderLayout.NORTH);
        chartCard.add(revenueChart, BorderLayout.CENTER);

        UITheme.RoundedCard occupancyCard = new UITheme.RoundedCard(20);
        occupancyCard.setLayout(new BorderLayout(0, 8));
        occupancyLabel.setFont(UITheme.FONT_BODY_BOLD);
        occupancyLabel.setForeground(UITheme.TEXT);
        occupancyCard.add(occupancyLabel, BorderLayout.NORTH);
        occupancyBar.setPreferredSize(new Dimension(0, 12));
        occupancyCard.add(occupancyBar, BorderLayout.CENTER);

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(grid, BorderLayout.NORTH);
        body.add(chartCard, BorderLayout.CENTER);
        body.add(occupancyCard, BorderLayout.SOUTH);
        return body;
    }

    private JPanel statCard(String title, JLabel valueLabel, Color accent) {
        UITheme.RoundedCard card = new UITheme.RoundedCard(20);
        card.setAccent(accent);
        card.setHoverEffect();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 16));

        JLabel titleLabel = new JLabel(title.toUpperCase(Locale.ENGLISH));
        titleLabel.setFont(UITheme.font(Font.BOLD, 11));
        titleLabel.setForeground(UITheme.MUTED);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(12, 2, 0, 2));

        JButton refreshButton = new JButton("Refresh");
        UITheme.secondary(refreshButton);
        refreshButton.addActionListener(e -> onBookingChanged());

        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.MUTED);

        footer.add(refreshButton, BorderLayout.WEST);
        JPanel statusWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        statusWrap.setOpaque(false);
        statusWrap.add(statusLabel);
        footer.add(statusWrap, BorderLayout.EAST);
        return footer;
    }

    /** Re-queries the database and updates every widget. Called on open and after any booking change. */
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

            revenueChart.setData(bookingDAO.revenueLastDays(7));

            int bookable = (int) (rooms.size() - maintenance);
            int pct = bookable == 0 ? 0 : Math.round(100f * occupied / bookable);
            occupancyBar.setPercent(pct);
            occupancyLabel.setText("Occupancy " + pct + "%  (" + occupied + " of "
                    + bookable + " bookable rooms occupied)");

            statusLabel.setForeground(UITheme.MUTED);
            statusLabel.setText("Last updated " + java.time.LocalTime.now().withNano(0));
        } catch (Exception e) {
            for (JLabel v : new JLabel[]{totalRoomsValue, availableValue, occupiedValue,
                    maintenanceValue, activeStaysValue, revenueTodayValue}) {
                v.setText("-");
            }
            revenueChart.setData(null);
            occupancyBar.setPercent(0);
            occupancyLabel.setText("Occupancy -");
            statusLabel.setForeground(UITheme.ACCENT_RED);
            statusLabel.setText("Database not connected - check MySQL is running and config.properties. ("
                    + e.getMessage() + ")");
        }
    }

    /** A clean line chart of daily revenue, painted by hand (no chart library needed). */
    private static class RevenueChart extends JComponent {

        private LinkedHashMap<LocalDate, BigDecimal> data;

        void setData(LinkedHashMap<LocalDate, BigDecimal> data) {
            this.data = data;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int leftPad = 52;
            int rightPad = 16;
            int topPad = 18;
            int labelZone = 24;
            int chartH = h - topPad - labelZone;

            if (data == null || data.isEmpty()) {
                g2.setFont(UITheme.FONT_BODY);
                g2.setColor(UITheme.MUTED);
                String msg = "No revenue data to display";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2);
                g2.dispose();
                return;
            }

            double max = 0;
            for (BigDecimal v : data.values()) {
                max = Math.max(max, v.doubleValue());
            }
            if (max <= 0) {
                max = 1;
            }

            // Horizontal grid lines with y-axis value labels (0, 1/2, max).
            g2.setFont(UITheme.font(Font.PLAIN, 10));
            for (int q = 0; q <= 2; q++) {
                double frac = q / 2.0;
                int gy = topPad + chartH - (int) Math.round((chartH - 10) * frac);
                g2.setColor(new Color(0xED, 0xF1, 0xF6));
                g2.drawLine(leftPad, gy, w - rightPad, gy);
                g2.setColor(UITheme.MUTED);
                String yLabel = compact(max * frac);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(yLabel, leftPad - 8 - fm.stringWidth(yLabel), gy + 4);
            }

            int n = data.size();
            int plotW = w - leftPad - rightPad;
            int slot = plotW / n;
            LocalDate today = LocalDate.now();
            DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH);

            int[] px = new int[n];
            int[] py = new int[n];
            boolean[] isTodayAt = new boolean[n];

            int i = 0;
            for (Map.Entry<LocalDate, BigDecimal> entry : data.entrySet()) {
                double value = entry.getValue().doubleValue();
                px[i] = leftPad + i * slot + slot / 2;
                py[i] = topPad + chartH - (int) Math.round((chartH - 10) * value / max);
                isTodayAt[i] = entry.getKey().equals(today);

                // Day label along the bottom axis.
                g2.setFont(UITheme.font(isTodayAt[i] ? Font.BOLD : Font.PLAIN, 11));
                g2.setColor(isTodayAt[i] ? UITheme.TEXT : UITheme.MUTED);
                String day = entry.getKey().format(dayFmt);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(day, px[i] - fm.stringWidth(day) / 2, h - 6);

                // Value label above the data point.
                if (value > 0) {
                    g2.setFont(UITheme.font(Font.BOLD, 10));
                    g2.setColor(isTodayAt[i] ? UITheme.GOLD : UITheme.MUTED);
                    String label = compact(value);
                    FontMetrics fm2 = g2.getFontMetrics();
                    g2.drawString(label, px[i] - fm2.stringWidth(label) / 2, Math.max(12, py[i] - 12));
                }
                i++;
            }

            // The line itself: bold navy strokes joining every day's total.
            g2.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(UITheme.NAVY);
            for (int k = 0; k < n - 1; k++) {
                g2.drawLine(px[k], py[k], px[k + 1], py[k + 1]);
            }

            // Solid dots on every point; today's dot is gold and larger.
            for (int k = 0; k < n; k++) {
                int r = isTodayAt[k] ? 7 : 6;
                g2.setColor(isTodayAt[k] ? UITheme.GOLD : UITheme.NAVY);
                g2.fillOval(px[k] - r, py[k] - r, 2 * r, 2 * r);
            }
            g2.dispose();
        }

        private String compact(double v) {
            if (v >= 1000000) {
                return String.format(Locale.ENGLISH, "%.1fM", v / 1000000);
            }
            if (v >= 1000) {
                return String.format(Locale.ENGLISH, "%.1fk", v / 1000);
            }
            return String.valueOf(Math.round(v));
        }
    }

    /** A slim rounded progress bar showing hotel occupancy. */
    private static class OccupancyBar extends JComponent {

        private int percent;

        void setPercent(int percent) {
            this.percent = Math.max(0, Math.min(100, percent));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int arc = h;

            g2.setColor(new Color(0xEA, 0xEF, 0xF5));
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            int fill = Math.round(w * (percent / 100f));
            if (fill > 0) {
                Color c = percent < 60 ? UITheme.ACCENT_GREEN
                        : (percent < 85 ? UITheme.ACCENT_AMBER : UITheme.ACCENT_RED);
                g2.setColor(c);
                g2.fillRoundRect(0, 0, Math.max(fill, h), h, arc, arc);
            }
            g2.dispose();
        }
    }
}
