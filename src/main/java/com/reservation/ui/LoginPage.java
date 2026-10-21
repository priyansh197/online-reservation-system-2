package com.reservation.ui;

import com.reservation.model.User;
import com.reservation.service.AuthenticationService;
import com.reservation.service.ServiceException;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Arrays;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

final class LoginPage extends JPanel {
    private final Widgets.Field username = new Widgets.Field("Enter your username", 22);
    private final Widgets.PassField password = new Widgets.PassField("Enter your password", 22);
    private final Widgets.Banner banner = new Widgets.Banner();
    private final AuthenticationService auth = new AuthenticationService();
    private final AppFrame app;

    LoginPage(AppFrame app) {
        super(new java.awt.BorderLayout());
        this.app = app;

        JPanel f = Theme.vbox();
        f.add(Theme.left((javax.swing.JComponent) Box.createRigidArea(new Dimension(360, 0))));
        f.add(Theme.left(Theme.h1("Welcome back")));
        f.add(Theme.left(Theme.muted("Sign in to book and manage your train tickets.")));
        f.add(Box.createVerticalStrut(18));
        f.add(banner);
        f.add(Box.createVerticalStrut(6));
        f.add(Theme.labeled("Username", username));
        f.add(Box.createVerticalStrut(14));
        f.add(Theme.labeled("Password", password));
        f.add(Box.createVerticalStrut(8));

        JCheckBox show = new JCheckBox("Show password");
        show.setOpaque(false);
        show.setFocusPainted(false);
        char echo = password.getEchoChar();
        show.addActionListener(e -> password.setEchoChar(show.isSelected() ? (char) 0 : echo));
        f.add(Theme.left(show));
        f.add(Box.createVerticalStrut(16));

        Widgets.Btn login = Theme.primary("Sign in");
        login.addActionListener(e -> doLogin());
        f.add(Theme.fullWidth(login));
        f.add(Box.createVerticalStrut(16));

        JPanel foot = Theme.panel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        foot.add(Theme.muted("New to RailEase?"));
        var create = Theme.linkButton("Create an account");
        create.addActionListener(e -> app.showRegister());
        foot.add(create);
        f.add(Theme.left(foot));

        username.addActionListener(e -> password.requestFocusInWindow());
        password.addActionListener(e -> doLogin());

        Widgets.Card card = new Widgets.Card(34);
        card.add(f);
        add(AuthShell.wrap(card));
    }

    void showSuccess(String msg) { banner.show(Widgets.Tone.SUCCESS, msg); }

    void reset() {
        username.setText("");
        password.setText("");
        banner.clear();
    }

    private void doLogin() {
        char[] pw = password.getPassword();
        try {
            User u = auth.login(username.getText(), pw);
            reset();
            app.startSession(u);
        } catch (ServiceException ex) {
            banner.show(Widgets.Tone.ERROR, ex.getMessage());
            password.setText("");
        } finally {
            Arrays.fill(pw, ' ');
        }
    }
}
