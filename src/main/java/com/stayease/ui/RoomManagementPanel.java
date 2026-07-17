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
import java.util.ArrayList;
import java.util.List;

/**
 * Rooms screen: a visual "floor view" of every room as a colour-coded tile
 * (green available, blue occupied, amber maintenance), the full table, and
 * the CRUD form - all in floating rounded cards.
 */
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
    private final FloorView floorView = new FloorView();

    private final JTextField roomNumberField = new JTextField(10);
    private final JComboBox<RoomType> roomTypeCombo = new JComboBox<>(RoomType.values());
    private final JTextField rateField = new JTextField(10);
    private final JTextField capacityField = new JTextField(5);
    private final JComboBox<RoomStatus> statusCombo = new JComboBox<>(RoomStatus.values());
    private final JTextField descriptionField = new JTextField(20);

    private Integer selectedRoomId = null;

    public RoomManagementPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.BG);
        setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        JPanel north = new JPanel(new BorderLayout(0, 12));
        north.setOpaque(false);
        north.add(buildHeader(), BorderLayout.NORTH);
        north.add(buildFloorCard(), BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);

        UITheme.RoundedCard tableCard = new UITheme.RoundedCard(20);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        UITheme.styleTable(table);
        table.getColumnModel().getColumn(5).setCellRenderer(new UITheme.StatusPillRenderer());
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

        refreshTable();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Rooms");
        title.setFont(UITheme.FONT_H1);
        title.setForeground(UITheme.TEXT);
        title.setAlignmentX(LEFT_ALIGNMENT);
        JLabel sub = new JLabel("Floor view and full room inventory");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.MUTED);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(2));
        header.add(sub);
        return header;
    }

    private JPanel buildFloorCard() {
        UITheme.RoundedCard card = new UITheme.RoundedCard(20);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JPanel legendRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        legendRow.setOpaque(false);
        JLabel cardTitle = new JLabel("Floor view");
        cardTitle.setFont(UITheme.FONT_BODY_BOLD);
        cardTitle.setForeground(UITheme.TEXT);
        legendRow.add(cardTitle);
        legendRow.add(legend("Available", UITheme.PILL_GREEN_FG));
        legendRow.add(legend("Occupied", UITheme.PILL_BLUE_FG));
        legendRow.add(legend("Maintenance", UITheme.PILL_AMBER_FG));

        card.add(legendRow, BorderLayout.NORTH);
        card.add(floorView, BorderLayout.CENTER);
        return card;
    }

    private JLabel legend(String text, Color color) {
        JLabel l = new JLabel("● " + text);
        l.setFont(UITheme.FONT_SMALL);
        l.setForeground(color);
        return l;
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
        form.add(fieldLabel("Room number"), c);
        c.gridx = 1;
        form.add(roomNumberField, c);
        c.gridx = 2;
        form.add(fieldLabel("Type"), c);
        c.gridx = 3;
        form.add(roomTypeCombo, c);
        c.gridx = 4;
        form.add(fieldLabel("Rate/night"), c);
        c.gridx = 5;
        form.add(rateField, c);

        c.gridx = 0;
        c.gridy = 1;
        form.add(fieldLabel("Capacity"), c);
        c.gridx = 1;
        form.add(capacityField, c);
        c.gridx = 2;
        form.add(fieldLabel("Status"), c);
        c.gridx = 3;
        form.add(statusCombo, c);
        c.gridx = 4;
        form.add(fieldLabel("Description"), c);
        c.gridx = 5;
        form.add(descriptionField, c);

        JButton addButton = new JButton("Add New");
        UITheme.primary(addButton);
        addButton.addActionListener(e -> addRoom());
        JButton updateButton = new JButton("Update Selected");
        UITheme.secondary(updateButton);
        updateButton.addActionListener(e -> updateRoom());
        JButton deleteButton = new JButton("Delete Selected");
        UITheme.danger(deleteButton);
        deleteButton.addActionListener(e -> deleteRoom());
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
            floorView.setRooms(rooms);
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
        int modelRow = table.convertRowIndexToModel(row);
        selectedRoomId = (Integer) tableModel.getValueAt(modelRow, 0);
        roomNumberField.setText(String.valueOf(tableModel.getValueAt(modelRow, 1)));
        roomTypeCombo.setSelectedItem(tableModel.getValueAt(modelRow, 2));
        rateField.setText(String.valueOf(tableModel.getValueAt(modelRow, 3)));
        capacityField.setText(String.valueOf(tableModel.getValueAt(modelRow, 4)));
        statusCombo.setSelectedItem(tableModel.getValueAt(modelRow, 5));
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
            roomDAO.create(readForm());
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

    /**
     * Hotel "floor view": every room drawn as a rounded tile, colour-coded by
     * status, with the room number inside and type/rate in the tooltip.
     */
    private class FloorView extends JComponent {

        private static final int TILE_W = 74;
        private static final int TILE_H = 44;
        private static final int GAP = 10;

        private List<Room> rooms = new ArrayList<>();

        FloorView() {
            ToolTipManager.sharedInstance().registerComponent(this);
        }

        void setRooms(List<Room> rooms) {
            this.rooms = rooms;
            revalidate();
            repaint();
        }

        private int tilesPerRow() {
            int w = Math.max(getWidth(), TILE_W + GAP);
            return Math.max(1, (w + GAP) / (TILE_W + GAP));
        }

        @Override
        public Dimension getPreferredSize() {
            int perRow = tilesPerRow();
            int rows = rooms.isEmpty() ? 1 : (rooms.size() + perRow - 1) / perRow;
            return new Dimension(0, rows * (TILE_H + GAP));
        }

        @Override
        public String getToolTipText(java.awt.event.MouseEvent e) {
            Room r = roomAt(e.getX(), e.getY());
            if (r == null) {
                return null;
            }
            return r.getRoomNumber() + " - " + r.getRoomType() + " - Rs. "
                    + r.getRatePerNight() + "/night - " + r.getStatus();
        }

        private Room roomAt(int mx, int my) {
            int perRow = tilesPerRow();
            for (int i = 0; i < rooms.size(); i++) {
                int x = (i % perRow) * (TILE_W + GAP);
                int y = (i / perRow) * (TILE_H + GAP);
                if (mx >= x && mx <= x + TILE_W && my >= y && my <= y + TILE_H) {
                    return rooms.get(i);
                }
            }
            return null;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (rooms.isEmpty()) {
                g2.setFont(UITheme.FONT_BODY);
                g2.setColor(UITheme.MUTED);
                g2.drawString("No rooms yet - add your first room below.", 4, 24);
                g2.dispose();
                return;
            }

            int perRow = tilesPerRow();
            for (int i = 0; i < rooms.size(); i++) {
                Room r = rooms.get(i);
                int x = (i % perRow) * (TILE_W + GAP);
                int y = (i / perRow) * (TILE_H + GAP);

                Color bg;
                Color fg;
                switch (r.getStatus()) {
                    case OCCUPIED:
                        bg = UITheme.PILL_BLUE_BG;
                        fg = UITheme.PILL_BLUE_FG;
                        break;
                    case MAINTENANCE:
                        bg = UITheme.PILL_AMBER_BG;
                        fg = UITheme.PILL_AMBER_FG;
                        break;
                    default:
                        bg = UITheme.PILL_GREEN_BG;
                        fg = UITheme.PILL_GREEN_FG;
                }

                g2.setColor(bg);
                g2.fillRoundRect(x, y, TILE_W, TILE_H, 14, 14);
                g2.setColor(fg);
                g2.setStroke(new BasicStroke(1.4f));
                g2.drawRoundRect(x, y, TILE_W, TILE_H, 14, 14);

                g2.setFont(UITheme.font(Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                String num = r.getRoomNumber();
                g2.drawString(num, x + (TILE_W - fm.stringWidth(num)) / 2, y + 20);

                g2.setFont(UITheme.font(Font.PLAIN, 9));
                FontMetrics fm2 = g2.getFontMetrics();
                String type = r.getRoomType().toString();
                g2.drawString(type, x + (TILE_W - fm2.stringWidth(type)) / 2, y + 34);
            }
            g2.dispose();
        }
    }
}
