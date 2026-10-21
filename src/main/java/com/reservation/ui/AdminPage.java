package com.reservation.ui;

import com.reservation.model.Reservation;
import com.reservation.model.Train;
import com.reservation.model.User;
import com.reservation.service.ReservationService;
import com.reservation.service.ServiceException;
import com.reservation.service.TrainService;
import com.reservation.util.ValidationUtil;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/** Administrator console: train management, all reservations, reports. */
final class AdminPage extends Page {
    private final User admin;
    private final TrainService trainService = new TrainService();
    private final ReservationService reservationService = new ReservationService();
    private final Widgets.Banner banner = new Widgets.Banner();

    private final DefaultTableModel trainModel = Theme.model("Train No", "Train name", "From", "To", "Status");
    private final JTable trainTable = Theme.table(trainModel);
    private final List<Train> shownTrains = new ArrayList<>();
    private final Widgets.Field trainSearch = new Widgets.Field("Search by train number or name", 22);

    private final DefaultTableModel resModel = Theme.model(
            "PNR", "Passenger", "Train", "Class", "Date", "From", "To", "Status", "Booked at");
    private final JTable resTable = Theme.table(resModel);
    private final TableRowSorter<DefaultTableModel> resSorter = new TableRowSorter<>(resModel);
    private final Widgets.Field resFilter = new Widgets.Field("Filter by PNR, passenger, train, date...", 22);

    private final Widgets.StatCard sActive = new Widgets.StatCard("Active bookings");
    private final Widgets.StatCard sCancelled = new Widgets.StatCard("Cancelled bookings");
    private final Widgets.StatCard sTotal = new Widgets.StatCard("Total bookings ever made");
    private final DefaultTableModel reportModel = Theme.model("Train", "Active bookings");

