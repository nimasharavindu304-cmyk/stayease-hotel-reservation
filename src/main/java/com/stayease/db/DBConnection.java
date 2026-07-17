package com.stayease.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton (Design Pattern #1).
 * <p>
 * Guarantees a single, shared JDBC {@link Connection} for the whole
 * application instead of every DAO opening its own connection. The
 * connection is opened lazily on first use, and any failure is surfaced as
 * a checked {@link SQLException} so the UI layer can report it gracefully
 * instead of crashing.
 */
public final class DBConnection {

    private static DBConnection instance;

    private Connection connection;

    private DBConnection() {
    }

    /** Single shared instance. */
    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    /**
     * Returns the shared connection, opening it on first use (or reopening it
     * if it was closed).
     *
     * @throws SQLException if the driver is missing, config cannot be read,
     *                      or the database cannot be reached / authenticated.
     */
    public Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                Properties props = loadProperties();
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(
                        props.getProperty("db.url"),
                        props.getProperty("db.user"),
                        props.getProperty("db.password"));
            }
            return connection;
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found on the classpath", e);
        } catch (IOException e) {
            throw new SQLException("Could not read config.properties", e);
        }
    }

    private static Properties loadProperties() throws IOException {
        Properties props = new Properties();
        try (InputStream in = DBConnection.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (in == null) {
                throw new IOException("config.properties not found on classpath");
            }
            props.load(in);
        }
        return props;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
