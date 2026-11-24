package bank.dao;

import bank.exception.DataAccessException;
import bank.model.Account;
import bank.model.Bank;
import bank.model.Customer;
import bank.model.IdGenerator;
import bank.model.InterestBearing;
import bank.model.Transaction;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * A Bank that also saves every change to the database (overriding). All business rules stay in
 * Bank and the account classes; this class only adds the saving. Each change is saved in one
 * database transaction, and if saving fails the in-memory change is undone and an error is shown.
 */
public class PersistentBank extends Bank {

    private final Database db;
    private final CustomerDAO customerDao;
    private final AccountDAO accountDao;
    private final TransactionDAO transactionDao;

    public PersistentBank(String branch, Database db, CustomerDAO customerDao,
                          AccountDAO accountDao, TransactionDAO transactionDao) {
        super(branch);
        this.db = db;
        this.customerDao = customerDao;
        this.accountDao = accountDao;
        this.transactionDao = transactionDao;
        accountDao.setHolderLookup(this::findCustomer);
        load();
    }

    /** Reads every customer and account back from the database and continues the ID numbering. */
    private void load() {
        long lastCustomer = 0;
        long lastAccount = 0;
        for (Customer customer : customerDao.findAll()) {
            super.registerCustomer(customer);
            lastCustomer = Math.max(lastCustomer, number(customer.getCustomerId()));
        }
        for (Account account : accountDao.findAll()) {
            restoreAccount(account);
            lastAccount = Math.max(lastAccount, number(account.getAccountNumber()));
        }
        IdGenerator.resetTo(lastCustomer, lastAccount);
    }

    private static long number(String id) {
        return Long.parseLong(id.replaceAll("\\D", ""));
    }

    @Override
    public Customer registerCustomer(Customer customer) {
        customerDao.save(customer);
        return super.registerCustomer(customer);
    }

    @Override
    public Customer updateCustomer(String customerId, String address, String phone, String email) {
        Customer customer = findCustomer(customerId);
        String oldAddress = customer.getAddress();
        String oldPhone = customer.getPhone();
        String oldEmail = customer.getEmail();
        super.updateCustomer(customerId, address, phone, email);
        try {
            customerDao.update(customer);
        } catch (DataAccessException e) {
            customer.updateContact(oldAddress, oldPhone, oldEmail);
            throw e;
        }
        return customer;
    }

    /** A new account and its opening transaction are saved before the account is added to the bank. */
    @Override
    protected <T extends Account> T add(Customer holder, T account) {
        db.inTransaction(() -> {
            accountDao.save(account);
            for (Transaction transaction : account.getTransactions()) {
                transactionDao.save(transaction);
            }
        });
        return super.add(holder, account);
    }

    @Override
    public void deposit(String accountNumber, BigDecimal amount) {
        changeAndSave(findAccount(accountNumber), () -> super.deposit(accountNumber, amount));
    }

    @Override
    public void withdraw(String accountNumber, BigDecimal amount) {
        changeAndSave(findAccount(accountNumber), () -> super.withdraw(accountNumber, amount));
    }

    @Override
    public List<Account> payMonthlyInterest(YearMonth month) {
        List<Account> paid = new ArrayList<>();
        for (Account account : getAccounts()) {
            if (account instanceof InterestBearing payer && !month.equals(account.getLastInterestMonth())) {
                changeAndSave(account, () -> payer.payInterest(month));
                paid.add(account);
            }
        }
        return paid;
    }

    /** Runs a change on the account, then saves the new transactions and balance together. */
    private void changeAndSave(Account account, Runnable change) {
        List<Transaction> before = new ArrayList<>(account.getTransactions());
        YearMonth lastInterest = account.getLastInterestMonth();
        change.run();
        try {
            db.inTransaction(() -> {
                List<Transaction> now = account.getTransactions();
                for (Transaction transaction : now.subList(before.size(), now.size())) {
                    transactionDao.save(transaction);
                }
                accountDao.update(account);
            });
        } catch (DataAccessException e) {
            account.restoreHistory(before, lastInterest);
            throw e;
        }
    }
}
