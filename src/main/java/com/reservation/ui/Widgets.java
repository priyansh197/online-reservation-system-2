package com.reservation.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

/** Custom-painted building blocks that give the Swing app a modern, web-style look. */
final class Widgets {
    private Widgets() { }

    static void aa(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    static void onChange(JTextComponent c, Runnable r) {
        c.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { r.run(); }
            @Override public void removeUpdate(DocumentEvent e) { r.run(); }
            @Override public void changedUpdate(DocumentEvent e) { r.run(); }
        });
    }

    // ================= Card =================
    static class Card extends JPanel {
        Card(int pad) {
            super(new BorderLayout());
            setOpaque(false);
            setBorder(new EmptyBorder(pad, pad, pad + 4, pad));
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            aa(g);
            int w = getWidth(), h = getHeight() - 4;
            g.setColor(new Color(15, 23, 42, 14));
            g.fillRoundRect(0, 3, w, h, 16, 16);
            g.setColor(Theme.SURFACE);
            g.fillRoundRect(0, 0, w, h, 16, 16);
            g.setColor(Theme.BORDER);
            g.drawRoundRect(0, 0, w - 1, h - 1, 16, 16);
            g.dispose();
        }
    }

    static class StatCard extends Card {
        private final JLabel value = Theme.label("-", 28, true, Theme.TEXT);

        StatCard(String caption) {
            super(20);
            JPanel p = Theme.vbox();
            p.add(Theme.left(value));
            p.add(Theme.left(Theme.muted(caption)));
            add(p);
        }

        void set(String v) { value.setText(v); }
    }

    // ================= Buttons =================
    enum Kind { PRIMARY, SECONDARY, DANGER, LIGHT, OUTLINE_LIGHT }

    static class Btn extends JButton {
        private final Kind kind;

        Btn(String text, Kind kind) {
            super(text);
            this.kind = kind;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(Theme.font(Font.BOLD, 13));
        }

        @Override public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(fm.stringWidth(getText()) + 40, 40);
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            aa(g);
            boolean on = isEnabled();
            boolean hover = on && (getModel().isRollover() || getModel().isPressed());
            Color bg, fg, border = null;
            if (!on) {
                bg = new Color(0xE5E7EB);
                fg = new Color(0x9CA3AF);
            } else {
                switch (kind) {
                    case PRIMARY -> { bg = hover ? Theme.PRIMARY_DARK : Theme.PRIMARY; fg = Color.WHITE; }
                    case DANGER -> { bg = hover ? new Color(0xB91C1C) : Theme.DANGER; fg = Color.WHITE; }
                    case LIGHT -> { bg = hover ? new Color(0xE0E7FF) : Color.WHITE; fg = Theme.PRIMARY; }
                    case OUTLINE_LIGHT -> {
                        bg = hover ? new Color(255, 255, 255, 45) : new Color(255, 255, 255, 0);
                        fg = Color.WHITE;
                        border = Color.WHITE;
                    }
                    default -> { bg = hover ? new Color(0xF3F4F6) : Color.WHITE; fg = Theme.TEXT; border = Theme.BORDER; }
                }
            }
            int w = getWidth(), h = getHeight();
            g.setColor(bg);
            g.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
            if (border != null) {
                g.setColor(border);
                g.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);
            }
            g.setFont(getFont());
            g.setColor(fg);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(getText(), (w - fm.stringWidth(getText())) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }

    /** Navigation / tab link with an underline for the active item. */
    static class NavLink extends JToggleButton {
        private final int height;

        NavLink(String text, int height) {
            super(text);
            this.height = height;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(Theme.font(Font.BOLD, 14));
        }

        @Override public Dimension getPreferredSize() {
            return new Dimension(getFontMetrics(getFont()).stringWidth(getText()) + 36, height);
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            aa(g);
            boolean sel = isSelected();
            g.setFont(getFont());
            g.setColor(sel ? Theme.PRIMARY : (getModel().isRollover() ? Theme.TEXT : Theme.MUTED));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            if (sel) {
                g.setColor(Theme.PRIMARY);
                g.fillRoundRect(14, getHeight() - 4, getWidth() - 28, 4, 4, 4);
            }
            g.dispose();
        }
    }

    // ================= Inputs =================
    static class FieldBorder extends AbstractBorder {
        @Override public void paintBorder(Component c, Graphics g0, int x, int y, int w, int h) {
            Graphics2D g = (Graphics2D) g0.create();
            aa(g);
            boolean focus = c.hasFocus();
            g.setColor(focus ? Theme.PRIMARY : new Color(0xD1D5DB));
            g.setStroke(new java.awt.BasicStroke(focus ? 2f : 1f));
            g.drawRoundRect(x + 1, y + 1, w - 3, h - 3, 10, 10);
            g.dispose();
        }

        @Override public Insets getBorderInsets(Component c) { return new Insets(10, 13, 10, 13); }

        @Override public Insets getBorderInsets(Component c, Insets i) {
            i.set(10, 13, 10, 13);
            return i;
        }
    }

    private static void initField(JTextComponent c) {
        c.setFont(Theme.font(Font.PLAIN, 14));
        c.setForeground(Theme.TEXT);
        c.setBackground(Color.WHITE);
        c.setCaretColor(Theme.PRIMARY);
        c.setBorder(new FieldBorder());
        c.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { c.repaint(); }
            @Override public void focusLost(FocusEvent e) { c.repaint(); }
        });
    }

    private static void placeholder(JTextComponent c, Graphics g0, String text) {
        if (text == null || !c.getText().isEmpty()) return;
        Graphics2D g = (Graphics2D) g0.create();
        aa(g);
        g.setColor(new Color(0x9CA3AF));
        g.setFont(c.getFont());
        Insets in = c.getInsets();
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, in.left, in.top + (c.getHeight() - in.top - in.bottom - fm.getHeight()) / 2 + fm.getAscent());
        g.dispose();
    }

    static class Field extends JTextField {
        private final String hint;

        Field(String hint, int cols) {
            super(cols);
            this.hint = hint;
            initField(this);
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            placeholder(this, g, hint);
        }
    }

    static class PassField extends JPasswordField {
        private final String hint;

        PassField(String hint, int cols) {
            super(cols);
            this.hint = hint;
            initField(this);
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            placeholder(this, g, hint);
        }
    }

    // ================= Inline notification =================
    enum Tone { SUCCESS, ERROR, INFO }

    static class Banner extends JPanel {
        private final JLabel text = new JLabel();
        private Color bg = Color.WHITE;

        Banner() {
            super(new BorderLayout());
            setOpaque(false);
            setBorder(new EmptyBorder(10, 14, 10, 14));
            text.setFont(Theme.font(Font.PLAIN, 13));
            add(text);
            setVisible(false);
            setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        void show(Tone tone, String message) {
            switch (tone) {
                case SUCCESS -> { bg = new Color(0xDCFCE7); text.setForeground(new Color(0x166534)); }
                case ERROR -> { bg = new Color(0xFEE2E2); text.setForeground(new Color(0x991B1B)); }
                default -> { bg = new Color(0xE0E7FF); text.setForeground(new Color(0x3730A3)); }
            }
            text.setText(message);
            setVisible(true);
            revalidate();
            repaint();
        }

        void clear() {
            setVisible(false);
            revalidate();
        }

        @Override public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            aa(g);
            g.setColor(bg);
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g.dispose();
        }
    }

    // ================= Gradient hero =================
    static class Gradient extends JPanel {
        private final Color a, b;
        private final int radius;

        Gradient(Color a, Color b, int radius) {
            super(new BorderLayout());
            this.a = a;
            this.b = b;
            this.radius = radius;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            aa(g);
            int w = getWidth(), h = getHeight();
            g.setClip(new RoundRectangle2D.Float(0, 0, w, h, radius, radius));
            g.setPaint(new GradientPaint(0, 0, a, w, h, b));
            g.fillRect(0, 0, w, h);
            g.setColor(new Color(255, 255, 255, 22));
            g.fillOval(w - 240, -90, 340, 340);
            g.fillOval(w - 90, h - 110, 190, 190);
            g.fillOval(-60, h - 60, 160, 160);
            g.dispose();
        }
    }

    // ================= Scroll body =================
    static class ScrollBody extends JPanel implements Scrollable {
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return Math.max(r.height - 32, 16); }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    // ================= Dashed separator (ticket) =================
    static class Dashed extends JPanel {
        Dashed() {
            setOpaque(false);
            setPreferredSize(new Dimension(10, 20));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
            setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setColor(new Color(0xD1D5DB));
            g.setStroke(new java.awt.BasicStroke(1f, java.awt.BasicStroke.CAP_BUTT,
                    java.awt.BasicStroke.JOIN_MITER, 10f, new float[] {6f, 6f}, 0f));
            g.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
            g.dispose();
        }
    }

    // ================= Date picker (calendar popup) =================
    static class DatePicker extends JPanel {
        private LocalDate date = LocalDate.now();
        private YearMonth shown = YearMonth.now();
        private final Field display = new Field(null, 10);
        private final JPopupMenu popup = new JPopupMenu();
        private final List<Runnable> listeners = new ArrayList<>();

        DatePicker() {
            super(new BorderLayout(8, 0));
            setOpaque(false);
            display.setEditable(false);
            display.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            display.setText(date.format(Theme.DATE));
            Btn pick = new Btn("Pick date", Kind.SECONDARY);
            add(display, BorderLayout.CENTER);
            add(pick, BorderLayout.EAST);
            display.addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) { open(); }
            });
            pick.addActionListener(e -> open());
            popup.setBorder(new LineBorder(Theme.BORDER));
            popup.setBackground(Color.WHITE);
        }

        LocalDate getDate() { return date; }

        void setDate(LocalDate d) {
            date = d;
            display.setText(d.format(Theme.DATE));
            listeners.forEach(Runnable::run);
        }

        void onChange(Runnable r) { listeners.add(r); }

        private void open() {
            shown = YearMonth.from(date);
            rebuild();
            popup.show(display, 0, display.getHeight() + 4);
        }

        private void rebuild() {
            popup.removeAll();
            popup.add(calendar());
            popup.revalidate();
            popup.pack();
            popup.repaint();
        }

        private JButton arrow(String t) {
            JButton b = new JButton(t);
            b.setFont(Theme.font(Font.BOLD, 18));
            b.setBorder(new EmptyBorder(0, 10, 0, 10));
            b.setContentAreaFilled(false);
            b.setFocusPainted(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return b;
        }

        private JPanel calendar() {
            JPanel p = new JPanel(new BorderLayout(0, 8));
            p.setBackground(Color.WHITE);
            p.setBorder(new EmptyBorder(12, 12, 12, 12));
            JButton prev = arrow("\u2039"), next = arrow("\u203A");
            prev.setEnabled(shown.isAfter(YearMonth.now()));
            prev.addActionListener(e -> { shown = shown.minusMonths(1); rebuild(); });
            next.addActionListener(e -> { shown = shown.plusMonths(1); rebuild(); });
            JLabel title = Theme.label(shown.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                    + " " + shown.getYear(), 14, true, Theme.TEXT);
            title.setHorizontalAlignment(SwingConstants.CENTER);
            JPanel head = Theme.panel(new BorderLayout());
            head.add(prev, BorderLayout.WEST);
            head.add(title, BorderLayout.CENTER);
            head.add(next, BorderLayout.EAST);

            JPanel grid = Theme.panel(new GridLayout(0, 7, 2, 2));
            for (String d : new String[] {"Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"}) {
                JLabel l = Theme.label(d, 11, true, Theme.MUTED);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                grid.add(l);
            }
            for (int i = 0; i < shown.atDay(1).getDayOfWeek().getValue() - 1; i++) grid.add(new JLabel());
            for (int day = 1; day <= shown.lengthOfMonth(); day++) grid.add(new DayCell(shown.atDay(day)));
            p.add(head, BorderLayout.NORTH);
            p.add(grid, BorderLayout.CENTER);
            return p;
        }

        private final class DayCell extends JLabel {
            private final LocalDate day;
            private final boolean selectable;
            private boolean hover;

            DayCell(LocalDate day) {
                super(String.valueOf(day.getDayOfMonth()), SwingConstants.CENTER);
                this.day = day;
                this.selectable = !day.isBefore(LocalDate.now());
                setFont(Theme.font(Font.PLAIN, 13));
                setPreferredSize(new Dimension(38, 32));
                if (selectable) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    addMouseListener(new MouseAdapter() {
                        @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                        @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                        @Override public void mousePressed(MouseEvent e) {
                            popup.setVisible(false);
                            setDate(DayCell.this.day);
                        }
                    });
                }
            }

            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                aa(g);
                boolean sel = day.equals(date);
                if (sel) {
                    g.setColor(Theme.PRIMARY);
                    g.fillOval(getWidth() / 2 - 15, getHeight() / 2 - 15, 30, 30);
                } else if (hover) {
                    g.setColor(Theme.PRIMARY_LIGHT);
                    g.fillOval(getWidth() / 2 - 15, getHeight() / 2 - 15, 30, 30);
                }
                g.dispose();
                setForeground(sel ? Color.WHITE : (selectable ? Theme.TEXT : new Color(0xD1D5DB)));
                super.paintComponent(g0);
            }
        }
    }

    static JPanel hbox() {
        JPanel p = Theme.panel(null);
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        return p;
    }
}
