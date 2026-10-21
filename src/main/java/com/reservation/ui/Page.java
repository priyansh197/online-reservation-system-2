package com.reservation.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

/** A scrollable "web page" inside the main window. Subclasses add content with put()/gap(). */
abstract class Page extends JPanel {
    private final Widgets.ScrollBody body = new Widgets.ScrollBody();

    Page() {
        super(new BorderLayout());
        setBackground(Theme.BG);
        body.setBackground(Theme.BG);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(24, 36, 36, 36));
        JScrollPane sp = new JScrollPane(body);
        sp.setBorder(null);
        sp.getViewport().setBackground(Theme.BG);
        sp.getVerticalScrollBar().setUnitIncrement(18);
        add(sp);
    }

    final void put(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(c);
    }

    final void gap(int px) { body.add(Box.createVerticalStrut(px)); }

    /** Called every time the page is shown, so data is always fresh. */
    void onShow() { }
}
