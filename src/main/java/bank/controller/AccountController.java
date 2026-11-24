package bank.controller;

import bank.exception.BankException;
import bank.model.Account;
import bank.model.Bank;
import bank.model.Customer;
import bank.view.AccountView;
import bank.view.ViewData;

import java.util.List;

/**
 * Handles View Accounts and Balance (teller and customer) and Open Account (teller only).
 * A customer always sees their own accounts, whatever ID is typed.
 */
public class AccountController implements AccountView.Listener {

    private final AccountView view;
    private final Session session;

    public AccountController(AccountView view, Session session) {
        this.view = view;
        this.session = session;
    }

    /** A customer's own accounts are shown as soon as the screen opens. */
    public void start() {
        if (session.isCustomer()) {
            onLoadAccounts("");
        }
    }

    @Override
    public void onLoadAccounts(String customerId) {
        run(() -> {
            Customer customer = customerFor(customerId);
            List<Account> accounts = customer.getAccounts();
            view.showAccounts(accounts.stream().map(Rows::account).toList());
            view.showMessage(customer.getFullName() + " has " + accounts.size() + " account(s).");
        });
    }

    @Override
    public void onOpenAccount(ViewData.AccountRequest request) {
        run(() -> {
            session.requireTeller();
            String id = Rows.required(request.customerId(), "a customer ID");
            Bank bank = session.getBank();
            Account account = switch (request.kind()) {
                case SAVINGS -> bank.openSavings(id, Rows.amount(request.initialDeposit()));
                case INVESTMENT -> bank.openInvestment(id, Rows.amount(request.initialDeposit()));
                case CHEQUE -> bank.openCheque(id, request.employerName(), request.employerAddress());
            };
            view.clearForm();
            view.showAccounts(bank.accountsOf(id).stream().map(Rows::account).toList());
            view.showMessage("Opened " + account.getAccountType() + " account " + account.getAccountNumber()
                    + " for " + account.getHolder().getFullName() + ".");
        });
    }

    private Customer customerFor(String customerId) {
        if (session.isCustomer()) {
            return session.getCustomer();
        }
        session.requireTeller();
        return session.getBank().findCustomer(Rows.required(customerId, "a customer ID"));
    }

    private void run(Runnable action) {
        try {
            action.run();
        } catch (BankException e) {
            view.showError(e.getMessage());
        }
    }
}
