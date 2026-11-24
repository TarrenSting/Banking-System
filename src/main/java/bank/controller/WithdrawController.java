package bank.controller;

import bank.model.Account;
import bank.view.WithdrawView;

import java.math.BigDecimal;

/** Handles Withdraw Funds. The model rejects an account that does not allow withdrawals or has too little money (Reject Withdrawal). */
public class WithdrawController implements WithdrawView.Listener {

    private final WithdrawView view;
    private final Session session;

    public WithdrawController(WithdrawView view, Session session) {
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
    public void onWithdraw(String accountNumber, String amount) {
        TransactionSupport.run(view, () -> {
            session.requireCustomer();
            Account account = TransactionSupport.lookUp(session, accountNumber);
            BigDecimal value = Rows.amount(amount);
            session.getBank().withdraw(account.getAccountNumber(), value);
            view.showAccount(Rows.account(account));
            view.clearAmount();
            view.showMessage("Withdrew BWP" + Account.money(value).toPlainString()
                    + ". New balance: BWP" + account.getBalance().toPlainString() + ".");
        });
    }
}
