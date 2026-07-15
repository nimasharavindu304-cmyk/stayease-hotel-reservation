package com.stayease.service;

import com.stayease.dao.UserDAO;
import com.stayease.dao.UserDAOImpl;
import com.stayease.model.User;
import com.stayease.util.PasswordUtil;

import java.sql.SQLException;

/** Handles staff login. */
public class AuthService {

    private final UserDAO userDAO = new UserDAOImpl();

    /** @return the authenticated User, or null if the credentials are invalid. */
    public User login(String username, String plainPassword) throws SQLException {
        User user = userDAO.findByUsername(username);
        if (user == null) {
            return null;
        }
        return PasswordUtil.matches(plainPassword, user.getPassword()) ? user : null;
    }
}
