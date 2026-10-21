package com.reservation.ui;

import com.reservation.model.Reservation;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/** E-ticket style view of a reservation. */
final class TicketPanel extends Widgets.Card {
    TicketPanel(Reservation r, String headline) {
        super(16);
        Widgets.Gradient head = new Widgets.Gradient(Theme.PRIMARY, Theme.VIOLET, 14);
        head.setBorder(new EmptyBorder(16, 20, 16, 20));
        JPanel left = Theme.vbox();
        left.add(Theme.left(Theme.label(headline, 11, true, new Color(255, 255, 255, 190))));
        JLabel pnr = new JLabel(r.pnr());
        pnr.setFont(new Font(Font.MONOSPACED, Font.BOLD, 28));
        pnr.setForeground(Color.WHITE);
        left.add(Theme.left(pnr));
        JLabel status = Theme.label(r.status(), 13, true, Color.WHITE);
        head.add(left, BorderLayout.WEST);
        head.add(status, BorderLayout.EAST);

        JPanel body = Theme.vbox();
        body.setBorder(new EmptyBorder(18, 6, 4, 6));
        body.add(Theme.left(Theme.label(r.source() + "  \u2192  " + r.destination(), 20, true, Theme.TEXT)));
        body.add(Theme.left(Theme.muted(r.trainNumber() + "  \u00B7  " + r.trainName())));
        body.add(new Widgets.Dashed());
        JPanel grid = Theme.panel(new GridLayout(0, 3, 16, 14));
        grid.add(kv("Passenger", r.passengerName()));
        grid.add(kv("Journey date", r.journeyDate().format(Theme.DATE)));
        grid.add(kv("Class", r.classType()));
        grid.add(kv("Train", String.valueOf(r.trainNumber())));
        grid.add(kv("Status", r.status()));
        grid.add(kv("Booked at", r.createdAt().format(Theme.DATE_TIME)));
        body.add(Theme.left(grid));

        add(head, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
    }

    private static JPanel kv(String k, String v) {
        JPanel p = Theme.vbox();
        p.add(Theme.left(Theme.label(k.toUpperCase(), 10, true, Theme.MUTED)));
        p.add(Box.createVerticalStrut(3));
        p.add(Theme.left(Theme.label(v, 14, true, Theme.TEXT)));
        return p;
    }
}
