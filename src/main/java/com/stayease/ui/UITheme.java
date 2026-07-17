package com.stayease.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Central design system for the whole application.
 * <p>
 * Every screen pulls its colours, fonts, spacing and component styling from
 * here, so the UI stays visually consistent and a single change (say, the
 * brand colour) updates the entire app — the same idea as a CSS theme file
 * in web development.
 */
public final class UITheme {

    private UITheme() {
    }

    /* ----------------------------- Brand palette ----------------------------- */

    public static final Color BG = new Color(0xF5, 0xF7, 0xFA);
    public static final Color CARD = Color.WHITE;
    public static final Color NAVY = new Color(0x15, 0x2A, 0x45);
    public static final Color NAVY_HOVER = new Color(0x1E, 0x3A, 0x5C);
    public static final Color NAVY_ACTIVE = new Color(0x24, 0x44, 0x6B);
    public static final Color GOLD = new Color(0xC8, 0x9B, 0x3C);
    public static final Color GOLD_HOVER = new Color(0xD9, 0xAC, 0x4F);
    public static final Color TEXT = new Color(0x1E, 0x2A, 0x38);
    public static final Color MUTED = new Color(0x6B, 0x7A, 0x8D);
    public static final Color SIDEBAR_TEXT = new Color(0xC3, 0xCD, 0xD9);
    public static final Color BORDER = new Color(0xE2, 0xE8, 0xF0);
    public static final Color SHADOW = new Color(0, 0, 0, 18);

    /* -------------------------- Stat-card accents --------------------------- */

    public static final Color ACCENT_BLUE = new Color(0x2F, 0x80, 0xED);
    public static final Color ACCENT_GREEN = new Color(0x27, 0xAE, 0x60);
    public static final Color ACCENT_AMBER = new Color(0xE2, 0xA0, 0x3F);
    public static final Color ACCENT_RED = new Color(0xEB, 0x57, 0x57);
    public static final Color ACCENT_TEAL = new Color(0x2D, 0x9C, 0xDB);
    public static final Color ACCENT_PURPLE = new Color(0x9B, 0x51, 0xE0);

    /* --------------------------- Pill (badge) tints -------------------------- */

    public static final Color PILL_GREEN_BG = new Color(0xE3, 0xF5, 0xEB);
    public static final Color PILL_GREEN_FG = new Color(0x1B, 0x7A, 0x43);
    public static final Color PILL_AMBER_BG = new Color(0xFB, 0xF0, 0xDC);
    public static final Color PILL_AMBER_FG = new Color(0x9A, 0x66, 0x14);
    public static final Color PILL_RED_BG = new Color(0xFC, 0xE8, 0xE8);
    public static final Color PILL_RED_FG = new Color(0xB0, 0x30, 0x30);
    public static final Color PILL_BLUE_BG = new Color(0xE6, 0xF0, 0xFC);
    public static final Color PILL_BLUE_FG = new Color(0x1D, 0x5B, 0xA8);
    public static final Color PILL_GRAY_BG = new Color(0xEE, 0xF1, 0xF6);
    public static final Color PILL_GRAY_FG = new Color(0x5A, 0x68, 0x7A);

    /* ------------------------------- Fonts ---------------------------------- */

    /**
     * Uses the geometric "Poppins" typeface when it is installed on the system
     * (free from Google Fonts), falling back to Segoe UI otherwise.
     */
    public static final String FONT_FAMILY = resolveFontFamily();

    private static String resolveFontFamily() {
        for (String family : GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames()) {
            if (family.equalsIgnoreCase("Poppins")) {
                return family;
            }
        }
        return "Segoe UI";
    }

    public static Font font(int style, int size) {
        return new Font(FONT_FAMILY, style, size);
    }

