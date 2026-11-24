package bank.controller;

import bank.exception.AccessDeniedException;
import bank.model.Account;
import bank.model.Bank;
import bank.model.Customer;
import bank.model.Role;
import bank.model.User;

/** Who is logged in, and the role checks every controller runs before it acts (NFR-01). */
public class Session {

    private final Bank bank;
    private User user;

    public Session(Bank bank) {
        this.bank = bank;
    }

    public Bank getBank() {
        return bank;
    }

    public void logIn(User user) {
        this.user = user;
    }

    public void logOut() {
        if (user != null) {
            user.logout();
        }
        user = null;
    }

    public User getUser() {
        return user;
    }

    public boolean isTeller() {
        return user != null && user.getRole() == Role.TELLER;
    }

    public boolean isCustomer() {
        return user != null && user.getRole() == Role.CUSTOMER;
    }

    /** The customer a customer login belongs to; null for tellers. */
    public Customer getCustomer() {
        return user == null ? null : user.getCustomer();
    }

    public void requireTeller() {
        if (!isTeller()) {
            throw new AccessDeniedException("Only a bank teller can do this.");
        }
    }

    public void requireCustomer() {
        if (!isCustomer()) {
            throw new AccessDeniedException("Only a customer can do this.");
        }
    }

    /** A customer may only use their own accounts; a teller may view any account. */
    public void requireAccess(Account account) {
        if (user == null) {
            throw new AccessDeniedException("Please log in first.");
        }
        if (isCustomer() && account.getHolder() != getCustomer()) {
            throw new AccessDeniedException("You can only use your own accounts.");
        }
    }
}
