package bank.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Savings account: deposits only, 0.05% monthly interest (FR-11, FR-19).
 * It implements InterestBearing but not Withdrawable, so it has no withdraw method.
 */
public class SavingsAccount extends Account implements InterestBearing {

    public static final BigDecimal MONTHLY_RATE = new BigDecimal("0.0005");

    public SavingsAccount(Customer holder, String branch) {
        super(holder, branch);
    }

    /**
     * Opens the account with a first deposit, which is recorded as the first transaction.
     * This is the "Open Savings Account includes Deposit Amount" use case relationship.
     *
     * @throws bank.exception.InvalidAmountException if the deposit is not greater than zero
     */
    public SavingsAccount(Customer holder, String branch, BigDecimal initialDeposit) {
        super(holder, branch);
        requirePositive(initialDeposit);
        deposit(initialDeposit, "Opening deposit");
    }

    /** Overload used when loading from the database. */
    public SavingsAccount(String accountNumber, Customer holder, String branch, LocalDate dateOpened) {
        super(accountNumber, holder, branch, dateOpened);
    }

    @Override
    public String getAccountType() {
        return "Savings";
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
