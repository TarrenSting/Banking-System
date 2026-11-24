package bank.view;

/**
 * Plain text data passed between the views and the controllers. Everything is a String or a
 * simple value, so the boundary classes never import a domain class and hold no business logic.
 * The controller parses and validates the text.
 */
public final class ViewData {

    private ViewData() {
    }

    /** One line of an account list. */
    public record AccountRow(String number, String type, String balance) {
    }

    /** One line of a transaction history. */
    public record TransactionRow(String time, String type, String amount, String balanceAfter, String reference) {
    }

    /** One line of a customer search result. */
    public record CustomerRow(String id, String name, String address, String phone, String email) {
    }

    /** What the user typed on the customer form. */
    public record CustomerForm(boolean company, String firstName, String surname, String companyName,
                               String address, String phone, String email, boolean employed) {
    }

    /** What the user typed on the open-account form. */
    public record AccountRequest(String customerId, AccountKind kind, String initialDeposit,
                                 String employerName, String employerAddress) {
    }
}