    AdminPage(User admin) {
        if (admin == null || !admin.isAdmin()) throw new IllegalStateException("Administrator role required");
        this.admin = admin;
        trainTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resTable.setRowSorter(resSorter);

        JPanel title = Theme.vbox();
        title.add(Theme.left(Theme.h1("Admin console")));
        title.add(Theme.left(Theme.muted("Manage trains, review reservations and see how the system is used.")));
        title.setBorder(new EmptyBorder(0, 0, 14, 0));

        CardLayout cards = new CardLayout();
        JPanel host = Theme.panel(cards);
        host.add(trainsTab(), "trains");
        host.add(reservationsTab(), "res");
        host.add(reportsTab(), "report");

        JPanel tabs = Theme.panel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabs.setBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER));
        ButtonGroup group = new ButtonGroup();
        String[][] defs = {{"Trains", "trains"}, {"Reservations", "res"}, {"Reports", "report"}};
        for (String[] d : defs) {
            Widgets.NavLink l = new Widgets.NavLink(d[0], 46);
            l.addActionListener(e -> cards.show(host, d[1]));
            group.add(l);
            tabs.add(l);
            if (d[1].equals("trains")) l.setSelected(true);
        }

        put(title);
        put(banner);
        gap(4);
        put(tabs);
        gap(16);
        put(host);
    }

    @Override void onShow() {
        banner.clear();
        refreshTrains();
        refreshReservations();
        refreshReport();
    }

    // ---------------- Trains ----------------
    private JPanel trainsTab() {
        Widgets.Btn search = Theme.secondary("Search");
        Widgets.Btn all = Theme.secondary("Show all");
        Widgets.Btn add = Theme.primary("Add train");
        Widgets.Btn edit = Theme.secondary("Edit");
        Widgets.Btn toggle = Theme.secondary("Activate / Deactivate");
        search.addActionListener(e -> refreshTrains());
        trainSearch.addActionListener(e -> refreshTrains());
        all.addActionListener(e -> { trainSearch.setText(""); refreshTrains(); });
        add.addActionListener(e -> trainDialog(null));
        edit.addActionListener(e -> { Train t = selectedTrain(); if (t != null) trainDialog(t); });
        toggle.addActionListener(e -> toggleSelected());

        JPanel left = Theme.panel(new BorderLayout(10, 0));
        JPanel btns = Theme.panel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.add(search);
        btns.add(all);
        left.add(trainSearch, BorderLayout.CENTER);
        left.add(btns, BorderLayout.EAST);
        JPanel right = Theme.panel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.add(edit);
        right.add(toggle);
        right.add(add);

        JPanel bar = Theme.panel(new BorderLayout(20, 0));
        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        bar.setBorder(new EmptyBorder(0, 0, 14, 0));

        Widgets.Card card = new Widgets.Card(22);
        card.add(bar, BorderLayout.NORTH);
        card.add(Theme.scroll(trainTable, 360), BorderLayout.CENTER);
        JPanel p = Theme.panel(new BorderLayout());
        p.add(card);
        return p;
    }

    private void refreshTrains() {
        try {
            List<Train> list = trainService.search(admin, trainSearch.getText());
            shownTrains.clear();
            shownTrains.addAll(list);
            trainModel.setRowCount(0);
            for (Train t : list)
                trainModel.addRow(new Object[] {t.number(), t.name(), t.source(), t.destination(),
                        t.active() ? "Active" : "Inactive"});
        } catch (ServiceException ex) {
            banner.show(Widgets.Tone.ERROR, ex.getMessage());
        }
    }

    private Train selectedTrain() {
        int row = trainTable.getSelectedRow();
        if (row < 0) {
            banner.show(Widgets.Tone.INFO, "Select a train in the table first.");
            return null;
        }
        banner.clear();
        return shownTrains.get(trainTable.convertRowIndexToModel(row));
    }

    private void toggleSelected() {
        Train t = selectedTrain();
        if (t == null) return;
        try {
            trainService.setActive(admin, t.number(), !t.active());
            banner.show(Widgets.Tone.SUCCESS, "Train " + t.number() + (t.active() ? " deactivated." : " activated."));
            refreshTrains();
        } catch (ServiceException ex) {
            banner.show(Widgets.Tone.ERROR, ex.getMessage());
        }
    }

    /** Add (existing == null) or edit a train; re-prompts with the typed values if validation fails. */
    private void trainDialog(Train existing) {
        Widgets.Field number = new Widgets.Field("e.g. 12345", 20);
        Widgets.Field name = new Widgets.Field("e.g. Gujarat Express", 20);
        Widgets.Field source = new Widgets.Field("Source station", 20);
        Widgets.Field dest = new Widgets.Field("Destination station", 20);
        JCheckBox active = new JCheckBox("Active (can be booked)", existing == null || existing.active());
        active.setOpaque(false);
        if (existing != null) {
            number.setText(String.valueOf(existing.number()));
            number.setEnabled(false);
            name.setText(existing.name());
            source.setText(existing.source());
            dest.setText(existing.destination());
        }
        JPanel form = Theme.vbox();
        form.setOpaque(true);
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(6, 6, 6, 6));
        form.add(Theme.labeled("Train number", number));
        form.add(Box.createVerticalStrut(10));
        form.add(Theme.labeled("Train name", name));
        form.add(Box.createVerticalStrut(10));
        form.add(Theme.labeled("Source station", source));
        form.add(Box.createVerticalStrut(10));
        form.add(Theme.labeled("Destination station", dest));
        form.add(Box.createVerticalStrut(10));
        form.add(Theme.left(active));

        while (true) {
            int ok = JOptionPane.showConfirmDialog(this, form, existing == null ? "Add train" : "Edit train",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (ok != JOptionPane.OK_OPTION) return;
            try {
                int n = existing != null ? existing.number() : ValidationUtil.parseTrainNumber(number.getText());
                if (existing == null)
                    trainService.add(admin, n, name.getText(), source.getText(), dest.getText(), active.isSelected());
                else
                    trainService.update(admin, n, name.getText(), source.getText(), dest.getText(), active.isSelected());
                banner.show(Widgets.Tone.SUCCESS, "Train " + n + (existing == null ? " added." : " updated."));
                refreshTrains();
                refreshReport();
                return;
            } catch (ServiceException ex) {
                Theme.error(this, ex.getMessage());
            }
        }
    }

    // ---------------- Reservations ----------------
    private JPanel reservationsTab() {
        Widgets.Btn refresh = Theme.secondary("Refresh");
        Widgets.Btn cancel = Theme.danger("Cancel selected");
        refresh.addActionListener(e -> { refreshReservations(); refreshReport(); });
        cancel.addActionListener(e -> cancelSelected());
        Widgets.onChange(resFilter, () -> {
            String q = resFilter.getText().trim();
            resSorter.setRowFilter(q.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(q)));
        });

        JPanel right = Theme.panel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.add(refresh);
        right.add(cancel);
        JPanel bar = Theme.panel(new BorderLayout(20, 0));
        bar.add(resFilter, BorderLayout.CENTER);
        bar.add(right, BorderLayout.EAST);
        bar.setBorder(new EmptyBorder(0, 0, 14, 0));

        Widgets.Card card = new Widgets.Card(22);
        card.add(bar, BorderLayout.NORTH);
        card.add(Theme.scroll(resTable, 360), BorderLayout.CENTER);
        JPanel p = Theme.panel(new BorderLayout());
        p.add(card);
        return p;
    }

    private void refreshReservations() {
        try {
            resModel.setRowCount(0);
            for (Reservation r : reservationService.listAll(admin)) resModel.addRow(Theme.reservationRow(r));
        } catch (ServiceException ex) {
            banner.show(Widgets.Tone.ERROR, ex.getMessage());
        }
    }

    private void cancelSelected() {
        int row = resTable.getSelectedRow();
        if (row < 0) {
            banner.show(Widgets.Tone.INFO, "Select a reservation in the table first.");
            return;
        }
        String pnr = (String) resModel.getValueAt(resTable.convertRowIndexToModel(row), 0);
        if (!Theme.confirm(this, "Cancel reservation", "Cancel reservation " + pnr + "?\nThis cannot be undone.",
                "Yes, cancel it", "Keep it")) return;
        try {
            reservationService.cancel(admin, pnr);
            banner.show(Widgets.Tone.SUCCESS, "Reservation " + pnr + " cancelled.");
            refreshReservations();
            refreshReport();
        } catch (ServiceException ex) {
            banner.show(Widgets.Tone.ERROR, ex.getMessage());
        }
    }

    // ---------------- Reports ----------------
    private JPanel reportsTab() {
        JPanel stats = Theme.panel(new GridLayout(1, 3, 16, 0));
        stats.add(sActive);
        stats.add(sCancelled);
        stats.add(sTotal);
        Widgets.Card card = new Widgets.Card(22);
        JPanel head = Theme.vbox();
        head.add(Theme.left(Theme.h2("Active bookings per train")));
        head.setBorder(new EmptyBorder(0, 0, 14, 0));
        card.add(head, BorderLayout.NORTH);
        card.add(Theme.scroll(Theme.table(reportModel), 300), BorderLayout.CENTER);

        JPanel p = Theme.panel(new BorderLayout(0, 18));
        p.add(stats, BorderLayout.NORTH);
        p.add(card, BorderLayout.CENTER);
        return p;
    }

    private void refreshReport() {
        try {
            ReservationService.Report r = reservationService.report(admin);
            sActive.set(String.valueOf(r.active()));
            sCancelled.set(String.valueOf(r.cancelled()));
            sTotal.set(String.valueOf(r.active() + r.cancelled()));
            reportModel.setRowCount(0);
            for (Map.Entry<String, Integer> e : r.perTrain().entrySet())
                reportModel.addRow(new Object[] {e.getKey(), e.getValue()});
        } catch (ServiceException ex) {
            banner.show(Widgets.Tone.ERROR, ex.getMessage());
        }
    }
}
