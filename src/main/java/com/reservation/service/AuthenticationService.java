package com.reservation.service;

import com.reservation.dao.UserDAO;
import com.reservation.model.User;
import com.reservation.util.PasswordUtil;
import com.reservation.util.ValidationUtil;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Arrays;
import java.util.Optional;

public class AuthenticationService {
    private static String dummyHash;
    private final UserDAO userDao = new UserDAO();

    private static synchronized String dummy() {
        if (dummyHash == null) dummyHash = PasswordUtil.hash("not-a-real-password1".toCharArray());
        return dummyHash;
    }

    public User login(String username, char[] password) throws ServiceException {
        String u = ValidationUtil.clean(username);
        if (u.isEmpty() || password == null || password.length == 0)
            throw new ServiceException("Username and password are required.");
        try {
            Optional<UserDAO.Credentials> cred = userDao.findCredentials(u);
            // Always run a hash verification so timing does not reveal whether the username exists.
            boolean ok = PasswordUtil.verify(password, cred.map(UserDAO.Credentials::passwordHash).orElse(dummy()));
            if (cred.isEmpty() || !ok) throw new ServiceException("Invalid username or password.");
            return cred.get().user();
        } catch (SQLException e) {
            throw ServiceException.database(e);
        }
    }

    /** Self-registration: the role is always USER. */
    public void register(String fullName, String username, char[] password, char[] confirm) throws ServiceException {
        create(fullName, username, password, confirm, "USER");
    }

    /** Used only by the command-line tool (com.reservation.tools.CreateAdmin), never by the GUI. */
    public void createAdmin(String fullName, String username, char[] password) throws ServiceException {
        create(fullName, username, password, password, "ADMIN");
    }

    private void create(String fullName, String username, char[] pw, char[] confirm, String role)
            throws ServiceException {
        String name = ValidationUtil.validatePersonName(fullName, "Full name");
        String user = ValidationUtil.validateUsername(username);
        ValidationUtil.validatePassword(pw);
        if (!Arrays.equals(pw, confirm)) throw new ServiceException("Password and confirmation do not match.");
        try {
            userDao.insert(name, user, PasswordUtil.hash(pw), role);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new ServiceException("That username is already taken.");
        } catch (SQLException e) {
            throw ServiceException.database(e);
        }
    }

    /** Server-side authorization check; the GUI hiding buttons is not a security control. */
    public static void requireAdmin(User actor) throws ServiceException {
        if (actor == null || !actor.isAdmin())
            throw new ServiceException("Access denied: administrator privileges are required.");
    }
}
