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
 * instance is created lazily and thread-safely on first use.
 */
public final class DBConnection {

    private static volatile DBConnection instance;

    private Connection connection;

    private DBConnection() {
        try {
            Properties props = loadProperties();
            String url = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String password = props.getProperty("db.password");

            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException | SQLException | IOException e) {
            throw new RuntimeException("Failed to initialise database connection", e);
        }
    }

    /** Double-checked locking so only one instance is ever created. */
    public static DBConnection getInstance() {
        if (instance == null) {
            synchronized (DBConnection.class) {
                if (instance == null) {
                    instance = new DBConnection();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                synchronized (DBConnection.class) {
                    instance = new DBConnection();
                }
                return instance.connection;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Unable to verify database connection state", e);
        }
        return connection;
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
