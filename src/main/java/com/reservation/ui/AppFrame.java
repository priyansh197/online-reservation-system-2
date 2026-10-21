package com.reservation.ui;

import com.reservation.model.User;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/** The single application window: auth screens, or navbar + pages + footer once signed in. */
public class AppFrame extends JFrame {
    private final CardLayout rootCards = new CardLayout();
    private final JPanel root = new JPanel(rootCards);
    private final CardLayout authCards = new CardLayout();
    private final JPanel authHost = new JPanel(authCards);
    private final LoginPage loginPage = new LoginPage(this);
    private final RegisterPage registerPage = new RegisterPage(this);

    private JPanel shell;
    private CardLayout pageCards;
    private JPanel pageHost;
    private final Map<String, Page> pages = new LinkedHashMap<>();
    private final Map<String, Widgets.NavLink> links = new LinkedHashMap<>();
    private final ButtonGroup navGroup = new ButtonGroup();

    public AppFrame() {
        super("RailEase - Online Reservation System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        authHost.add(loginPage, "login");
        authHost.add(registerPage, "register");
        root.add(authHost, "auth");
        getContentPane().add(root);
        setMinimumSize(new Dimension(1020, 700));
        setSize(1200, 780);
        setLocationRelativeTo(null);
    }

    // ---------- auth navigation ----------
    void showLogin(String successMessage) {
        if (successMessage != null) loginPage.showSuccess(successMessage);
        rootCards.show(root, "auth");
        authCards.show(authHost, "login");
    }

    void showRegister() {
        registerPage.reset();
        authCards.show(authHost, "register");
    }

    // ---------- session ----------
    void startSession(User user) {
        if (shell != null) root.remove(shell);
        pages.clear();
        links.clear();
        pageCards = new CardLayout();
        pageHost = new JPanel(pageCards);
        pageHost.setBackground(Theme.BG);

        JPanel navLinks = Theme.panel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        register(navLinks, "home", "Home", new HomePage(this, user));
        register(navLinks, "book", "Book Ticket", new BookingPage(this, user));
        register(navLinks, "manage", "Manage Booking", new ManagePage(this, user));
        register(navLinks, "mine", "My Bookings", new MyBookingsPage(this, user));
        if (user.isAdmin()) register(navLinks, "admin", "Admin", new AdminPage(user));

        shell = new JPanel(new BorderLayout());
        shell.add(navbar(user, navLinks), BorderLayout.NORTH);
        shell.add(pageHost, BorderLayout.CENTER);
        JLabel footer = Theme.label("RailEase  \u00B7  Educational project - not an official railway booking service",
                11, false, Theme.MUTED);
        footer.setHorizontalAlignment(SwingConstants.CENTER);
        footer.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, Theme.BORDER), new EmptyBorder(8, 0, 8, 0)));
        footer.setOpaque(true);
        footer.setBackground(Theme.SURFACE);
        shell.add(footer, BorderLayout.SOUTH);

        root.add(shell, "app");
        rootCards.show(root, "app");
        navigate("home");
        root.revalidate();
    }

    private void register(JPanel navLinks, String key, String title, Page page) {
        Widgets.NavLink link = new Widgets.NavLink(title, 64);
        link.addActionListener(e -> navigate(key));
        navGroup.add(link);
        navLinks.add(link);
        links.put(key, link);
        pages.put(key, page);
        pageHost.add(page, key);
    }

    private JPanel navbar(User user, JPanel navLinks) {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(Theme.SURFACE);
        nav.setBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER));

        JLabel brand = Theme.label("RailEase", 22, true, Theme.PRIMARY);
        brand.setBorder(new EmptyBorder(0, 28, 0, 32));
        JPanel leftSide = Theme.panel(new BorderLayout());
        leftSide.add(brand, BorderLayout.WEST);
        leftSide.add(navLinks, BorderLayout.CENTER);

        JPanel right = Theme.panel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        JLabel who = Theme.label(user.fullName() + (user.isAdmin() ? "  (Admin)" : ""), 13, true, Theme.TEXT);
        Widgets.Btn out = Theme.secondary("Log out");
        out.addActionListener(e -> logout());
        right.add(who);
        right.add(out);

        nav.add(leftSide, BorderLayout.CENTER);
        nav.add(right, BorderLayout.EAST);
        return nav;
    }

    void navigate(String key) {
        Page p = pages.get(key);
        if (p == null) return;
        links.get(key).setSelected(true);
        pageCards.show(pageHost, key);
        p.onShow();
    }

    void logout() {
        pages.clear();
        loginPage.reset();
        rootCards.show(root, "auth");
        authCards.show(authHost, "login");
    }

    // ---------- cross-page shortcuts ----------
    void openBooking(int trainNumber) {
        navigate("book");
        ((BookingPage) pages.get("book")).preselect(trainNumber);
    }

    void openManage(String pnr) {
        navigate("manage");
        ((ManagePage) pages.get("manage")).lookup(pnr);
    }
}
