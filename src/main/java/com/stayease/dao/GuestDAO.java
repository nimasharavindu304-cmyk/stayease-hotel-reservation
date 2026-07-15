package com.stayease.dao;

import com.stayease.model.Guest;

import java.sql.SQLException;
import java.util.List;

/** DAO pattern (Design Pattern #2): isolates SQL for Guest from the rest of the app. */
public interface GuestDAO {
    Guest create(Guest guest) throws SQLException;
    Guest findById(int guestId) throws SQLException;
    Guest findByNic(String nicPassport) throws SQLException;
    List<Guest> findAll() throws SQLException;
    List<Guest> search(String keyword) throws SQLException;
    boolean update(Guest guest) throws SQLException;
    boolean delete(int guestId) throws SQLException;
}
