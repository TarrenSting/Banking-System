package bank.model;

import bank.exception.AccountRuleException;
import bank.exception.InsufficientFundsException;
import bank.exception.InvalidAmountException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract base class of every account (abstraction). An account cannot exist without
 * the customer who holds it, so the holder is a required constructor argument.
 * All attributes are private (encapsulation); the balance only changes through
 * deposit(), the protected debit()/applyInterest() helpers and the subclasses.
 */
public abstract class Account {

    private final String accountNumber;
    private final Customer holder;
    private final String branch;
    private final LocalDate dateOpened;
    private BigDecimal balance = money(BigDecimal.ZERO);
    private YearMonth lastInterestMonth;
    private final List<Transaction> transactions = new ArrayList<>();

    /** Opens a new account with a generated account number, dated today. */
    protected Account(Customer holder, String branch) {
        this(IdGenerator.nextAccountNumber(), holder, branch, LocalDate.now());
    }

    /** Overload used when an account is loaded back from the database. */
    protected Account(String accountNumber, Customer holder, String branch, LocalDate dateOpened) {
        if (holder == null) {
            throw new AccountRuleException("An account must be held by a customer.");
        }
        if (branch == null || branch.isBlank()) {
            throw new AccountRuleException("A branch is required.");
        }
        this.accountNumber = accountNumber;
        this.holder = holder;
        this.branch = branch;
        this.dateOpened = dateOpened;
    }

    /** Rounds an amount to two decimals, half-up (NFR-05). */
    public static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    /** Short name shown to the user, e.g. "Savings". Overridden by every subclass. */
    public abstract String getAccountType();

    /** Puts money into the account. */
    public void deposit(BigDecimal amount) {
        deposit(amount, null);
    }

    /** Overload: deposit with a note such as a salary reference. */
    public void deposit(BigDecimal amount, String reference) {
        BigDecimal value = requirePositive(amount);
        balance = balance.add(value);
        record(TransactionType.DEPOSIT, value, reference);
    }

    /** Takes money out. Only called by withdraw() in the Withdrawable subclasses. */
    protected void debit(BigDecimal amount) {
        BigDecimal value = requirePositive(amount);
        if (value.compareTo(balance) > 0) {
            throw new InsufficientFundsException("Insufficient funds: balance is BWP"
                    + balance + " but BWP" + value + " was requested.");
        }
        balance = balance.subtract(value);
        record(TransactionType.WITHDRAWAL, value, null);
    }

    /**
     * Pays interest at the given monthly rate. Never pays twice for the same month (FR-21).
     * A zero balance earns nothing, so no transaction is recorded for it.
     */
    protected void applyInterest(YearMonth month, BigDecimal rate) {
        if (month.equals(lastInterestMonth)) {
            throw new AccountRuleException("Interest for " + month + " has already been paid on account "
                    + accountNumber + ".");
        }
        BigDecimal interest = interestOn(rate);
        lastInterestMonth = month;
        if (interest.signum() > 0) {
            balance = balance.add(interest);
            record(TransactionType.INTEREST, interest, month.toString());
        }
    }

    /** Interest on the current balance at the given rate, rounded to two decimals. */
    protected BigDecimal interestOn(BigDecimal rate) {
        return money(balance.multiply(rate));
    }

    /** Validates an amount: it must exist and be greater than zero after rounding. */
    protected static BigDecimal requirePositive(BigDecimal amount) {
        if (amount == null) {
            throw new InvalidAmountException("An amount is required.");
        }
        BigDecimal value = money(amount);
        if (value.signum() <= 0) {
            throw new InvalidAmountException("The amount must be greater than zero.");
        }
        return value;
    }

    /**
     * Puts back the state of an account loaded from the database: the transaction list, the
     * balance (the last "balance after") and the month interest was last paid for.
     */
    public void restoreHistory(List<Transaction> history, YearMonth lastInterest) {
        transactions.clear();
        transactions.addAll(history);
        balance = history.isEmpty() ? money(BigDecimal.ZERO) : history.get(history.size() - 1).getBalanceAfter();
        lastInterestMonth = lastInterest;
    }

    protected void record(TransactionType type, BigDecimal amount, String reference) {
        transactions.add(new Transaction(accountNumber, type, amount, balance, reference));
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public Customer getHolder() {
        return holder;
    }

    public String getBranch() {
        return branch;
    }

    public LocalDate getDateOpened() {
        return dateOpened;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    /** The month interest was last paid for, or null if never (stored for FR-21). */
    public YearMonth getLastInterestMonth() {
        return lastInterestMonth;
    }

    /** Transactions oldest first. The list cannot be changed from outside. */
    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    @Override
    public String toString() {
        return getAccountType() + " " + accountNumber + " BWP" + balance;
    }
}
