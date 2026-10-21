package com.reservation.tools;

import com.reservation.service.AuthenticationService;
import com.reservation.service.ServiceException;
import java.io.Console;
import java.util.Arrays;
import java.util.Scanner;

/**
 * One-off command-line tool to create an administrator account.
 * Usage: java -cp target/online-reservation-system-1.0.0.jar com.reservation.tools.CreateAdmin <username> "<Full Name>"
 * The password is typed at a prompt, so it never appears in source code or shell history.
 */
public final class CreateAdmin {
    private CreateAdmin() { }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: CreateAdmin <username> \"<Full Name>\"");
            System.exit(2);
        }
        char[] pw = readPassword("Admin password: ");
        try {
            new AuthenticationService().createAdmin(args[1], args[0], pw);
            System.out.println("Administrator '" + args[0] + "' created.");
        } catch (ServiceException e) {
            System.err.println("Failed: " + e.getMessage());
            System.exit(1);
        } finally {
            Arrays.fill(pw, ' ');
        }
    }

    private static char[] readPassword(String prompt) {
        Console console = System.console();
        if (console != null) return console.readPassword(prompt);
        System.out.print(prompt + "(input will be visible) ");
        return new Scanner(System.in).nextLine().toCharArray();
    }
}
