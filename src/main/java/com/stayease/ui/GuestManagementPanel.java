package com.stayease.ui;

import com.stayease.exception.DuplicateGuestException;
import com.stayease.model.Guest;
import com.stayease.service.GuestService;
import com.stayease.util.ValidationUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Guests screen: searchable guest directory with avatar initials, plus the
 * registration/edit form - all in floating rounded cards.
 */
public class GuestManagementPanel extends JPanel {

    private final GuestService guestService = new GuestService();

    private final DefaultTableModel tableModel =
            new DefaultTableModel(new Object[]{"ID", "Guest", "Last Name", "NIC/Passport", "Phone", "Email", "Address"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
    private final JTable table = new JTable(tableModel);

    private final JTextField firstNameField = new JTextField(12);
    private final JTextField lastNameField = new JTextField(12);
    private final JTextField nicField = new JTextField(14);
    private final JTextField phoneField = new JTextField(12);
    private final JTextField emailField = new JTextField(16);
    private final JTextField addressField = new JTextField(20);
    private final JTextField searchField = new JTextField(16);
    private final JLabel countLabel = new JLabel(" ");

    private Integer selectedGuestId = null;

    public GuestManagementPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.BG);
        setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        add(buildTop(), BorderLayout.NORTH);

        UITheme.RoundedCard tableCard = new UITheme.RoundedCard(20);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        UITheme.styleTable(table);
        table.setRowHeight(40);
        table.getColumnModel().getColumn(1).setCellRenderer(new AvatarRenderer());
        table.getColumnModel().getColumn(1).setPreferredWidth(170);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UITheme.CARD);
        tableCard.add(scroll, BorderLayout.CENTER);
        add(tableCard, BorderLayout.CENTER);

