package bank.controller;

import bank.exception.BankException;
import bank.model.Account;
import bank.model.Transaction;
import bank.view.BaseView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** What the Deposit, Withdraw and History controllers share: account lookup, history filter, error handling. */
final class TransactionSupport {

    private TransactionSupport() {
    }

    /** Finds the account (number is upper-cased) and checks that this user may use it. */
    static Account lookUp(Session session, String accountNumber) {
        Account account = session.getBank().findAccount(
                Rows.required(accountNumber, "an account number").toUpperCase(Locale.ROOT));
        session.requireAccess(account);
        return account;
    }

    /** The account's transactions between the dates (either may be null), newest first (FR-22, FR-23). */
    static List<Transaction> history(Account account, LocalDate from, LocalDate to) {
        List<Transaction> shown = new ArrayList<>();
        for (Transaction t : account.getTransactions()) {
            LocalDate day = t.getTime().toLocalDate();
            if ((from == null || !day.isBefore(from)) && (to == null || !day.isAfter(to))) {
                shown.add(t);
            }
        }
        Collections.reverse(shown);
        return shown;
    }

    /** Runs the action; a business-rule failure becomes a red message on the screen. */
    static void run(BaseView view, Runnable action) {
        try {
            action.run();
        } catch (BankException e) {
            view.showError(e.getMessage());
        }
    }
}
