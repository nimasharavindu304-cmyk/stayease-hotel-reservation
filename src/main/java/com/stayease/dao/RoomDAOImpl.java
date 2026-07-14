package com.stayease.dao;

import com.stayease.db.DBConnection;
import com.stayease.model.Room;
import com.stayease.model.RoomStatus;
import com.stayease.model.RoomType;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RoomDAOImpl implements RoomDAO {

    private Connection getConnection() {
        return DBConnection.getInstance().getConnection();
    }

    @Override
    public Room create(Room r) throws SQLException {
        String sql = "INSERT INTO rooms (room_number, room_type, rate_per_night, capacity, status, description) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getRoomNumber());
            ps.setString(2, r.getRoomType().name());
            ps.setBigDecimal(3, r.getRatePerNight());
            ps.setInt(4, r.getCapacity());
            ps.setString(5, r.getStatus().name());
            ps.setString(6, r.getDescription());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    r.setRoomId(keys.getInt(1));
                }
            }
        }
        return r;
    }

    @Override
    public Room findById(int roomId) throws SQLException {
        String sql = "SELECT * FROM rooms WHERE room_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Room> findAll() throws SQLException {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms ORDER BY room_number";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    @Override
    public List<Room> findByStatus(RoomStatus status) throws SQLException {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms WHERE status = ? ORDER BY room_number";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    /**
     * Rooms not under maintenance and with no CONFIRMED/CHECKED_IN booking whose
     * date range overlaps [checkIn, checkOut).
     */
    @Override
    public List<Room> findAvailableForDates(LocalDate checkIn, LocalDate checkOut) throws SQLException {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms r WHERE r.status != 'MAINTENANCE' AND r.room_id NOT IN ("
                + "  SELECT b.room_id FROM bookings b "
                + "  WHERE b.status IN ('CONFIRMED','CHECKED_IN') "
                + "    AND b.check_in_date < ? AND b.check_out_date > ?"
                + ") ORDER BY r.room_number";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(checkOut));
            ps.setDate(2, Date.valueOf(checkIn));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    @Override
    public boolean update(Room r) throws SQLException {
        String sql = "UPDATE rooms SET room_number=?, room_type=?, rate_per_night=?, capacity=?, "
                + "status=?, description=? WHERE room_id=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, r.getRoomNumber());
            ps.setString(2, r.getRoomType().name());
            ps.setBigDecimal(3, r.getRatePerNight());
            ps.setInt(4, r.getCapacity());
            ps.setString(5, r.getStatus().name());
            ps.setString(6, r.getDescription());
            ps.setInt(7, r.getRoomId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateStatus(int roomId, RoomStatus status) throws SQLException {
        String sql = "UPDATE rooms SET status=? WHERE room_id=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, roomId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int roomId) throws SQLException {
        String sql = "DELETE FROM rooms WHERE room_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, roomId);
            return ps.executeUpdate() > 0;
        }
    }

    private Room map(ResultSet rs) throws SQLException {
        Room r = new Room();
        r.setRoomId(rs.getInt("room_id"));
        r.setRoomNumber(rs.getString("room_number"));
        r.setRoomType(RoomType.valueOf(rs.getString("room_type")));
        r.setRatePerNight(rs.getBigDecimal("rate_per_night"));
        r.setCapacity(rs.getInt("capacity"));
        r.setStatus(RoomStatus.valueOf(rs.getString("status")));
        r.setDescription(rs.getString("description"));
        return r;
    }
}
