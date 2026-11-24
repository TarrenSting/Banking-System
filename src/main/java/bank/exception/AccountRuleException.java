package bank.exception;

/**
 * Thrown when an account rule is broken: the BWP500.00 Investment minimum (FR-12),
 * the employed-customer rule for Cheque accounts (FR-13), a withdrawal from an
 * account that cannot be withdrawn from (FR-11), or interest paid twice in a month (FR-21).
 */
public class AccountRuleException extends BankException {

    public AccountRuleException(String message) {
        super(message);
    }
}
