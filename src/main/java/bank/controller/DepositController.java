package bank.controller;

import bank.model.Account;
import bank.view.DepositView;

import java.math.BigDecimal;

/** Handles Deposit. Only a customer deposits, and only into their own accounts. */
public class DepositController implements DepositView.Listener {

    private final DepositView view;
    private final Session session;

    public DepositController(DepositView view, Session session) {
        this.view = view;
        this.session = session;
    }

    @Override
    public void onFindAccount(String accountNumber) {
        TransactionSupport.run(view, () -> {
            session.requireCustomer();
            Account account = TransactionSupport.lookUp(session, accountNumber);
            view.showAccount(Rows.account(account));
            view.showMessage("Account selected.");
        });
    }

    @Override
    public void onDeposit(String accountNumber, String amount) {
        TransactionSupport.run(view, () -> {
            session.requireCustomer();
            Account account = TransactionSupport.lookUp(session, accountNumber);
            BigDecimal value = Rows.amount(amount);
            session.getBank().deposit(account.getAccountNumber(), value);
            view.showAccount(Rows.account(account));
            view.clearAmount();
            view.showMessage("Deposited BWP" + Account.money(value).toPlainString()
                    + ". New balance: BWP" + account.getBalance().toPlainString() + ".");
        });
    }
}
