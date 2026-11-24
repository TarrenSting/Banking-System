package bank.exception;

/** Thrown when an amount is missing, zero or negative (FR-15, FR-16). */
public class InvalidAmountException extends BankException {

    public InvalidAmountException(String message) {
        super(message);
    }
}
