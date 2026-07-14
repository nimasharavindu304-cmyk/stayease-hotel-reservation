package com.stayease.dao;

import com.stayease.model.Room;
import com.stayease.model.RoomStatus;

import java.sql.SQLException;
import java.util.List;

public interface RoomDAO {
    Room create(Room room) throws SQLException;
    Room findById(int roomId) throws SQLException;
    List<Room> findAll() throws SQLException;
    List<Room> findByStatus(RoomStatus status) throws SQLException;
    List<Room> findAvailableForDates(java.time.LocalDate checkIn, java.time.LocalDate checkOut) throws SQLException;
    boolean update(Room room) throws SQLException;
    boolean updateStatus(int roomId, RoomStatus status) throws SQLException;
    boolean delete(int roomId) throws SQLException;
}
