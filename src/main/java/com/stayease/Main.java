package com.stayease;

import com.stayease.ui.LoginFrame;

import javax.swing.*;

/** Application entry point. */
public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fall back to the default cross-platform look and feel.
        }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