        add(buildForm(), BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedRow();
            }
        });

        refreshTable(null);
    }

    private JPanel buildTop() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel heading = new JLabel("Guests");
        heading.setFont(UITheme.FONT_H1);
        heading.setForeground(UITheme.TEXT);
        heading.setAlignmentX(LEFT_ALIGNMENT);
        countLabel.setFont(UITheme.FONT_SMALL);
        countLabel.setForeground(UITheme.MUTED);
        countLabel.setAlignmentX(LEFT_ALIGNMENT);
        titles.add(heading);
        titles.add(Box.createVerticalStrut(2));
        titles.add(countLabel);
        top.add(titles, BorderLayout.WEST);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        bar.setOpaque(false);
        UITheme.placeholder(searchField, "Name, NIC or phone");
        bar.add(searchField);
        JButton searchButton = new JButton("Search");
        UITheme.primary(searchButton);
        searchButton.addActionListener(e -> refreshTable(searchField.getText().trim()));
        JButton clearSearch = new JButton("Show All");
        UITheme.secondary(clearSearch);
        clearSearch.addActionListener(e -> {
            searchField.setText("");
            refreshTable(null);
        });
        bar.add(searchButton);
        bar.add(clearSearch);
        top.add(bar, BorderLayout.EAST);
        return top;
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
        form.add(fieldLabel("First name"), c);
        c.gridx = 1;
        form.add(firstNameField, c);
        c.gridx = 2;
        form.add(fieldLabel("Last name"), c);
        c.gridx = 3;
        form.add(lastNameField, c);
        c.gridx = 4;
        form.add(fieldLabel("NIC/Passport"), c);
        c.gridx = 5;
        form.add(nicField, c);

        c.gridx = 0;
        c.gridy = 1;
        form.add(fieldLabel("Phone"), c);
        c.gridx = 1;
        form.add(phoneField, c);
        c.gridx = 2;
        form.add(fieldLabel("Email"), c);
        c.gridx = 3;
        form.add(emailField, c);
        c.gridx = 4;
        form.add(fieldLabel("Address"), c);
        c.gridx = 5;
        form.add(addressField, c);

        JButton addButton = new JButton("Add New");
        UITheme.primary(addButton);
        addButton.addActionListener(e -> addGuest());
        JButton updateButton = new JButton("Update Selected");
        UITheme.secondary(updateButton);
        updateButton.addActionListener(e -> updateGuest());
        JButton deleteButton = new JButton("Delete Selected");
        UITheme.danger(deleteButton);
        deleteButton.addActionListener(e -> deleteGuest());
        JButton clearButton = new JButton("Clear Form");
        UITheme.secondary(clearButton);
        clearButton.addActionListener(e -> clearForm());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        buttons.setOpaque(false);
        buttons.add(addButton);
        buttons.add(updateButton);
        buttons.add(deleteButton);
        buttons.add(clearButton);

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

    private void refreshTable(String keyword) {
        tableModel.setRowCount(0);
        try {
            List<Guest> guests = (keyword == null || keyword.isEmpty())
                    ? guestService.getGuestDAO().findAll()
                    : guestService.getGuestDAO().search(keyword);
            for (Guest g : guests) {
                tableModel.addRow(new Object[]{
                        g.getGuestId(), g.getFirstName(), g.getLastName(),
                        g.getNicPassport(), g.getPhone(), g.getEmail(), g.getAddress()
                });
            }
            countLabel.setText(guests.size() + (guests.size() == 1 ? " guest" : " guests") + " registered");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load guests: " + e.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSelectedRow() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        selectedGuestId = (Integer) tableModel.getValueAt(modelRow, 0);
        firstNameField.setText(String.valueOf(tableModel.getValueAt(modelRow, 1)));
        lastNameField.setText(String.valueOf(tableModel.getValueAt(modelRow, 2)));
        nicField.setText(String.valueOf(tableModel.getValueAt(modelRow, 3)));
        phoneField.setText(String.valueOf(tableModel.getValueAt(modelRow, 4)));
        emailField.setText(tableModel.getValueAt(modelRow, 5) == null ? "" : String.valueOf(tableModel.getValueAt(modelRow, 5)));
        addressField.setText(tableModel.getValueAt(modelRow, 6) == null ? "" : String.valueOf(tableModel.getValueAt(modelRow, 6)));
    }

    private boolean validateForm() {
        if (ValidationUtil.isBlank(firstNameField.getText()) || ValidationUtil.isBlank(lastNameField.getText())) {
            showWarning("First and last name are required.");
            return false;
        }
        if (ValidationUtil.isBlank(nicField.getText())) {
            showWarning("NIC/Passport is required.");
            return false;
        }
        if (!ValidationUtil.isValidPhone(phoneField.getText())) {
            showWarning("Phone must be 10 digits starting with 0 (e.g. 0771234567).");
            return false;
        }
        if (!ValidationUtil.isValidEmail(emailField.getText())) {
            showWarning("Email address is not valid.");
            return false;
        }
        return true;
    }

    private void addGuest() {
        if (!validateForm()) {
            return;
        }
        try {
            guestService.registerGuest(readForm());
            refreshTable(null);
            clearForm();
        } catch (DuplicateGuestException e) {
            showWarning(e.getMessage());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to add guest: " + e.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateGuest() {
        if (selectedGuestId == null) {
            showWarning("Select a guest in the table first.");
            return;
        }
        if (!validateForm()) {
            return;
        }
        try {
            Guest g = readForm();
            g.setGuestId(selectedGuestId);
            guestService.updateGuest(g);
            refreshTable(null);
            clearForm();
        } catch (DuplicateGuestException e) {
            showWarning(e.getMessage());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to update guest: " + e.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteGuest() {
        if (selectedGuestId == null) {
            showWarning("Select a guest in the table first.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete the selected guest?",
                "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            guestService.getGuestDAO().delete(selectedGuestId);
            refreshTable(null);
            clearForm();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to delete guest (they may have existing bookings): " + e.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Guest readForm() {
        Guest g = new Guest();
        g.setFirstName(firstNameField.getText().trim());
        g.setLastName(lastNameField.getText().trim());
        g.setNicPassport(nicField.getText().trim());
        g.setPhone(phoneField.getText().trim());
        g.setEmail(emailField.getText().trim());
        g.setAddress(addressField.getText().trim());
        return g;
    }

    private void clearForm() {
        selectedGuestId = null;
        firstNameField.setText("");
        lastNameField.setText("");
        nicField.setText("");
        phoneField.setText("");
        emailField.setText("");
        addressField.setText("");
        table.clearSelection();
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Renders the guest's first name with a coloured circular avatar showing
     * their initials (first + last name), like modern contact lists.
     */
    private class AvatarRenderer extends DefaultTableCellRenderer {

        private final Color[] palette = {
                UITheme.ACCENT_BLUE, UITheme.ACCENT_GREEN, UITheme.ACCENT_AMBER,
                UITheme.ACCENT_TEAL, UITheme.ACCENT_PURPLE, UITheme.ACCENT_RED
        };

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                        boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            int modelRow = table.convertRowIndexToModel(row);
            String first = String.valueOf(tableModel.getValueAt(modelRow, 1));
            String last = String.valueOf(tableModel.getValueAt(modelRow, 2));
            String initials = (first.isEmpty() ? "" : first.substring(0, 1))
                    + (last.isEmpty() || "null".equals(last) ? "" : last.substring(0, 1));
            Color color = palette[Math.abs((first + last).hashCode()) % palette.length];

            label.setIcon(new AvatarIcon(initials.toUpperCase(), color));
            label.setIconTextGap(10);
            label.setFont(UITheme.FONT_BODY_BOLD);
            if (!isSelected) {
                label.setBackground(row % 2 == 0 ? UITheme.CARD : new Color(0xF7, 0xF9, 0xFC));
                label.setForeground(UITheme.TEXT);
            }
            label.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            return label;
        }
    }

    /** A small filled circle with white initials, drawn as a Swing Icon. */
    private static class AvatarIcon implements Icon {

        private static final int SIZE = 28;
        private final String initials;
        private final Color color;

        AvatarIcon(String initials, Color color) {
            this.initials = initials;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(x, y, SIZE, SIZE);
            g2.setColor(Color.WHITE);
            g2.setFont(UITheme.font(Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (SIZE - fm.stringWidth(initials)) / 2;
            int ty = y + (SIZE + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(initials, tx, ty);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return SIZE;
        }

        @Override
        public int getIconHeight() {
            return SIZE;
        }
    }
}
