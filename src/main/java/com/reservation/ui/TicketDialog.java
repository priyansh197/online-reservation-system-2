package com.reservation.ui;

import com.reservation.model.Reservation;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.FlowLayout;
import java.awt.Window;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

final class TicketDialog {
    private TicketDialog() { }

    /** Shows the confirmation ticket. Returns true if the user asked to open "My Bookings". */
    static boolean show(Component parent, Reservation r) {
        Window owner = SwingUtilities.getWindowAncestor(parent);
        JDialog d = new JDialog(owner, "Booking confirmed", Dialog.ModalityType.APPLICATION_MODAL);
        boolean[] openBookings = {false};

        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setBackground(Theme.BG);
        content.setBorder(new EmptyBorder(22, 24, 18, 24));

        JPanel top = Theme.vbox();
        top.add(Theme.left(Theme.h1("Booking confirmed")));
        top.add(Theme.left(Theme.muted("Keep your PNR safe - you need it to view or cancel this booking.")));
        content.add(top, BorderLayout.NORTH);
        content.add(new TicketPanel(r, "E-TICKET  \u00B7  PNR"), BorderLayout.CENTER);

        Widgets.Btn copy = Theme.secondary("Copy PNR");
        Widgets.Btn mine = Theme.secondary("View my bookings");
        Widgets.Btn done = Theme.primary("Done");
        copy.addActionListener(e -> {
            try {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(r.pnr()), null);
                copy.setText("Copied!");
            } catch (RuntimeException ex) {
                copy.setText("Copy failed");
            }
        });
        mine.addActionListener(e -> { openBookings[0] = true; d.dispose(); });
        done.addActionListener(e -> d.dispose());
        JPanel buttons = Theme.panel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.add(copy);
        buttons.add(mine);
        buttons.add(done);
        content.add(buttons, BorderLayout.SOUTH);

        d.setContentPane(content);
        d.pack();
        d.setMinimumSize(new Dimension(620, d.getHeight()));
        d.setSize(Math.max(620, d.getWidth()), d.getHeight());
        d.setLocationRelativeTo(owner);
        d.getRootPane().setDefaultButton(done);
        d.setVisible(true);
        return openBookings[0];
    }
}
