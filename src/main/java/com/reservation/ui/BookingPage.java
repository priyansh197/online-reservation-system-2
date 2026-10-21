package com.reservation.ui;

import com.reservation.model.BookingRequest;
import com.reservation.model.Reservation;
import com.reservation.model.Train;
import com.reservation.model.User;
import com.reservation.service.ReservationService;
import com.reservation.service.ServiceException;
import com.reservation.service.TrainService;
import com.reservation.util.ValidationUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

final class BookingPage extends Page {
    private static final String PLACEHOLDER = "Select a train";

    private final AppFrame app;
    private final User user;
    private final ReservationService reservations = new ReservationService();
    private final TrainService trains = new TrainService();
    private final Map<String, Train> byLabel = new LinkedHashMap<>();

    private final Widgets.Field passenger = new Widgets.Field("Name as on ID proof", 20);
    private final JComboBox<String> trainBox = new JComboBox<>();
    private final JComboBox<String> fromBox = new JComboBox<>();
    private final JComboBox<String> toBox = new JComboBox<>();
    private final JComboBox<String> classBox = new JComboBox<>(ValidationUtil.CLASS_TYPES.toArray(new String[0]));
    private final Widgets.DatePicker datePicker = new Widgets.DatePicker();
    private final Widgets.Banner banner = new Widgets.Banner();

    private final JLabel sPassenger = Theme.label("-", 14, true, Theme.TEXT);
    private final JLabel sTrain = Theme.label("-", 14, true, Theme.TEXT);
    private final JLabel sRoute = Theme.label("-", 14, true, Theme.TEXT);
    private final JLabel sDate = Theme.label("-", 14, true, Theme.TEXT);
    private final JLabel sClass = Theme.label("-", 14, true, Theme.TEXT);
    private boolean loading;

    BookingPage(AppFrame app, User user) {
        this.app = app;
        this.user = user;
        for (JComboBox<?> c : new JComboBox<?>[] {trainBox, fromBox, toBox, classBox}) Theme.style(c);

        // ----- form card -----
        JPanel f = Theme.vbox();
        f.add(Theme.left(Theme.h2("Journey details")));
        f.add(Theme.left(Theme.muted("Choose a train and fill in the passenger details.")));
        f.add(Box.createVerticalStrut(14));
        f.add(banner);
        f.add(Box.createVerticalStrut(4));
        f.add(Theme.labeled("Passenger name", passenger));
        f.add(Box.createVerticalStrut(14));
        f.add(Theme.labeled("Train", trainBox));
        f.add(Box.createVerticalStrut(14));

        Widgets.Btn swap = Theme.secondary("Swap");
        swap.addActionListener(e -> swapStations());
        JPanel route = Theme.panel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        route.add(Theme.labeled("From", fromBox), c);
        c.weightx = 0;
        c.gridx = 1;
        c.insets = new Insets(20, 10, 0, 10);
        route.add(swap, c);
        c.weightx = 1;
        c.gridx = 2;
        c.insets = new Insets(0, 0, 0, 0);
        route.add(Theme.labeled("To", toBox), c);
        route.setMaximumSize(new Dimension(Integer.MAX_VALUE, route.getPreferredSize().height));
        f.add(Theme.left(route));
        f.add(Box.createVerticalStrut(14));

        JPanel row = Theme.panel(new GridBagLayout());
        GridBagConstraints r = new GridBagConstraints();
        r.fill = GridBagConstraints.HORIZONTAL;
        r.weightx = 1;
        r.gridx = 0;
        row.add(Theme.labeled("Class", classBox), r);
        r.gridx = 1;
        r.insets = new Insets(0, 14, 0, 0);
        row.add(Theme.labeled("Journey date", datePicker), r);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        f.add(Theme.left(row));
        f.add(Box.createVerticalStrut(22));

        Widgets.Btn book = Theme.primary("Confirm booking");
        Widgets.Btn reset = Theme.secondary("Reset");
        book.addActionListener(e -> book());
        reset.addActionListener(e -> { banner.clear(); resetForm(); });
        JPanel buttons = Theme.panel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttons.add(book);
        buttons.add(Box.createHorizontalStrut(10));
        buttons.add(reset);
        f.add(Theme.left(buttons));

        Widgets.Card formCard = new Widgets.Card(26);
        formCard.add(f);

        // ----- summary card -----
        JPanel s = Theme.vbox();
        s.add(Theme.left(Theme.h2("Booking summary")));
        s.add(Box.createVerticalStrut(14));
        addSummary(s, "Passenger", sPassenger);
        addSummary(s, "Train", sTrain);
        addSummary(s, "Route", sRoute);
        addSummary(s, "Journey date", sDate);
        addSummary(s, "Class", sClass);
        s.add(Box.createVerticalStrut(6));
        s.add(Theme.left(Theme.muted("A unique PNR is generated once your booking is saved.")));
        Widgets.Card summaryCard = new Widgets.Card(26);
        summaryCard.add(s);

        JPanel cols = Theme.panel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.NORTH;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridy = 0;
        g.gridx = 0;
        g.weightx = 0.62;
        cols.add(formCard, g);
        g.gridx = 1;
        g.weightx = 0.38;
        g.insets = new Insets(0, 20, 0, 0);
        cols.add(summaryCard, g);

        JPanel title = Theme.vbox();
        title.add(Theme.left(Theme.h1("Book a ticket")));
        title.add(Theme.left(Theme.muted("Reserve your seat in three quick steps.")));
        title.setBorder(new EmptyBorder(0, 0, 18, 0));
        put(title);
        put(cols);

        trainBox.addActionListener(e -> { if (!loading) onTrainChosen(); });
        fromBox.addActionListener(e -> updateSummary());
        toBox.addActionListener(e -> updateSummary());
        classBox.addActionListener(e -> updateSummary());
        datePicker.onChange(this::updateSummary);
        Widgets.onChange(passenger, this::updateSummary);
        updateSummary();
    }

