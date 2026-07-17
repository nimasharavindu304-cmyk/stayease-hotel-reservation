package com.stayease.ui;

import com.stayease.model.Role;
import com.stayease.model.User;
import com.stayease.service.AuthService;

import javax.swing.*;
import java.awt.*;

/**
 * First screen shown to the user. Split-panel design: a navy brand panel on
 * the left, the sign-in form on the right — the layout used by most modern
 * SaaS products.
 */
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
        setSize(760, 460);
        setLocationRelativeTo(null);
    }

    private void buildUi() {
        JPanel root = new JPanel(new GridLayout(1, 2));
        root.add(buildBrandPanel());
        root.add(buildFormPanel());
        setContentPane(root);
    }

    /** Left half: navy brand panel with logo, tagline and gold accent. */
    private JPanel buildBrandPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Subtle oversized circles for depth (flat, brand-coloured).
                g2.setColor(UITheme.NAVY_HOVER);
                g2.fillOval(-90, getHeight() - 180, 260, 260);
                g2.setColor(UITheme.NAVY_ACTIVE);
                g2.fillOval(getWidth() - 130, -110, 240, 240);
                g2.dispose();
            }
        };
        panel.setBackground(UITheme.NAVY);
        panel.setLayout(new GridBagLayout());

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        JLabel brand = new JLabel("StayEase");
        brand.setFont(UITheme.font(Font.BOLD, 34));
        brand.setForeground(Color.WHITE);
        brand.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel goldRule = new JPanel();
        goldRule.setBackground(UITheme.GOLD);
        goldRule.setMaximumSize(new Dimension(64, 3));
        goldRule.setPreferredSize(new Dimension(64, 3));
        goldRule.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel line1 = new JLabel("Hotel Reservation &");
        JLabel line2 = new JLabel("Booking Management");
        for (JLabel l : new JLabel[]{line1, line2}) {
            l.setFont(UITheme.font(Font.PLAIN, 15));
            l.setForeground(UITheme.SIDEBAR_TEXT);
            l.setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        inner.add(brand);
        inner.add(Box.createVerticalStrut(12));
        inner.add(goldRule);
        inner.add(Box.createVerticalStrut(16));
        inner.add(line1);
        inner.add(line2);

        panel.add(inner);
        return panel;
    }

    /** Right half: the sign-in form. */
    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.CARD);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel welcome = new JLabel("Welcome back");
        welcome.setFont(UITheme.FONT_H1);
        welcome.setForeground(UITheme.TEXT);
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Sign in to the front desk");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(UITheme.FONT_BODY_BOLD);
        userLabel.setForeground(UITheme.MUTED);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        UITheme.placeholder(usernameField, "e.g. admin");
        usernameField.setMaximumSize(new Dimension(280, 36));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(UITheme.FONT_BODY_BOLD);
        passLabel.setForeground(UITheme.MUTED);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        UITheme.placeholder(passwordField, "Enter your password");
        passwordField.setMaximumSize(new Dimension(280, 36));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton loginButton = new JButton("Sign in");
        UITheme.gold(loginButton);
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(280, 40));
        loginButton.addActionListener(e -> doLogin());
        getRootPane().setDefaultButton(loginButton);

        JLabel hint = new JLabel("Default login:  admin  /  admin123");
        hint.setFont(UITheme.FONT_SMALL);
        hint.setForeground(UITheme.MUTED);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(welcome);
        form.add(Box.createVerticalStrut(4));
        form.add(sub);
        form.add(Box.createVerticalStrut(26));
        form.add(userLabel);
        form.add(Box.createVerticalStrut(6));
        form.add(usernameField);
        form.add(Box.createVerticalStrut(14));
        form.add(passLabel);
        form.add(Box.createVerticalStrut(6));
        form.add(passwordField);
        form.add(Box.createVerticalStrut(22));
        form.add(loginButton);
        form.add(Box.createVerticalStrut(14));
        form.add(hint);

        panel.add(form);
        return panel;
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.",
                    "Missing information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Login is currently bypassed: any non-empty username/password signs in
        // as the default administrator. The real database-backed authentication
        // code still lives in AuthService and UserDAO.
        User user = new User(1, username, "", "System Administrator", Role.ADMIN);
        new MainFrame(user).setVisible(true);
        dispose();
    }
}
