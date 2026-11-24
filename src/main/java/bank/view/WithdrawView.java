package bank.view;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Withdraw screen (use cases: Withdraw Funds, Reject Withdrawal). A refused withdrawal comes back as an error message. */
public class WithdrawView extends AccountSelectView {

    /** Implemented by the withdrawal controller. */
    public interface Listener extends AccountSelectView.Finder {
        void onWithdraw(String accountNumber, String amount);
    }

    private final VBox root;
    private final TextField amount = new TextField();
    private Listener listener;

    public WithdrawView() {
        amount.setPromptText("0.00");
        amount.setOnAction(event -> submit());
        Button action = Ui.warning("Withdraw");
        action.setOnAction(event -> submit());

        root = Ui.page(
                Ui.heading("Withdraw"),
                Ui.subtitle("Only accounts that allow withdrawals are listed (Savings accounts are deposit-only)."),
                accountSection(),
                Ui.card(new HBox(10, new Label("Amount (BWP)"), amount, action)),
                statusLabel());
    }

    private void submit() {
        if (listener != null) {
            listener.onWithdraw(selectedAccount(), Ui.text(amount));
        }
    }

    /** Empties the amount box after a successful withdrawal. */
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
