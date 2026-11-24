package bank.controller;

import bank.exception.BankException;
import bank.model.Account;
import bank.model.Customer;
import bank.model.Transaction;
import bank.view.ViewData;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Turns model objects into the plain text the views display, and parses what the user typed. */
final class Rows {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Rows() {
    }

    static ViewData.AccountRow account(Account account) {
        return new ViewData.AccountRow(account.getAccountNumber(), account.getAccountType(),
                account.getBalance().toPlainString());
    }

    static ViewData.TransactionRow transaction(Transaction t) {
        String name = t.getType().name();
        String type = name.charAt(0) + name.substring(1).toLowerCase(Locale.ROOT);
        return new ViewData.TransactionRow(t.getTime().format(TIME), type, t.getAmount().toPlainString(),
                t.getBalanceAfter().toPlainString(), t.getReference() == null ? "" : t.getReference());
    }

    static ViewData.CustomerRow customer(Customer c) {
        return new ViewData.CustomerRow(c.getCustomerId(), c.getFullName(), nullToEmpty(c.getAddress()),
                nullToEmpty(c.getPhone()), nullToEmpty(c.getEmail()));
    }

    /** Parses an amount typed by the user, e.g. "250.00" or "1,250.50". */
    static BigDecimal amount(String text) {
        if (text == null || text.isBlank()) {
            throw new BankException("Enter an amount.");
        }
        try {
            return new BigDecimal(text.trim().replace(",", ""));
        } catch (NumberFormatException e) {
            throw new BankException("The amount must be a number such as 250.00.");
        }
    }

    static String required(String text, String what) {
        if (text == null || text.isBlank()) {
            throw new BankException("Enter " + what + ".");
        }
        return text.trim();
    }

    private static String nullToEmpty(String text) {
        return text == null ? "" : text;
    }
}
