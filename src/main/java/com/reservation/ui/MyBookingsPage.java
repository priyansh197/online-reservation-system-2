package com.reservation.ui;

import com.reservation.model.Reservation;
import com.reservation.model.User;
import com.reservation.service.ReservationService;
import com.reservation.service.ServiceException;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.regex.Pattern;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

final class MyBookingsPage extends Page {
    private final User user;
    private final ReservationService service = new ReservationService();
    private final DefaultTableModel model = Theme.model(
            "PNR", "Passenger", "Train", "Class", "Date", "From", "To", "Status", "Booked at");
    private final JTable table = Theme.table(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
    private final Widgets.Field filter = new Widgets.Field("Filter by PNR, passenger, train...", 24);
    private final Widgets.Banner banner = new Widgets.Banner();
    private final CardLayout cards = new CardLayout();
    private final JPanel stack = Theme.panel(cards);

    MyBookingsPage(AppFrame app, User user) {
        this.user = user;
        table.setRowSorter(sorter);
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (e.getClickCount() == 2 && row >= 0)
                    app.openManage((String) model.getValueAt(table.convertRowIndexToModel(row), 0));
            }
        });
        Widgets.onChange(filter, () -> {
            String q = filter.getText().trim();
            sorter.setRowFilter(q.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(q)));
        });

        JPanel title = Theme.vbox();
        title.add(Theme.left(Theme.h1("My bookings")));
        title.add(Theme.left(Theme.muted("Double-click a booking to view it or cancel it.")));
        title.setBorder(new EmptyBorder(0, 0, 18, 0));

        Widgets.Btn refresh = Theme.secondary("Refresh");
        refresh.addActionListener(e -> load());
        JPanel bar = Theme.panel(new BorderLayout(12, 0));
        bar.add(filter, BorderLayout.CENTER);
        bar.add(refresh, BorderLayout.EAST);
        bar.setBorder(new EmptyBorder(0, 0, 14, 0));

        JPanel empty = Theme.vbox();
        empty.setBorder(new EmptyBorder(50, 0, 50, 0));
        empty.add(Theme.left(Theme.h2("No bookings yet")));
        empty.add(Box.createVerticalStrut(6));
        empty.add(Theme.left(Theme.muted("Once you book a ticket it will show up here.")));
        empty.add(Box.createVerticalStrut(16));
        Widgets.Btn go = Theme.primary("Book your first ticket");
        go.addActionListener(e -> app.navigate("book"));
        JPanel goRow = Theme.panel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        goRow.add(go);
        empty.add(Theme.left(goRow));

        stack.add(Theme.scroll(table, 380), "table");
        stack.add(empty, "empty");

        Widgets.Card card = new Widgets.Card(22);
        card.add(bar, BorderLayout.NORTH);
        card.add(stack, BorderLayout.CENTER);

        put(title);
        put(banner);
        put(card);
    }

    @Override void onShow() {
        load();
    }

    private void load() {
        banner.clear();
        try {
            model.setRowCount(0);
            for (Reservation r : service.listForUser(user)) model.addRow(Theme.reservationRow(r));
            cards.show(stack, model.getRowCount() == 0 ? "empty" : "table");
        } catch (ServiceException ex) {
            banner.show(Widgets.Tone.ERROR, ex.getMessage());
        }
    }
}
