package bank.dao;

import bank.exception.DataAccessException;
import bank.model.Account;
import bank.model.ChequeAccount;
import bank.model.Customer;
import bank.model.InvestmentAccount;
import bank.model.SavingsAccount;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.function.Function;

/** MySQL storage of accounts. The right subclass is rebuilt from the account_type column. */
public class JdbcAccountDAO implements AccountDAO {

    private final Database db;
    private final TransactionDAO transactions;
    private Function<String, Customer> holders;

    public JdbcAccountDAO(Database db, TransactionDAO transactions) {
        this.db = db;
        this.transactions = transactions;
    }

    @Override
    public void setHolderLookup(Function<String, Customer> holders) {
        this.holders = holders;
    }

    @Override
    public void save(Account a) {
        String employer = null;
        String employerAddress = null;
        if (a instanceof ChequeAccount cheque) {
            employer = cheque.getEmployerName();
            employerAddress = cheque.getEmployerAddress();
        }
        db.update("INSERT INTO accounts (account_number, account_type, customer_id, branch, date_opened,"
                        + " balance, last_interest_month, employer_name, employer_address)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                a.getAccountNumber(), a.getAccountType(), a.getHolder().getCustomerId(), a.getBranch(),
                a.getDateOpened(), a.getBalance(), month(a), employer, employerAddress);
    }

    @Override
    public Account findById(String id) {
        return db.queryOne("SELECT * FROM accounts WHERE account_number = ?", this::map, id);
    }

    @Override
    public List<Account> findAll() {
        return db.query("SELECT * FROM accounts ORDER BY account_number", this::map);
    }

    @Override
    public List<Account> findByCustomer(String customerId) {
        return db.query("SELECT * FROM accounts WHERE customer_id = ? ORDER BY account_number",
                this::map, customerId);
    }

    @Override
    public void update(Account a) {
        db.update("UPDATE accounts SET balance = ?, last_interest_month = ? WHERE account_number = ?",
                a.getBalance(), month(a), a.getAccountNumber());
    }

    @Override
    public void delete(String id) {
        db.update("DELETE FROM accounts WHERE account_number = ?", id);
    }

    private static String month(Account a) {
        return a.getLastInterestMonth() == null ? null : a.getLastInterestMonth().toString();
    }

    private Account map(ResultSet r) throws SQLException {
        String number = r.getString("account_number");
        Customer holder = holders.apply(r.getString("customer_id"));
        String branch = r.getString("branch");
        LocalDate opened = r.getObject("date_opened", LocalDate.class);
        Account account = switch (r.getString("account_type")) {
            case "Savings" -> new SavingsAccount(number, holder, branch, opened);
            case "Investment" -> new InvestmentAccount(number, holder, branch, opened);
            case "Cheque" -> new ChequeAccount(number, holder, branch, opened,
                    r.getString("employer_name"), r.getString("employer_address"));
            default -> throw new DataAccessException("Unknown account type for " + number + ".");
        };
        String last = r.getString("last_interest_month");
        account.restoreHistory(transactions.findByAccount(number), last == null ? null : YearMonth.parse(last));
        return account;
    }
}
