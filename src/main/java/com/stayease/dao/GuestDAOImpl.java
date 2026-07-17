package com.stayease.dao;

import com.stayease.db.DBConnection;
import com.stayease.model.Guest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class GuestDAOImpl implements GuestDAO {

    private Connection getConnection() throws SQLException {
        return DBConnection.getInstance().getConnection();
    }

    @Override
    public Guest create(Guest g) throws SQLException {
        String sql = "INSERT INTO guests (first_name, last_name, nic_passport, phone, email, address) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, g.getFirstName());
            ps.setString(2, g.getLastName());
            ps.setString(3, g.getNicPassport());
            ps.setString(4, g.getPhone());
            ps.setString(5, g.getEmail());
            ps.setString(6, g.getAddress());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    g.setGuestId(keys.getInt(1));
                }
            }
        }
        return g;
    }

    @Override
    public Guest findById(int guestId) throws SQLException {
        String sql = "SELECT * FROM guests WHERE guest_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, guestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Guest findByNic(String nicPassport) throws SQLException {
        String sql = "SELECT * FROM guests WHERE nic_passport = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, nicPassport);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Guest> findAll() throws SQLException {
        List<Guest> list = new ArrayList<>();
        String sql = "SELECT * FROM guests ORDER BY guest_id DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    @Override
    public List<Guest> search(String keyword) throws SQLException {
        List<Guest> list = new ArrayList<>();
        String sql = "SELECT * FROM guests WHERE first_name LIKE ? OR last_name LIKE ? "
                + "OR nic_passport LIKE ? OR phone LIKE ? ORDER BY guest_id DESC";
        String like = "%" + keyword + "%";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    @Override
    public boolean update(Guest g) throws SQLException {
        String sql = "UPDATE guests SET first_name=?, last_name=?, nic_passport=?, phone=?, email=?, address=? "
                + "WHERE guest_id=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, g.getFirstName());
            ps.setString(2, g.getLastName());
            ps.setString(3, g.getNicPassport());
            ps.setString(4, g.getPhone());
            ps.setString(5, g.getEmail());
            ps.setString(6, g.getAddress());
            ps.setInt(7, g.getGuestId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int guestId) throws SQLException {
        String sql = "DELETE FROM guests WHERE guest_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, guestId);
            return ps.executeUpdate() > 0;
        }
    }

    private Guest map(ResultSet rs) throws SQLException {
        Guest g = new Guest();
        g.setGuestId(rs.getInt("guest_id"));
        g.setFirstName(rs.getString("first_name"));
        g.setLastName(rs.getString("last_name"));
        g.setNicPassport(rs.getString("nic_passport"));
        g.setPhone(rs.getString("phone"));
        g.setEmail(rs.getString("email"));
        g.setAddress(rs.getString("address"));
        if (rs.getTimestamp("created_at") != null) {
            g.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        return g;
    }
}
