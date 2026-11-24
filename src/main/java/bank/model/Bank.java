package bank.model;

import bank.exception.AccountRuleException;
import bank.exception.BankException;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * In-memory registry that ties the domain classes together and carries out the use cases of
 * the use case diagram: register customer, open account, search account, deposit, withdraw and
 * pay monthly interest. It has no GUI and no database code. In Part B the controllers will call
 * it, and the DAOs will take over storage from the two maps.
 */
public class Bank {

    private final String branch;
    private final Map<String, Customer> customers = new LinkedHashMap<>();
    private final Map<String, Account> accounts = new LinkedHashMap<>();

    public Bank(String branch) {
        if (branch == null || branch.isBlank()) {
            throw new BankException("A branch is required.");
        }
        this.branch = branch;
    }

    public String getBranch() {
        return branch;
    }

    // ---- Register Customer, Update Customer Details, Search ----

    public Customer registerCustomer(Customer customer) {
        if (customers.containsKey(customer.getCustomerId())) {
            throw new BankException("Customer " + customer.getCustomerId() + " is already registered.");
        }
        customers.put(customer.getCustomerId(), customer);
        return customer;
    }

    /** Updates a customer's address, phone and email (Update Customer Details, FR-03). */
    public Customer updateCustomer(String customerId, String address, String phone, String email) {
        Customer customer = findCustomer(customerId);
        customer.updateContact(address, phone, email);
        return customer;
    }

    public Customer findCustomer(String customerId) {
        Customer customer = customers.get(customerId);
        if (customer == null) {
            throw new BankException("No customer with ID " + customerId + ".");
        }
        return customer;
    }

    /** Finds customers whose surname or company name starts with the text, ignoring case. */
    public List<Customer> searchCustomers(String text) {
        String needle = text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
        List<Customer> found = new ArrayList<>();
        for (Customer customer : customers.values()) {
            String name = customer.isCompany() ? customer.getCompanyName() : customer.getSurname();
            if (name.toLowerCase(Locale.ROOT).startsWith(needle)) {
                found.add(customer);
            }
        }
        return found;
    }

    // ---- Open Account (generalised into Savings, Investment and Cheque) ----

    /** Opens a Savings account; the opening deposit is required (includes Deposit Amount). */
    public SavingsAccount openSavings(String customerId, BigDecimal initialDeposit) {
        Customer holder = findCustomer(customerId);
        return add(holder, new SavingsAccount(holder, branch, initialDeposit));
    }

    /** Opens an Investment account; the BWP500.00 minimum is checked (includes Check Minimum Deposit). */
    public InvestmentAccount openInvestment(String customerId, BigDecimal initialDeposit) {
        Customer holder = findCustomer(customerId);
        return add(holder, new InvestmentAccount(holder, branch, initialDeposit));
    }

    /** Opens a Cheque account; employer details are required (includes Record Employer Details). */
    public ChequeAccount openCheque(String customerId, String employerName, String employerAddress) {
        Customer holder = findCustomer(customerId);
        return add(holder, new ChequeAccount(holder, branch, employerName, employerAddress));
    }

    /** Adds a newly opened account to its holder and to the bank. A subclass can save it first. */
    protected <T extends Account> T add(Customer holder, T account) {
        holder.addAccount(account);
        accounts.put(account.getAccountNumber(), account);
        return account;
    }

    /** Adds an account loaded from the database to its holder and to the bank. */
    public void restoreAccount(Account account) {
        account.getHolder().addAccount(account);
        accounts.put(account.getAccountNumber(), account);
    }

    // ---- Search Account, View Accounts & Balance ----

    public Account findAccount(String accountNumber) {
        Account account = accounts.get(accountNumber);
        if (account == null) {
            throw new BankException("No account with number " + accountNumber + ".");
        }
        return account;
    }

    public List<Account> accountsOf(String customerId) {
        return findCustomer(customerId).getAccounts();
    }

    // ---- Deposit, Withdraw Funds (Reject Withdrawal extends it) ----

    public void deposit(String accountNumber, BigDecimal amount) {
        findAccount(accountNumber).deposit(amount);
    }

    /**
     * Withdraws money. A Savings account is not Withdrawable, so the request is rejected
     * before any balance changes (FR-11, FR-18).
     */
    public void withdraw(String accountNumber, BigDecimal amount) {
        Account account = findAccount(accountNumber);
        if (!(account instanceof Withdrawable withdrawable)) {
            throw new AccountRuleException("Withdrawals are not allowed on a " + account.getAccountType()
                    + " account.");
        }
        withdrawable.withdraw(amount);
    }

    // ---- Pay Monthly Interest (includes Calculate Interest) ----

    /**
     * Pays interest to every Savings and Investment account for the month. An account that has
     * already been paid for that month is skipped, so running it twice never pays twice (FR-21).
     *
     * @return the accounts that were paid in this run
     */
    public List<Account> payMonthlyInterest(YearMonth month) {
        List<Account> paid = new ArrayList<>();
        for (Account account : accounts.values()) {
            if (account instanceof InterestBearing payer && !month.equals(account.getLastInterestMonth())) {
                payer.payInterest(month);
                paid.add(account);
            }
        }
        return paid;
    }

    public List<Customer> getCustomers() {
        return List.copyOf(customers.values());
    }

    public List<Account> getAccounts() {
        return List.copyOf(accounts.values());
    }
}
