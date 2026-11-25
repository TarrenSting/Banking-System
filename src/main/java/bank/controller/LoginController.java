package bank.controller;

import bank.dao.UserDAO;
import bank.model.Role;
import bank.model.User;
import bank.view.LoginView;

/** Handles Log In: checks the username and password and tells the app when the user is in. */
public class LoginController implements LoginView.Listener {

    private final LoginView view;
    private final UserDAO users;
    private final Session session;
    private final Runnable onSuccess;

    public LoginController(LoginView view, UserDAO users, Session session, Runnable onSuccess) {
        this.view = view;
        this.users = users;
        this.session = session;
        this.onSuccess = onSuccess;
    }

    @Override
    public void onLogin(String username, String password, boolean asTeller) {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            view.showError("Enter your username and password.");
            view.clearPassword();
            return;
        }
        User user = users.findByUsername(username);
        // The same message for an unknown username and a wrong password (NFR-01, FR-06).
        if (user == null || !user.login(password)) {
            view.showError("Invalid username or password.");
            view.clearPassword();
            return;
        }
        if (asTeller != (user.getRole() == Role.TELLER)) {
            user.logout();
            view.showError(asTeller ? "This is a customer account. Use the Customer login."
                    : "This is a staff account. Use the Teller login.");
            view.clearPassword();
            return;
        }
        session.logIn(user);
        onSuccess.run();
    }
}
