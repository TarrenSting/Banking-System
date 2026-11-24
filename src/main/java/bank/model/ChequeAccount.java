package bank.model;

import bank.exception.AccountRuleException;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Cheque account: salary account with deposits and withdrawals and no interest (FR-13).
 * It can only be opened for a working customer, and the employer's name and address are recorded.
 */
public class ChequeAccount extends Account implements Withdrawable {

    private final String employerName;
    private final String employerAddress;

    /**
     * @throws AccountRuleException if the customer is not employed or the employer details are missing
     */
    public ChequeAccount(Customer holder, String branch, String employerName, String employerAddress) {
        super(holder, branch);
        if (!holder.isEmployed()) {
            throw new AccountRuleException("A Cheque account can only be opened for a working customer.");
        }
        if (isBlank(employerName) || isBlank(employerAddress)) {
            throw new AccountRuleException("The employer's name and address are required.");
        }
        this.employerName = employerName;
        this.employerAddress = employerAddress;
    }

    /** Overload used when loading from the database. */
    public ChequeAccount(String accountNumber, Customer holder, String branch, LocalDate dateOpened,
                         String employerName, String employerAddress) {
        super(accountNumber, holder, branch, dateOpened);
        this.employerName = employerName;
        this.employerAddress = employerAddress;
    }

    private static boolean isBlank(String text) {
        return text == null || text.isBlank();
    }

    @Override
    public String getAccountType() {
        return "Cheque";
    }

    @Override
    public void withdraw(BigDecimal amount) {
        debit(amount);
    }

    public String getEmployerName() {
        return employerName;
    }

    public String getEmployerAddress() {
        return employerAddress;
    }
}
