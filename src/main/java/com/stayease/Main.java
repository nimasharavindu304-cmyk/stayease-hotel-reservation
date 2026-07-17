package com.stayease;

import com.formdev.flatlaf.FlatLightLaf;
import com.stayease.ui.LoginFrame;
import com.stayease.ui.UITheme;

import javax.swing.*;

/** Application entry point. Installs the modern FlatLaf look and feel before showing any window. */
public class Main {

    public static void main(String[] args) {
        // Install FlatLaf (modern flat theme) instead of the dated default Swing look.
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            UITheme.applyGlobalDefaults();
        } catch (Exception e) {
            // If FlatLaf is unavailable for any reason, fall back to the system look and feel.
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Last resort: keep the default cross-platform look and feel.
            }
        }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
