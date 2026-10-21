package com.reservation.ui;

import com.reservation.service.AuthenticationService;
import com.reservation.service.ServiceException;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Arrays;
import javax.swing.Box;
import javax.swing.JPanel;

final class RegisterPage extends JPanel {
    private final Widgets.Field fullName = new Widgets.Field("e.g. Asha Verma", 22);
    private final Widgets.Field username = new Widgets.Field("Letters, digits, underscore", 22);
    private final Widgets.PassField password = new Widgets.PassField("At least 8 characters, letter + digit", 22);
    private final Widgets.PassField confirm = new Widgets.PassField("Repeat your password", 22);
    private final Widgets.Banner banner = new Widgets.Banner();
    private final AuthenticationService auth = new AuthenticationService();
    private final AppFrame app;

    RegisterPage(AppFrame app) {
        super(new java.awt.BorderLayout());
        this.app = app;

        JPanel f = Theme.vbox();
        f.add(Theme.left((javax.swing.JComponent) Box.createRigidArea(new Dimension(360, 0))));
        f.add(Theme.left(Theme.h1("Create your account")));
        f.add(Theme.left(Theme.muted("It takes less than a minute.")));
        f.add(Box.createVerticalStrut(16));
        f.add(banner);
        f.add(Box.createVerticalStrut(4));
        f.add(Theme.labeled("Full name", fullName));
        f.add(Box.createVerticalStrut(12));
        f.add(Theme.labeled("Username", username));
        f.add(Box.createVerticalStrut(12));
        f.add(Theme.labeled("Password", password));
        f.add(Box.createVerticalStrut(12));
        f.add(Theme.labeled("Confirm password", confirm));
        f.add(Box.createVerticalStrut(18));

        Widgets.Btn create = Theme.primary("Create account");
        create.addActionListener(e -> register());
        confirm.addActionListener(e -> register());
        f.add(Theme.fullWidth(create));
        f.add(Box.createVerticalStrut(14));

        JPanel foot = Theme.panel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        foot.add(Theme.muted("Already registered?"));
        var back = Theme.linkButton("Sign in");
        back.addActionListener(e -> app.showLogin(null));
        foot.add(back);
        f.add(Theme.left(foot));

        Widgets.Card card = new Widgets.Card(30);
        card.add(f);
        add(AuthShell.wrap(card));
    }

    void reset() {
        fullName.setText("");
        username.setText("");
        password.setText("");
        confirm.setText("");
        banner.clear();
    }

    private void register() {
        char[] pw = password.getPassword();
        char[] cf = confirm.getPassword();
        try {
            auth.register(fullName.getText(), username.getText(), pw, cf);
            reset();
            app.showLogin("Account created - please sign in.");
        } catch (ServiceException ex) {
            banner.show(Widgets.Tone.ERROR, ex.getMessage());
        } finally {
            Arrays.fill(pw, ' ');
            Arrays.fill(cf, ' ');
        }
    }
}
