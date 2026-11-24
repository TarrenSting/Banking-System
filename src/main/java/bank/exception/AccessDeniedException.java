package bank.exception;

/** Thrown when a logged-in user tries something their role does not allow (NFR-01). */
public class AccessDeniedException extends BankException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
