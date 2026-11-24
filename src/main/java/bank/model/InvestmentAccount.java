package bank.model;

import bank.exception.AccountRuleException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Investment account: deposits and withdrawals, 5% monthly interest, and it can only be
 * opened with an initial deposit of at least BWP500.00 (FR-12, FR-19).
 */
public class InvestmentAccount extends Account implements InterestBearing, Withdrawable {

    public static final BigDecimal MONTHLY_RATE = new BigDecimal("0.05");
    public static final BigDecimal MIN_OPENING = new BigDecimal("500.00");

    /**
     * Opens the account. The opening deposit is recorded as the first transaction.
     *
     * @throws AccountRuleException if the initial deposit is below BWP500.00
     */
    public InvestmentAccount(Customer holder, String branch, BigDecimal initialDeposit) {
        super(holder, branch);
        if (initialDeposit == null || money(initialDeposit).compareTo(MIN_OPENING) < 0) {
            throw new AccountRuleException("An Investment account needs an initial deposit of at least BWP"
                    + MIN_OPENING + ".");
        }
        deposit(initialDeposit, "Opening deposit");
    }

    /** Overload used when loading from the database; the balance is restored from stored transactions. */
    public InvestmentAccount(String accountNumber, Customer holder, String branch, LocalDate dateOpened) {
        super(accountNumber, holder, branch, dateOpened);
    }

    @Override
    public String getAccountType() {
        return "Investment";
    }

    @Override
    public void withdraw(BigDecimal amount) {
        debit(amount);
    }

    @Override
    public BigDecimal getMonthlyRate() {
        return MONTHLY_RATE;
    }

    @Override
    public BigDecimal calculateInterest() {
        return interestOn(MONTHLY_RATE);
    }

    @Override
    public void payInterest(YearMonth month) {
        applyInterest(month, MONTHLY_RATE);
    }
}
