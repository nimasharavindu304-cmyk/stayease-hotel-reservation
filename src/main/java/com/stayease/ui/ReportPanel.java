package com.stayease.ui;

import com.stayease.dao.BookingDAO;
import com.stayease.dao.BookingDAOImpl;
import com.stayease.model.Booking;
import com.stayease.report.JasperReportGenerator;
import com.stayease.report.ReportFactory;
import com.stayease.report.ReportType;
import net.sf.jasperreports.engine.JRException;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reports screen. Uses {@link ReportFactory} (Factory pattern) to obtain the
 * correct {@link JasperReportGenerator} for the report the user picked, then
 * either shows it on screen or exports it to PDF.
 */
public class ReportPanel extends JPanel {

    private final BookingDAO bookingDAO = new BookingDAOImpl();

    private final JComboBox<ReportType> reportTypeCombo = new JComboBox<>(ReportType.values());
    private final JComboBox<Booking> bookingCombo = new JComboBox<>();
    private final JSpinner fromDateSpinner = new JSpinner(new SpinnerDateModel());
    private final JSpinner toDateSpinner = new JSpinner(new SpinnerDateModel());

    private final CardLayout paramCards = new CardLayout();
    private final JPanel paramPanel = new JPanel(paramCards);

    public ReportPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0;
        c.gridy = 0;
        top.add(new JLabel("Report:"), c);
        c.gridx = 1;
        top.add(reportTypeCombo, c);
        reportTypeCombo.addActionListener(e -> paramCards.show(paramPanel,
                ((ReportType) reportTypeCombo.getSelectedItem()).name()));

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        top.add(paramPanel, c);

        paramPanel.add(buildInvoiceParams(), ReportType.BOOKING_INVOICE.name());
        paramPanel.add(buildRevenueParams(), ReportType.REVENUE_SUMMARY.name());

        JButton previewButton = new JButton("Preview");
        previewButton.addActionListener(e -> generate(false));
        JButton pdfButton = new JButton("Export to PDF");
        pdfButton.addActionListener(e -> generate(true));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(previewButton);
        buttons.add(pdfButton);
        c.gridy = 2;
        top.add(buttons, c);

        add(top, BorderLayout.NORTH);

        loadBookings();
        fromDateSpinner.setEditor(new JSpinner.DateEditor(fromDateSpinner, "dd-MM-yyyy"));
        toDateSpinner.setEditor(new JSpinner.DateEditor(toDateSpinner, "dd-MM-yyyy"));
        fromDateSpinner.setValue(java.sql.Date.valueOf(LocalDate.now().withDayOfMonth(1)));
        toDateSpinner.setValue(java.sql.Date.valueOf(LocalDate.now().plusDays(1)));
    }

    private JPanel buildInvoiceParams() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(new JLabel("Booking:"));
        p.add(bookingCombo);
        return p;
    }

    private JPanel buildRevenueParams() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(new JLabel("From:"));
        p.add(fromDateSpinner);
        p.add(new JLabel("To:"));
        p.add(toDateSpinner);
        return p;
    }

    private void loadBookings() {
        try {
            List<Booking> bookings = bookingDAO.findAll();
            for (Booking b : bookings) {
                bookingCombo.addItem(b);
            }
            bookingCombo.setRenderer(new DefaultListCellRendererForBooking());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load bookings: " + e.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generate(boolean toPdf) {
        ReportType type = (ReportType) reportTypeCombo.getSelectedItem();
        Map<String, Object> params = new HashMap<>();

        if (type == ReportType.BOOKING_INVOICE) {
            Booking selected = (Booking) bookingCombo.getSelectedItem();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Select a booking first.",
                        "Missing selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            params.put("BOOKING_ID", selected.getBookingId());
        } else {
            Date from = (Date) fromDateSpinner.getValue();
            Date to = (Date) toDateSpinner.getValue();
            params.put("FROM_DATE", new Timestamp(from.getTime()));
            params.put("TO_DATE", new Timestamp(to.getTime()));
        }

        JasperReportGenerator generator = ReportFactory.getGenerator(type);
        try {
            if (toPdf) {
                JFileChooser chooser = new JFileChooser();
                chooser.setSelectedFile(new File(type.name().toLowerCase() + ".pdf"));
                int result = chooser.showSaveDialog(this);
                if (result != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                generator.generateToPdf(params, chooser.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(this, "PDF saved to " + chooser.getSelectedFile().getAbsolutePath());
            } else {
                generator.generateAndShow(params);
            }
        } catch (JRException e) {
            JOptionPane.showMessageDialog(this, "Failed to generate report: " + e.getMessage(),
                    "Report error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Shows "#12 - Jane Doe (Room 201)" instead of Booking's default toString(). */
    private static class DefaultListCellRendererForBooking extends javax.swing.DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                        boolean isSelected, boolean cellHasFocus) {
            String text = value == null ? "" : formatBooking((Booking) value);
            return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
        }

        private String formatBooking(Booking b) {
            return "#" + b.getBookingId() + " - " + b.getGuestName() + " (Room " + b.getRoomNumber() + ")";
        }
    }
}
