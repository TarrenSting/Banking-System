package bank.model;

import java.math.BigDecimal;

/**
 * Implemented by accounts that allow withdrawals: Investment and Cheque.
 * Savings accounts do not implement it, so a Savings account has no withdraw method at all.
 */
public interface Withdrawable {

    /**
     * Takes money out of the account.
     *
     * @throws bank.exception.InvalidAmountException    if the amount is not greater than zero
     * @throws bank.exception.InsufficientFundsException if the amount is more than the balance
     */
    void withdraw(BigDecimal amount);
}
