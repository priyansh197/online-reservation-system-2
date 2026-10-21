package com.reservation.ui;

import com.reservation.model.Reservation;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.LayoutManager;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/** Design tokens (colours, fonts) and small factory helpers shared by all pages. */
public final class Theme {
    static final Color PRIMARY = new Color(0x4F46E5);
    static final Color PRIMARY_DARK = new Color(0x4338CA);
    static final Color PRIMARY_LIGHT = new Color(0xEEF2FF);
    static final Color VIOLET = new Color(0x7C3AED);
    static final Color BG = new Color(0xF3F4F6);
    static final Color SURFACE = Color.WHITE;
    static final Color TEXT = new Color(0x111827);
    static final Color MUTED = new Color(0x6B7280);
    static final Color BORDER = new Color(0xE5E7EB);
    static final Color DANGER = new Color(0xDC2626);
    static final Color SUCCESS = new Color(0x16A34A);

    static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String FAMILY = "Segoe UI";

    private Theme() { }

    static Font font(int style, float size) {
        return new Font(FAMILY, style, Math.round(size));
    }

    public static void install() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // keep default look and feel
        }
        FontUIResource f = new FontUIResource(font(Font.PLAIN, 13));
        for (String k : new String[] {"Label.font", "TextField.font", "PasswordField.font", "ComboBox.font",
                "Button.font", "CheckBox.font", "OptionPane.messageFont", "OptionPane.buttonFont",
                "Table.font", "ToolTip.font", "PopupMenu.font"}) {
            UIManager.put(k, f);
        }
        UIManager.put("OptionPane.background", SURFACE);
        UIManager.put("Panel.background", SURFACE);
    }

    // ---------- text ----------
    static JLabel label(String text, int size, boolean bold, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font(bold ? Font.BOLD : Font.PLAIN, size));
        l.setForeground(color);
        return l;
    }

    static JLabel h1(String t) { return label(t, 24, true, TEXT); }
    static JLabel h2(String t) { return label(t, 16, true, TEXT); }
    static JLabel muted(String t) { return label(t, 13, false, MUTED); }

    // ---------- containers ----------
    static JPanel panel(LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setOpaque(false);
        return p;
    }

    static JPanel vbox() {
        JPanel p = panel(null);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    static <T extends JComponent> T left(T c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    /** Lets a fixed-size component (e.g. a button) stretch across the width of a BoxLayout column. */
    static JPanel fullWidth(JComponent c) {
        JPanel w = panel(new BorderLayout());
        w.add(c);
        w.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        w.setAlignmentX(Component.LEFT_ALIGNMENT);
        return w;
    }

    /** Caption above a form control. */
    static JPanel labeled(String caption, JComponent field) {
        JPanel p = vbox();
        p.add(left(label(caption, 12, true, TEXT)));
        p.add(javax.swing.Box.createVerticalStrut(6));
        JPanel wrap = panel(new BorderLayout());
        wrap.add(field);
        p.add(left(wrap));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));
        return p;
    }

    // ---------- buttons / inputs ----------
    static Widgets.Btn primary(String t) { return new Widgets.Btn(t, Widgets.Kind.PRIMARY); }
    static Widgets.Btn secondary(String t) { return new Widgets.Btn(t, Widgets.Kind.SECONDARY); }
    static Widgets.Btn danger(String t) { return new Widgets.Btn(t, Widgets.Kind.DANGER); }

    static JButton linkButton(String text) {
        JButton b = new JButton(text);
        b.setFont(font(Font.BOLD, 13));
        b.setForeground(PRIMARY);
        b.setBorder(new EmptyBorder(0, 4, 0, 4));
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        return b;
    }

    static void style(JComboBox<?> cb) {
        cb.setFont(font(Font.PLAIN, 14));
        cb.setBackground(Color.WHITE);
        cb.setPreferredSize(new Dimension(cb.getPreferredSize().width, 42));
    }

    // ---------- tables ----------
    static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    static JTable table(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setRowHeight(38);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFont(font(Font.PLAIN, 13));
        t.setSelectionBackground(PRIMARY_LIGHT);
        t.setSelectionForeground(TEXT);
        t.setFillsViewportHeight(true);
        t.setAutoCreateRowSorter(true);
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel,
                                                                    boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tb, v, sel, false, row, col);
                setBorder(new EmptyBorder(0, 14, 0, 14));
                setForeground(TEXT);
                if (!sel) setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF9FAFB));
                return this;
            }
        });
        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 40));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel,
                                                                    boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tb, v, false, false, row, col);
                setBackground(new Color(0xF9FAFB));
                setForeground(MUTED);
                setFont(font(Font.BOLD, 12));
                setHorizontalAlignment(SwingConstants.LEFT);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER), new EmptyBorder(0, 14, 0, 14)));
                return this;
            }
        });
        return t;
    }

    static JScrollPane scroll(JTable t, int height) {
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(BorderFactory.createLineBorder(BORDER));
        sp.getViewport().setBackground(Color.WHITE);
        sp.setPreferredSize(new Dimension(100, height));
        return sp;
    }

    // ---------- dialogs ----------
    static void error(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    static boolean confirm(Component parent, String title, String msg, String yes, String no) {
        Object[] opts = {yes, no};
        return JOptionPane.showOptionDialog(parent, msg, title, JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE, null, opts, opts[1]) == 0;
    }

    static Object[] reservationRow(Reservation r) {
        return new Object[] {r.pnr(), r.passengerName(), r.trainNumber() + "  " + r.trainName(), r.classType(),
                r.journeyDate().format(DATE), r.source(), r.destination(), r.status(), r.createdAt().format(DATE_TIME)};
    }
}
