package bank.controller;

import bank.exception.BankException;
import bank.model.Account;
import bank.model.Transaction;
import bank.view.HistoryView;

import java.time.LocalDate;
import java.util.List;

/** Handles Search Account, View Transaction History and Filter By Date for customers and tellers. */
public class HistoryController implements HistoryView.Listener {

    private final HistoryView view;
    private final Session session;

    public HistoryController(HistoryView view, Session session) {
        this.view = view;
        this.session = session;
    }

    @Override
    public void onFindAccount(String accountNumber) {
        TransactionSupport.run(view, () -> {
            Account account = TransactionSupport.lookUp(session, accountNumber);
            List<Transaction> all = TransactionSupport.history(account, null, null);
            view.showAccount(Rows.account(account));
            view.showHistory(all.stream().map(Rows::transaction).toList());
            view.showMessage(all.size() + " transaction(s), newest first.");
        });
    }

    @Override
    public void onShowHistory(String accountNumber, LocalDate from, LocalDate to) {
        TransactionSupport.run(view, () -> {
            Account account = TransactionSupport.lookUp(session, accountNumber);
            if (from != null && to != null && from.isAfter(to)) {
                throw new BankException("The From date must not be after the To date.");
            }
            List<Transaction> shown = TransactionSupport.history(account, from, to);
            view.showHistory(shown.stream().map(Rows::transaction).toList());
            view.showMessage(shown.size() + " transaction(s) shown, newest first.");
        });
    }
}
