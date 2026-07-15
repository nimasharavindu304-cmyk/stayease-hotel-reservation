package com.stayease.dao;

import com.stayease.model.User;

import java.sql.SQLException;

public interface UserDAO {
    User findByUsername(String username) throws SQLException;
}