    private static void addSummary(JPanel p, String caption, JLabel value) {
        p.add(Theme.left(Theme.label(caption.toUpperCase(), 10, true, Theme.MUTED)));
        p.add(Box.createVerticalStrut(3));
        p.add(Theme.left(value));
        p.add(Box.createVerticalStrut(14));
    }

    @Override void onShow() {
        String keep = (String) trainBox.getSelectedItem();
        loadTrains();
        if (keep != null && byLabel.containsKey(keep)) trainBox.setSelectedItem(keep);
    }

    private void loadTrains() {
        loading = true;
        try {
            byLabel.clear();
            DefaultComboBoxModel<String> m = new DefaultComboBoxModel<>();
            m.addElement(PLACEHOLDER);
            for (Train t : trains.listActive()) {
                String label = t.number() + "  -  " + t.name();
                byLabel.put(label, t);
                m.addElement(label);
            }
            trainBox.setModel(m);
        } catch (ServiceException ex) {
            banner.show(Widgets.Tone.ERROR, ex.getMessage());
        } finally {
            loading = false;
        }
        onTrainChosen();
    }

    void preselect(int trainNumber) {
        for (Map.Entry<String, Train> e : byLabel.entrySet()) {
            if (e.getValue().number() == trainNumber) {
                trainBox.setSelectedItem(e.getKey());
                return;
            }
        }
    }

    private void onTrainChosen() {
        Train t = byLabel.get((String) trainBox.getSelectedItem());
        if (t == null) {
            fromBox.setModel(new DefaultComboBoxModel<>());
            toBox.setModel(new DefaultComboBoxModel<>());
        } else {
            fromBox.setModel(new DefaultComboBoxModel<>(new String[] {t.source(), t.destination()}));
            toBox.setModel(new DefaultComboBoxModel<>(new String[] {t.source(), t.destination()}));
            toBox.setSelectedIndex(1);
        }
        updateSummary();
    }

    private void swapStations() {
        if (fromBox.getItemCount() < 2) return;
        int a = fromBox.getSelectedIndex();
        fromBox.setSelectedIndex(toBox.getSelectedIndex());
        toBox.setSelectedIndex(a);
    }

    private void updateSummary() {
        String name = passenger.getText().trim();
        sPassenger.setText(name.isEmpty() ? "-" : name);
        sTrain.setText(byLabel.containsKey(trainBox.getSelectedItem()) ? (String) trainBox.getSelectedItem() : "-");
        Object from = fromBox.getSelectedItem(), to = toBox.getSelectedItem();
        sRoute.setText(from == null || to == null ? "-" : from + "  \u2192  " + to);
        sDate.setText(datePicker.getDate().format(Theme.DATE));
        sClass.setText(String.valueOf(classBox.getSelectedItem()));
    }

    private void resetForm() {
        passenger.setText("");
        trainBox.setSelectedIndex(0);
        classBox.setSelectedIndex(0);
        datePicker.setDate(LocalDate.now());
        updateSummary();
    }

    private void book() {
        banner.clear();
        Train t = byLabel.get((String) trainBox.getSelectedItem());
        if (t == null) {
            banner.show(Widgets.Tone.ERROR, "Please select a train.");
            return;
        }
        BookingRequest req = new BookingRequest(passenger.getText(), String.valueOf(t.number()),
                (String) classBox.getSelectedItem(), datePicker.getDate(),
                (String) fromBox.getSelectedItem(), (String) toBox.getSelectedItem());
        try {
            Reservation r = reservations.book(user, req);   // only reached after the DB confirmed the insert
            resetForm();
            banner.show(Widgets.Tone.SUCCESS, "Booking confirmed - your PNR is " + r.pnr());
            if (TicketDialog.show(this, r)) app.navigate("mine");
        } catch (ServiceException ex) {
            banner.show(Widgets.Tone.ERROR, ex.getMessage());   // form values are kept
        }
    }
}