    public static final Font FONT_H1 = font(Font.BOLD, 24);
    public static final Font FONT_H2 = font(Font.BOLD, 16);
    public static final Font FONT_BODY = font(Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = font(Font.BOLD, 13);
    public static final Font FONT_STAT = font(Font.BOLD, 32);
    public static final Font FONT_SMALL = font(Font.PLAIN, 12);
    public static final Font FONT_PILL = font(Font.BOLD, 11);

    /** Global FlatLaf tweaks applied once at startup. */
    public static void applyGlobalDefaults() {
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("ProgressBar.arc", 8);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("ScrollBar.trackArc", 999);
        UIManager.put("Table.rowHeight", 32);
        UIManager.put("Component.focusWidth", 1);
    }

    /* --------------------------- Component helpers -------------------------- */

    public static void primary(AbstractButton b) {
        b.setBackground(NAVY);
        b.setForeground(Color.WHITE);
        b.setFont(FONT_BODY_BOLD);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.putClientProperty("JButton.buttonType", "roundRect");
        b.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        hoverize(b, NAVY, NAVY_HOVER);
    }

    public static void gold(AbstractButton b) {
        b.setBackground(GOLD);
        b.setForeground(NAVY);
        b.setFont(FONT_BODY_BOLD);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.putClientProperty("JButton.buttonType", "roundRect");
        b.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        hoverize(b, GOLD, GOLD_HOVER);
    }

    public static void secondary(AbstractButton b) {
        b.setBackground(CARD);
        b.setForeground(NAVY);
        b.setFont(FONT_BODY);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.putClientProperty("JButton.buttonType", "roundRect");
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(7, 16, 7, 16)));
        hoverize(b, CARD, new Color(0xF3, 0xF6, 0xFA));
    }

    public static void danger(AbstractButton b) {
        b.setBackground(CARD);
        b.setForeground(ACCENT_RED);
        b.setFont(FONT_BODY);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.putClientProperty("JButton.buttonType", "roundRect");
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xF3, 0xC2, 0xC2), 1, true),
                BorderFactory.createEmptyBorder(7, 16, 7, 16)));
        hoverize(b, CARD, PILL_RED_BG);
    }

    /** Swaps the background colour on hover for a subtle interactive feel. */
    private static void hoverize(AbstractButton b, Color normal, Color hover) {
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(hover);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(normal);
            }
        });
    }

    /** Consistent table styling: taller rows, zebra stripes, branded header. */
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(32);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(BORDER);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(0xE7, 0xEE, 0xF7));
        table.setSelectionForeground(TEXT);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BODY_BOLD);
        header.setBackground(new Color(0xEE, 0xF1, 0xF6));
        header.setForeground(MUTED);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 36));

        table.setDefaultRenderer(Object.class, new ZebraRenderer());
    }

    /**
     * Renders a table column as a coloured status pill (e.g. a green
     * "AVAILABLE" chip) instead of plain text. Apply per column:
     * {@code table.getColumnModel().getColumn(5).setCellRenderer(new UITheme.StatusPillRenderer());}
     */
    public static class StatusPillRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                        boolean hasFocus, int row, int column) {
            String text = value == null ? "" : value.toString();
            JLabel pill = new JLabel(text, SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    FontMetrics fm = g2.getFontMetrics(getFont());
                    int w = fm.stringWidth(getText()) + 20;
                    int h = 20;
                    int x = (getWidth() - w) / 2;
                    int y = (getHeight() - h) / 2;
                    g2.setColor(getBackground());
                    g2.fillRoundRect(x, y, w, h, h, h);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            pill.setOpaque(false);
            pill.setFont(FONT_PILL);
            pill.setBackground(pillBg(text));
            pill.setForeground(pillFg(text));

            JPanel holder = new JPanel(new BorderLayout());
            holder.setOpaque(true);
            holder.setBackground(isSelected
                    ? table.getSelectionBackground()
                    : (row % 2 == 0 ? CARD : new Color(0xF7, 0xF9, 0xFC)));
            holder.add(pill, BorderLayout.CENTER);
            return holder;
        }

        private Color pillBg(String s) {
            switch (s) {
                case "AVAILABLE":
                case "CHECKED_IN":
                    return PILL_GREEN_BG;
                case "OCCUPIED":
                case "CONFIRMED":
                    return PILL_BLUE_BG;
                case "MAINTENANCE":
                    return PILL_AMBER_BG;
                case "CANCELLED":
                    return PILL_RED_BG;
                default:
                    return PILL_GRAY_BG;
            }
        }

        private Color pillFg(String s) {
            switch (s) {
                case "AVAILABLE":
                case "CHECKED_IN":
                    return PILL_GREEN_FG;
                case "OCCUPIED":
                case "CONFIRMED":
                    return PILL_BLUE_FG;
                case "MAINTENANCE":
                    return PILL_AMBER_FG;
                case "CANCELLED":
                    return PILL_RED_FG;
                default:
                    return PILL_GRAY_FG;
            }
        }
    }

    /**
     * A panel painted with genuinely rounded corners and a soft drop shadow —
     * the "card" look of modern dashboards, done with custom painting since
     * Swing has no built-in equivalent.
     */
    public static class RoundedCard extends JPanel {

        private final int arc;
        private Color accent;
        private boolean hovered;

        public RoundedCard(int arc) {
            this.arc = arc;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        }

        public void setAccent(Color accent) {
            this.accent = accent;
        }

        public void setHoverEffect() {
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int inset = 3;

            // Soft layered shadow (slightly stronger when hovered).
            int layers = hovered ? 4 : 3;
            for (int i = layers; i >= 1; i--) {
                g2.setColor(new Color(0, 0, 0, hovered ? 10 : 7));
                g2.fillRoundRect(inset - i + 2, inset - i + 4, w - 2 * inset + 2 * i - 4,
                        h - 2 * inset + 2 * i - 4, arc + i, arc + i);
            }

            // Card surface.
            g2.setColor(CARD);
            g2.fillRoundRect(inset, inset, w - 2 * inset, h - 2 * inset, arc, arc);

            // Left accent bar, clipped to the rounded shape.
            if (accent != null) {
                Shape old = g2.getClip();
                g2.clip(new java.awt.geom.RoundRectangle2D.Float(
                        inset, inset, w - 2f * inset, h - 2f * inset, arc, arc));
                g2.setColor(accent);
                g2.fillRect(inset, inset, 5, h - 2 * inset);
                g2.setClip(old);
            }

            // Hairline border.
            g2.setColor(BORDER);
            g2.drawRoundRect(inset, inset, w - 2 * inset - 1, h - 2 * inset - 1, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** A titled, padded container border for grouping form controls. */
    public static Border groupBorder(String title) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(BORDER, 1, true), title),
                BorderFactory.createEmptyBorder(8, 10, 8, 10));
    }

    /** Adds placeholder (hint) text to a text field, FlatLaf-style. */
    public static void placeholder(JComponent field, String hint) {
        field.putClientProperty("JTextField.placeholderText", hint);
    }

    /** Alternating (zebra) row background for readability. */
    private static class ZebraRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                        boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? CARD : new Color(0xF7, 0xF9, 0xFC));
                c.setForeground(TEXT);
            }
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            return c;
        }
    }
}
