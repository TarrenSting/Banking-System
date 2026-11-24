package bank.view;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Deposit screen (use case: Deposit). The amount stays text until the controller parses it. */
public class DepositView extends AccountSelectView {

    /** Implemented by the deposit controller. */
    public interface Listener extends AccountSelectView.Finder {
        void onDeposit(String accountNumber, String amount);
    }

    private final VBox root;
    private final TextField amount = new TextField();
    private Listener listener;

    public DepositView() {
        amount.setPromptText("0.00");
        amount.setOnAction(event -> submit());
        Button action = Ui.success("Deposit");
        action.setOnAction(event -> submit());

        root = Ui.page(
                Ui.heading("Deposit"),
                Ui.subtitle("Choose the account, type the amount and press Deposit."),
                accountSection(),
                Ui.card(new HBox(10, new Label("Amount (BWP)"), amount, action)),
                statusLabel());
    }

    private void submit() {
        if (listener != null) {
            listener.onDeposit(selectedAccount(), Ui.text(amount));
        }
    }

    /** Empties the amount box after a successful deposit. */
    public void clearAmount() {
        amount.clear();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
        setFinder(listener);
    }

    @Override
    public Parent getRoot() {
        return root;
    }
}
