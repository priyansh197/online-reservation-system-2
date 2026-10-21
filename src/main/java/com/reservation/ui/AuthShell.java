package com.reservation.ui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/** Split-screen layout shared by the sign-in and sign-up pages: brand panel + form. */
final class AuthShell {
    private AuthShell() { }

    static JPanel wrap(JComponent card) {
        JPanel root = new JPanel(new GridLayout(1, 2));

        Widgets.Gradient left = new Widgets.Gradient(Theme.PRIMARY, Theme.VIOLET, 0);
        left.setLayout(new GridBagLayout());
        left.setBorder(new EmptyBorder(40, 56, 40, 56));
        JPanel inner = Theme.vbox();
        inner.add(Theme.left(Theme.label("RailEase", 44, true, Color.WHITE)));
        inner.add(javax.swing.Box.createVerticalStrut(8));
        inner.add(Theme.left(Theme.label("Train reservations, made simple.", 18, false, new Color(255, 255, 255, 220))));
        inner.add(javax.swing.Box.createVerticalStrut(34));
        for (String s : new String[] {"Book a ticket in a few clicks", "Get a unique PNR instantly",
                "Look up or cancel any booking with its PNR", "Your data stays in your own MySQL database"}) {
            inner.add(Theme.left(Theme.label("\u2022   " + s, 15, false, Color.WHITE)));
            inner.add(javax.swing.Box.createVerticalStrut(12));
        }
        inner.add(javax.swing.Box.createVerticalStrut(40));
        inner.add(Theme.left(Theme.label("Educational project - not an official railway booking service.", 11, false,
                new Color(255, 255, 255, 170))));
        left.add(inner, new GridBagConstraints());

        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(Theme.BG);
        right.add(card, new GridBagConstraints());

        root.add(left);
        root.add(right);
        return root;
    }
}
