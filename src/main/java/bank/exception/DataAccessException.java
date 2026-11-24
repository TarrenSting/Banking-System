package bank.exception;

/** The database could not be reached or a stored value could not be read or written. */
public class DataAccessException extends BankException {

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}
