package com.reservation.ui;

import com.reservation.model.Reservation;
import com.reservation.model.User;
import com.reservation.service.ReservationService;
import com.reservation.service.ServiceException;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

final class ManagePage extends Page {
    private final User user;
    private final ReservationService service = new ReservationService();
    private final Widgets.Field pnrField = new Widgets.Field("Enter your 10-character PNR", 24);
    private final Widgets.Banner banner = new Widgets.Banner();
    private final JPanel result = Theme.panel(new BorderLayout());
    private Reservation current;

    ManagePage(AppFrame app, User user) {
        this.user = user;

        JPanel title = Theme.vbox();
        title.add(Theme.left(Theme.h1("Manage a booking")));
        title.add(Theme.left(Theme.muted("Look up a reservation by PNR and cancel it if your plans change.")));
        title.setBorder(new EmptyBorder(0, 0, 18, 0));

        Widgets.Btn find = Theme.primary("Find booking");
        find.addActionListener(e -> find());
        pnrField.addActionListener(e -> find());
        JPanel searchRow = Theme.panel(new BorderLayout(12, 0));
        searchRow.add(pnrField, BorderLayout.CENTER);
        searchRow.add(find, BorderLayout.EAST);
        Widgets.Card searchCard = new Widgets.Card(24);
        JPanel sc = Theme.vbox();
        sc.add(Theme.left(Theme.labeled("PNR number", searchRow)));
        sc.add(Box.createVerticalStrut(6));
        sc.add(banner);
        searchCard.add(sc);

        result.setVisible(false);
        put(title);
        put(searchCard);
        gap(20);
        put(result);
    }

    @Override void onShow() {
        banner.clear();
    }

    void lookup(String pnr) {
        pnrField.setText(pnr);
        find();
    }

    private void hideResult() {
        current = null;
        result.removeAll();
        result.setVisible(false);
    }

    private void find() {
        banner.clear();
        hideResult();
        try {
            current = service.find(user, pnrField.getText());
        } catch (ServiceException ex) {
            banner.show(Widgets.Tone.ERROR, ex.getMessage());
            return;
        }
        Widgets.Btn cancel = Theme.danger("Cancel this ticket");
        cancel.addActionListener(e -> cancel());
        JPanel actions = Theme.panel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actions.setBorder(new EmptyBorder(16, 0, 0, 0));
        actions.add(cancel);
        result.add(new TicketPanel(current, "E-TICKET  \u00B7  PNR"), BorderLayout.CENTER);
        result.add(actions, BorderLayout.SOUTH);
        result.setVisible(true);
        result.revalidate();
        result.repaint();
    }

    private void cancel() {
        if (current == null) return;
        String pnr = current.pnr();
        boolean yes = Theme.confirm(this, "Cancel ticket",
                "Cancel the ticket for " + current.passengerName() + " (PNR " + pnr + ")?\nThis cannot be undone.",
                "Yes, cancel ticket", "Keep ticket");
        if (!yes) return;   // user declined: nothing changes
        try {
            service.cancel(user, pnr);
            hideResult();
            pnrField.setText("");
            banner.show(Widgets.Tone.SUCCESS, "Reservation " + pnr + " has been cancelled.");
        } catch (ServiceException ex) {
            banner.show(Widgets.Tone.ERROR, ex.getMessage());
        }
        result.revalidate();
        result.repaint();
    }
}
