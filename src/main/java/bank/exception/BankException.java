package bank.exception;

/**
 * Base type for every business-rule failure in the banking system.
 * The GUI catches this type and shows {@link #getMessage()} to the user.
 */
public class BankException extends RuntimeException {

    public BankException(String message) {
        super(message);
    }
}
