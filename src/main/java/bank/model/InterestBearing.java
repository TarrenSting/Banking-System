package bank.model;

import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * Implemented by accounts that earn monthly interest: Savings and Investment.
 * Cheque accounts do not implement it. The monthly run works on this type, so it
 * never needs to know which account class it is dealing with (polymorphism).
 */
public interface InterestBearing {

    /** The monthly rate as a fraction, e.g. 0.05 for 5%. */
    BigDecimal getMonthlyRate();

    /** Interest that would be paid on the current balance, rounded to two decimals. */
    BigDecimal calculateInterest();

    /**
     * Adds the interest for the given month to the balance and records it as an
     * INTEREST transaction. An account is never paid twice for the same month (FR-21).
     */
    void payInterest(YearMonth month);

    /** Overload: pays interest for the current month. */
    default void payInterest() {
        payInterest(YearMonth.now());
    }
}
