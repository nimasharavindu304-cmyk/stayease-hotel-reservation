package com.stayease.ui;

import com.stayease.model.User;
import com.stayease.service.AuthService;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/** First screen shown to the user. Authenticates against the `users` table. */
public class LoginFrame extends JFrame {

    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final AuthService authService = new AuthService();

    public LoginFrame() {
        super("StayEase - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        buildUi();
        pack();
        setLocationRelativeTo(null);
    }

    private void buildUi() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);

        JLabel title = new JLabel("StayEase Hotel Reservation System");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(title, c);

        c.gridwidth = 1;
        c.anchor = GridBagConstraints.WEST;
        c.gridy = 1;
        c.gridx = 0;
        panel.add(new JLabel("Username:"), c);
        c.gridx = 1;
        panel.add(usernameField, c);

        c.gridy = 2;
        c.gridx = 0;
        panel.add(new JLabel("Password:"), c);
        c.gridx = 1;
        panel.add(passwordField, c);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> doLogin());
        getRootPane().setDefaultButton(loginButton);

        c.gridy = 3;
        c.gridx = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(loginButton, c);

        c.gridy = 4;
        JLabel hint = new JLabel("Default: admin / admin123");
        hint.setFont(hint.getFont().deriveFont(Font.ITALIC, 11f));
        panel.add(hint, c);

        setContentPane(panel);
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.",
                    "Missing information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User user = authService.login(username, password);
            if (user == null) {
                JOptionPane.showMessageDialog(this, "Invalid username or password.",
                        "Login failed", JOptionPane.ERROR_MESSAGE);
                passwordField.setText("");
                return;
            }
            new MainFrame(user).setVisible(true);
            dispose();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Could not reach the database:\n" + e.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
