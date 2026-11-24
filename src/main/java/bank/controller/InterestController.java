package bank.controller;

import bank.exception.BankException;
import bank.model.Account;
import bank.view.InterestView;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

/** Handles Pay Monthly Interest: one action pays every eligible account once for the month. */
public class InterestController implements InterestView.Listener {

    private final InterestView view;
    private final Session session;

    public InterestController(InterestView view, Session session) {
        this.view = view;
        this.session = session;
    }

    @Override
    public void onRunInterest(String month) {
        try {
            session.requireTeller();
            YearMonth yearMonth = parse(month);
            List<Account> paid = session.getBank().payMonthlyInterest(yearMonth);
            String summary = paid.isEmpty()
                    ? "No accounts to pay for " + yearMonth + ": every eligible account is already paid."
                    : "Paid interest to " + paid.size() + " account(s) for " + yearMonth + ".";
            view.showResults(paid.stream().map(Rows::account).toList(), summary);
            view.showMessage(summary);
        } catch (BankException e) {
            view.showError(e.getMessage());
        }
    }

    private static YearMonth parse(String month) {
        try {
            return YearMonth.parse(Rows.required(month, "the month as yyyy-MM, for example 2026-11"));
        } catch (DateTimeParseException e) {
            throw new BankException("Enter the month as yyyy-MM, for example 2026-11.");
        }
    }
}
