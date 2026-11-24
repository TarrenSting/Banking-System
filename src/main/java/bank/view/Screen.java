package bank.view;

/** The screens reachable from the main menu. The controller decides which ones a user may see. */
public enum Screen {
    CUSTOMERS("Customers", "Register, search and update customers", Ui.TEAL),
    ACCOUNTS("Accounts", "See balances or open a new account", Ui.BLUE),
    DEPOSIT("Deposit", "Put money into one of your accounts", Ui.GREEN),
    WITHDRAW("Withdraw", "Take money out of your Cheque or Investment account", Ui.ORANGE),
    HISTORY("Transaction History", "See every deposit, withdrawal and interest payment", Ui.SLATE),
    INTEREST("Monthly Interest", "Pay the month's interest", Ui.PURPLE);

    private final String label;
    private final String description;
    private final String color;

    Screen(String label, String description, String color) {
        this.label = label;
        this.description = description;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    /** One line under the name on the main menu. */
    public String getDescription() {
        return description;
    }

    /** Button colour on the main menu (display only). */
    public String getColor() {
        return color;
    }
}
