package com.stayease.ui;

import com.stayease.dao.RoomDAO;
import com.stayease.dao.RoomDAOImpl;
import com.stayease.model.Room;
import com.stayease.model.RoomStatus;
import com.stayease.model.RoomType;
import com.stayease.util.ValidationUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/** Input UI: add, edit, delete and list Rooms. */
public class RoomManagementPanel extends JPanel {

    private final RoomDAO roomDAO = new RoomDAOImpl();

    private final DefaultTableModel tableModel =
            new DefaultTableModel(new Object[]{"ID", "Number", "Type", "Rate/Night", "Capacity", "Status"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
    private final JTable table = new JTable(tableModel);

    private final JTextField roomNumberField = new JTextField(10);
    private final JComboBox<RoomType> roomTypeCombo = new JComboBox<>(RoomType.values());
    private final JTextField rateField = new JTextField(10);
    private final JTextField capacityField = new JTextField(5);
    private final JComboBox<RoomStatus> statusCombo = new JComboBox<>(RoomStatus.values());
    private final JTextField descriptionField = new JTextField(20);

    private Integer selectedRoomId = null;

    public RoomManagementPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildForm(), BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedRow();
            }
        });

        refreshTable();
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Room details"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;

        int row = 0;
        addField(form, c, row++, "Room number:", roomNumberField);
        addField(form, c, row++, "Room type:", roomTypeCombo);
        addField(form, c, row++, "Rate per night:", rateField);
        addField(form, c, row++, "Capacity:", capacityField);
        addField(form, c, row++, "Status:", statusCombo);
        addField(form, c, row++, "Description:", descriptionField);

        JButton addButton = new JButton("Add New");
        addButton.addActionListener(e -> addRoom());
        JButton updateButton = new JButton("Update Selected");
        updateButton.addActionListener(e -> updateRoom());
        JButton deleteButton = new JButton("Delete Selected");
        deleteButton.addActionListener(e -> deleteRoom());
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

    private void refreshTable() {
        tableModel.setRowCount(0);
        try {
            List<Room> rooms = roomDAO.findAll();
            for (Room r : rooms) {
                tableModel.addRow(new Object[]{
                        r.getRoomId(), r.getRoomNumber(), r.getRoomType(),
                        r.getRatePerNight(), r.getCapacity(), r.getStatus()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load rooms: " + e.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSelectedRow() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        selectedRoomId = (Integer) tableModel.getValueAt(row, 0);
        roomNumberField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        roomTypeCombo.setSelectedItem(tableModel.getValueAt(row, 2));
        rateField.setText(String.valueOf(tableModel.getValueAt(row, 3)));
        capacityField.setText(String.valueOf(tableModel.getValueAt(row, 4)));
        statusCombo.setSelectedItem(tableModel.getValueAt(row, 5));
    }

    private boolean validateForm() {
        if (ValidationUtil.isBlank(roomNumberField.getText())) {
            showWarning("Room number is required.");
            return false;
        }
        if (!ValidationUtil.isPositiveNumber(rateField.getText())) {
            showWarning("Rate per night must be a positive number.");
            return false;
        }
        try {
            if (Integer.parseInt(capacityField.getText().trim()) <= 0) {
                showWarning("Capacity must be a positive whole number.");
                return false;
            }
        } catch (NumberFormatException e) {
            showWarning("Capacity must be a whole number.");
            return false;
        }
        return true;
    }

    private void addRoom() {
        if (!validateForm()) {
            return;
        }
        try {
            Room room = readForm();
            roomDAO.create(room);
            refreshTable();
            clearForm();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to add room: " + e.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateRoom() {
        if (selectedRoomId == null) {
            showWarning("Select a room in the table first.");
            return;
        }
        if (!validateForm()) {
            return;
        }
        try {
            Room room = readForm();
            room.setRoomId(selectedRoomId);
            roomDAO.update(room);
            refreshTable();
            clearForm();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to update room: " + e.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteRoom() {
        if (selectedRoomId == null) {
            showWarning("Select a room in the table first.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete the selected room?",
                "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            roomDAO.delete(selectedRoomId);
            refreshTable();
            clearForm();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to delete room (it may have existing bookings): " + e.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Room readForm() {
        Room room = new Room();
        room.setRoomNumber(roomNumberField.getText().trim());
        room.setRoomType((RoomType) roomTypeCombo.getSelectedItem());
        room.setRatePerNight(new BigDecimal(rateField.getText().trim()));
        room.setCapacity(Integer.parseInt(capacityField.getText().trim()));
        room.setStatus((RoomStatus) statusCombo.getSelectedItem());
        room.setDescription(descriptionField.getText().trim());
        return room;
    }

    private void clearForm() {
        selectedRoomId = null;
        roomNumberField.setText("");
        roomTypeCombo.setSelectedIndex(0);
        rateField.setText("");
        capacityField.setText("");
        statusCombo.setSelectedIndex(0);
        descriptionField.setText("");
        table.clearSelection();
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation", JOptionPane.WARNING_MESSAGE);
    }
}
