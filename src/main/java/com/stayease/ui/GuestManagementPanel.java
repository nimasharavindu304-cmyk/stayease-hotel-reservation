package com.stayease.ui;

import com.stayease.exception.DuplicateGuestException;
import com.stayease.model.Guest;
import com.stayease.service.GuestService;
import com.stayease.util.ValidationUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/** Input UI: add, edit, delete, search and list Guests. */
public class GuestManagementPanel extends JPanel {

    private final GuestService guestService = new GuestService();

    private final DefaultTableModel tableModel =
            new DefaultTableModel(new Object[]{"ID", "First Name", "Last Name", "NIC/Passport", "Phone", "Email", "Address"}, 0) {
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

    private Integer selectedGuestId = null;

    public GuestManagementPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(buildSearchBar(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildForm(), BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedRow();
            }
        });

        refreshTable(null);
    }

    private JPanel buildSearchBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.add(new JLabel("Search:"));
        bar.add(searchField);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> refreshTable(searchField.getText().trim()));
        JButton clearSearch = new JButton("Show All");
        clearSearch.addActionListener(e -> {
            searchField.setText("");
            refreshTable(null);
        });
        bar.add(searchButton);
        bar.add(clearSearch);
        return bar;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Guest details"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;

        int row = 0;
        addField(form, c, row++, "First name:", firstNameField);
        addField(form, c, row++, "Last name:", lastNameField);
        addField(form, c, row++, "NIC/Passport:", nicField);
        addField(form, c, row++, "Phone (07XXXXXXXX):", phoneField);
        addField(form, c, row++, "Email:", emailField);
        addField(form, c, row++, "Address:", addressField);

        JButton addButton = new JButton("Add New");
        addButton.addActionListener(e -> addGuest());
        JButton updateButton = new JButton("Update Selected");
        updateButton.addActionListener(e -> updateGuest());
        JButton deleteButton = new JButton("Delete Selected");
        deleteButton.addActionListener(e -> deleteGuest());
        JButton clearButton = new JButton("Clear Form");
        clearButton.addActionListener(e -> clearForm());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(addButton);
        buttons.add(updateButton);
        buttons.add(deleteButton);
        buttons.add(clearButton);

        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 2;
        form.add(buttons, c);

        return form;
    }

    private void addField(JPanel form, GridBagConstraints c, int row, String label, JComponent field) {
        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 1;
        form.add(new JLabel(label), c);
        c.gridx = 1;
        form.add(field, c);
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
        selectedGuestId = (Integer) tableModel.getValueAt(row, 0);
        firstNameField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        lastNameField.setText(String.valueOf(tableModel.getValueAt(row, 2)));
        nicField.setText(String.valueOf(tableModel.getValueAt(row, 3)));
        phoneField.setText(String.valueOf(tableModel.getValueAt(row, 4)));
        emailField.setText(tableModel.getValueAt(row, 5) == null ? "" : String.valueOf(tableModel.getValueAt(row, 5)));
        addressField.setText(tableModel.getValueAt(row, 6) == null ? "" : String.valueOf(tableModel.getValueAt(row, 6)));
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
}
