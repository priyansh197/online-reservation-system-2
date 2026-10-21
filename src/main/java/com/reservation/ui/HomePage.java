package com.reservation.ui;

import com.reservation.model.Reservation;
import com.reservation.model.Train;
import com.reservation.model.User;
import com.reservation.service.ReservationService;
import com.reservation.service.ServiceException;
import com.reservation.service.TrainService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.List;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

final class HomePage extends Page {
    private final User user;
    private final ReservationService reservations = new ReservationService();
    private final TrainService trains = new TrainService();
    private final Widgets.Banner banner = new Widgets.Banner();
    private final Widgets.StatCard stat1;
    private final Widgets.StatCard stat2;
    private final Widgets.StatCard stat3 = new Widgets.StatCard("Trains available to book");
    private final DefaultTableModel model = Theme.model("Train No", "Train", "From", "To");

    HomePage(AppFrame app, User user) {
        this.user = user;
        stat1 = new Widgets.StatCard("Active bookings");
        stat2 = new Widgets.StatCard(user.isAdmin() ? "Cancelled bookings" : "Next journey");

        // hero
        Widgets.Gradient hero = new Widgets.Gradient(Theme.PRIMARY, Theme.VIOLET, 22);
        hero.setBorder(new EmptyBorder(30, 34, 30, 34));
        JPanel text = Theme.vbox();
        String first = user.fullName().split(" ")[0];
        text.add(Theme.left(Theme.label("Hello, " + first, 30, true, Color.WHITE)));
        text.add(Box.createVerticalStrut(6));
        text.add(Theme.left(Theme.label("Where are you travelling next?", 16, false, new Color(255, 255, 255, 225))));
        text.add(Box.createVerticalStrut(20));
        JPanel actions = Theme.panel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        Widgets.Btn book = new Widgets.Btn("Book a ticket", Widgets.Kind.LIGHT);
        Widgets.Btn manage = new Widgets.Btn("Manage a booking", Widgets.Kind.OUTLINE_LIGHT);
        book.addActionListener(e -> app.navigate("book"));
        manage.addActionListener(e -> app.navigate("manage"));
        actions.add(book);
        actions.add(Box.createHorizontalStrut(12));
        actions.add(manage);
        text.add(Theme.left(actions));
        hero.add(text, BorderLayout.WEST);

        JPanel stats = Theme.panel(new GridLayout(1, 3, 16, 0));
        stats.add(stat1);
        stats.add(stat2);
        stats.add(stat3);

        JTable table = Theme.table(model);
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (e.getClickCount() == 2 && row >= 0)
                    app.openBooking((Integer) model.getValueAt(table.convertRowIndexToModel(row), 0));
            }
        });
        Widgets.Card trainsCard = new Widgets.Card(22);
        JPanel head = Theme.vbox();
        head.add(Theme.left(Theme.h2("Available trains")));
        head.add(Theme.left(Theme.muted("Double-click a train to start booking it.")));
        head.setBorder(new EmptyBorder(0, 0, 14, 0));
        trainsCard.add(head, BorderLayout.NORTH);
        trainsCard.add(Theme.scroll(table, 292), BorderLayout.CENTER);

        put(hero);
        gap(14);
        put(banner);
        gap(6);
        put(stats);
        gap(22);
        put(trainsCard);
    }

    @Override void onShow() {
        banner.clear();
        try {
            List<Train> active = trains.listActive();
            model.setRowCount(0);
            for (Train t : active) model.addRow(new Object[] {t.number(), t.name(), t.source(), t.destination()});
            stat3.set(String.valueOf(active.size()));

            if (user.isAdmin()) {
                var rep = reservations.report(user);
                stat1.set(String.valueOf(rep.active()));
                stat2.set(String.valueOf(rep.cancelled()));
            } else {
                List<Reservation> mine = reservations.listForUser(user);
                stat1.set(String.valueOf(mine.size()));
                LocalDate next = mine.stream().map(Reservation::journeyDate)
                        .filter(d -> !d.isBefore(LocalDate.now())).min(LocalDate::compareTo).orElse(null);
                stat2.set(next == null ? "None" : next.format(Theme.DATE));
            }
        } catch (ServiceException ex) {
            banner.show(Widgets.Tone.ERROR, ex.getMessage());
        }
    }
}
