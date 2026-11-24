package bank.model;

import bank.security.PasswordHasher;

/** A login for a teller or a customer (use cases: Log In, Log Out). */
public class User {

    private final String username;
    private final String passwordHash;
    private final Role role;
    private Customer customer;
    private boolean loggedIn;

    /** Creates a new user; the password is hashed straight away and never stored in plain text. */
    public User(String username, String plainPassword, Role role) {
        this(username, role, PasswordHasher.hash(plainPassword));
    }

    private User(String username, Role role, String storedHash) {
        this.username = username;
        this.role = role;
        this.passwordHash = storedHash;
    }

    /** Rebuilds a user from the database, where only the hash is kept. */
    public static User fromStored(String username, String storedHash, Role role) {
        return new User(username, role, storedHash);
    }

    /** Returns true and marks the user as logged in when the password is right. */
    public boolean login(String password) {
        if (PasswordHasher.verify(password, passwordHash)) {
            loggedIn = true;
            return true;
        }
        return false;
    }

    public void logout() {
        loggedIn = false;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public Role getRole() {
        return role;
    }

    /** The customer this login belongs to; null for tellers. */
    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
