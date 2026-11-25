package bank.controller;

import bank.dao.Database;
import bank.dao.InMemoryUserDAO;
import bank.dao.JdbcAccountDAO;
import bank.dao.JdbcCustomerDAO;
import bank.dao.JdbcTransactionDAO;
import bank.dao.JdbcUserDAO;
import bank.dao.PersistentBank;
import bank.dao.TransactionDAO;
import bank.dao.UserDAO;
import bank.exception.DataAccessException;
import bank.model.Bank;
import bank.model.Withdrawable;
import bank.view.AccountView;
import bank.view.CustomerView;
import bank.view.InterestView;
import bank.view.LoginView;
import bank.view.Screen;
import bank.view.DepositView;
import bank.view.HistoryView;
import bank.view.WithdrawView;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Builds the bank and connects each screen to its controller. The JavaFX application class
 * talks only to this class, so the screens never touch the model or the data store.
 */
public class AppController {

    private static final String BRANCH = "Gaborone Main Branch";

    private final UserDAO users;
    private final Session session;
    private final String storageNote;

    /**
     * Connects to MySQL and loads the bank from it. If the database cannot be reached the program
     * still starts, with sample data kept in memory, and the login screen says so.
     */
    public AppController() {
        Bank bank;
        UserDAO userDao;
        String note;
        try {
            Database db = Database.fromConfig();
            db.connect();
            TransactionDAO transactions = new JdbcTransactionDAO(db);
            PersistentBank persistent = new PersistentBank(BRANCH, db, new JdbcCustomerDAO(db),
                    new JdbcAccountDAO(db, transactions), transactions);
            userDao = new JdbcUserDAO(db, persistent::findCustomer);
            if (userDao.findAll().isEmpty()) {
                DemoData.load(persistent, userDao);
            }
            bank = persistent;
            note = "Connected to the MySQL database.";
        } catch (DataAccessException e) {
            bank = new Bank(BRANCH);
            userDao = new InMemoryUserDAO();
            DemoData.load(bank, userDao);
            note = "MySQL not available, so sample data is kept in memory only. (" + e.getMessage() + ")";
        }
        this.users = userDao;
        this.session = new Session(bank);
        this.storageNote = note;
    }

    public void bindLogin(LoginView view, Runnable onSuccess) {
        view.setListener(new LoginController(view, users, session, onSuccess));
        view.showMessage(storageNote);
    }

    public void bindCustomers(CustomerView view) {
        CustomerController controller = new CustomerController(view, session);
        view.setListener(controller);
        controller.start();
    }

    public void bindAccounts(AccountView view) {
        view.setOwnAccountsOnly(session.isCustomer());
        AccountController controller = new AccountController(view, session);
        view.setListener(controller);
        controller.start();
    }

    public void bindDeposit(DepositView view) {
        view.setListener(new DepositController(view, session));
        view.setAccountChoices(choices(false), "You have no accounts yet.");
    }

    public void bindWithdraw(WithdrawView view) {
        view.setListener(new WithdrawController(view, session));
        view.setAccountChoices(choices(true),
                "None of your accounts allows withdrawals (Savings accounts are deposit-only).");
    }

    public void bindHistory(HistoryView view) {
        view.setListener(new HistoryController(view, session));
        if (session.isCustomer()) {
            view.setAccountChoices(choices(false), "You have no accounts yet.");
        }
    }

    /** The logged-in customer's accounts as "ACC0000001 (Savings)", optionally only those that allow withdrawals. */
    private List<String> choices(boolean withdrawableOnly) {
        if (!session.isCustomer()) {
            return List.of();
        }
        return session.getCustomer().getAccounts().stream()
                .filter(account -> !withdrawableOnly || account instanceof Withdrawable)
                .map(account -> account.getAccountNumber() + " (" + account.getAccountType() + ")")
                .toList();
    }

    public void bindInterest(InterestView view) {
        view.setListener(new InterestController(view, session));
    }

    /** The screens the logged-in user may open (FR-07): tellers four, customers two. */
    public Set<Screen> allowedScreens() {
        if (session.isTeller()) {
            return EnumSet.of(Screen.CUSTOMERS, Screen.ACCOUNTS, Screen.HISTORY, Screen.INTEREST);
        }
        if (session.isCustomer()) {
            return EnumSet.of(Screen.ACCOUNTS, Screen.DEPOSIT, Screen.WITHDRAW, Screen.HISTORY);
        }
        return EnumSet.noneOf(Screen.class);
    }

    /** A few headline numbers for the main menu, in the order they are shown. */
    public java.util.Map<String, String> summary() {
        java.util.Map<String, String> figures = new java.util.LinkedHashMap<>();
        if (session.isTeller()) {
            java.math.BigDecimal total = java.math.BigDecimal.ZERO;
            for (var account : session.getBank().getAccounts()) {
                total = total.add(account.getBalance());
            }
            figures.put("Customers", String.valueOf(session.getBank().getCustomers().size()));
            figures.put("Accounts", String.valueOf(session.getBank().getAccounts().size()));
            figures.put("Total held (BWP)", total.toPlainString());
        } else if (session.isCustomer()) {
            java.math.BigDecimal total = java.math.BigDecimal.ZERO;
            for (var account : session.getCustomer().getAccounts()) {
                total = total.add(account.getBalance());
            }
            figures.put("Total balance (BWP)", total.toPlainString());
        }
        return figures;
    }

    public String dashboardTitle() {
        return session.isTeller() ? "Teller Dashboard" : "Customer Dashboard";
    }

    /** The logged-in customer's accounts, for the cards on the dashboard (none for a teller). */
    public java.util.List<bank.view.ViewData.AccountRow> accountRows() {
        if (!session.isCustomer()) {
            return java.util.List.of();
        }
        return session.getCustomer().getAccounts().stream().map(Rows::account).toList();
    }

    /** The customer's five newest transactions across all their accounts (none for a teller). */
    public java.util.List<bank.view.ViewData.TransactionRow> recentTransactions() {
        if (!session.isCustomer()) {
            return java.util.List.of();
        }
        return session.getCustomer().getAccounts().stream()
                .flatMap(account -> account.getTransactions().stream())
                .sorted(java.util.Comparator.comparing(bank.model.Transaction::getTime).reversed())
                .limit(5)
                .map(Rows::transaction)
                .toList();
    }

    public String welcomeText() {
        if (session.getUser() == null) {
            return "";
        }
        return session.isCustomer()
                ? "Welcome, " + session.getCustomer().getFullName()
                : "Teller: " + session.getUser().getUsername();
    }

    public void logOut() {
        session.logOut();
    }
}
