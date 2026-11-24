package bank.exception;

/** Thrown when a withdrawal would take a balance below zero (FR-14, FR-16). */
public class InsufficientFundsException extends BankException {

    public InsufficientFundsException(String message) {
        super(message);
    }
}
