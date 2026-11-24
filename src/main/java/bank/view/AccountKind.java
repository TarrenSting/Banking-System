package bank.view;

/**
 * The account types a user can pick on the screen. This is a display choice only;
 * the controller turns it into the right domain class.
 */
public enum AccountKind {
    SAVINGS("Savings"),
    INVESTMENT("Investment"),
    CHEQUE("Cheque");

    private final String label;

    AccountKind(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Shown in drop-down lists. */
    @Override
    public String toString() {
        return label;
    }
}
