package bank.view;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * The "which account?" part shared by the Deposit, Withdraw and Transaction History screens.
 * A customer picks one of their own accounts from a list; a teller types an account number.
 * The controller does the lookup and the access check.
 */
public abstract class AccountSelectView extends BaseView {

    /** Implemented by each screen's controller. */
    public interface Finder {
        void onFindAccount(String accountNumber);
    }

    private final TextField accountNumber = new TextField();
    private final ComboBox<String> accountPicker = new ComboBox<>();
    private final Button find = Ui.primary("Find Account");
    private final Label accountSummary = new Label("Choose an account to begin.");
    private boolean usePicker;
    private Finder finder;

    protected AccountSelectView() {
        accountNumber.setPromptText("Account number, e.g. ACC0000001");
        accountNumber.setOnAction(event -> findAccount());
        find.setOnAction(event -> findAccount());
        accountPicker.setPromptText("Choose your account");
        accountPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                findAccount();
            }
        });
        accountPicker.setVisible(false);
        accountPicker.setManaged(false);
        accountSummary.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0d47a1;");
    }

    /** The panel with the account chooser and the selected account's balance. */
    protected VBox accountSection() {
        return Ui.card(new HBox(10, new Label("Account"), accountPicker, accountNumber, find), accountSummary);
    }

    protected void setFinder(Finder finder) {
        this.finder = finder;
    }

    private void findAccount() {
        if (finder != null) {
            finder.onFindAccount(selectedAccount());
        }
    }

    /** The account number the user chose or typed. */
    protected String selectedAccount() {
        if (usePicker) {
            String choice = accountPicker.getValue();
            return choice == null ? "" : choice.split(" ")[0];
        }
        return Ui.text(accountNumber);
    }

    /**
     * Lets the user pick from a list instead of typing, e.g. "ACC0000001 (Savings)". Call it after
     * the listener is set: the first account is chosen straight away. An empty list shows the hint.
     */
    public void setAccountChoices(List<String> choices, String hintWhenEmpty) {
        usePicker = true;
        accountNumber.setVisible(false);
        accountNumber.setManaged(false);
        find.setVisible(false);
        find.setManaged(false);
        accountPicker.setVisible(true);
        accountPicker.setManaged(true);
        accountPicker.getItems().setAll(choices);
        if (choices.isEmpty()) {
            accountSummary.setText(hintWhenEmpty);
        } else {
            accountPicker.setValue(choices.get(0));
        }
    }

    /** Shows the account that was found, e.g. "Savings account ACC0000001 | Balance: BWP2000.00". */
    public void showAccount(ViewData.AccountRow row) {
        accountSummary.setText(row.type() + " account " + row.number() + "   |   Balance: BWP" + row.balance());
    }
}
