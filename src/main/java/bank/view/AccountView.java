package bank.view;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Lists a customer's accounts and opens new ones (use cases: View Accounts and Balance, Open Account).
 * The employer fields are only enabled for a Cheque account and the deposit box is disabled for one,
 * which is screen behaviour only: the rules themselves (BWP500.00 minimum, employed customers)
 * are checked by the model.
 */
public class AccountView extends BaseView {

    /** Implemented by the account controller. */
    public interface Listener {
        void onLoadAccounts(String customerId);

        void onOpenAccount(ViewData.AccountRequest request);
    }

    private final VBox root;
    private final TextField customerId = new TextField();
    private final HBox customerRow;
    private final VBox openSection;
    private final Label intro = Ui.subtitle("Step 1: find the customer. Step 2: open a new account if needed.");
    private final TableView<ViewData.AccountRow> table = new TableView<>();
    private final ComboBox<AccountKind> kind = new ComboBox<>();
    private final TextField initialDeposit = new TextField();
    private final TextField employerName = new TextField();
    private final TextField employerAddress = new TextField();
    private final Label kindHint = new Label();
    private final Label depositLabel = new Label("Opening deposit (BWP)");
    private final Label employerNameLabel = new Label("Employer name");
    private final Label employerAddressLabel = new Label("Employer address");
    private Listener listener;

    public AccountView() {
        customerId.setPromptText("Customer ID, e.g. C00001 (see the Customers screen)");
        customerId.setPrefWidth(320);
        customerId.setOnAction(event -> {
            if (listener != null) {
                listener.onLoadAccounts(Ui.text(customerId));
            }
        });
        Button load = Ui.primary("View Accounts");
        load.setOnAction(event -> {
            if (listener != null) {
                listener.onLoadAccounts(Ui.text(customerId));
            }
        });

        table.getColumns().add(Ui.<ViewData.AccountRow>column("Account number", ViewData.AccountRow::number, 160));
        table.getColumns().add(Ui.<ViewData.AccountRow>column("Type", ViewData.AccountRow::type, 120));
        table.getColumns().add(Ui.<ViewData.AccountRow>column("Balance (BWP)", ViewData.AccountRow::balance, 140));
        table.setPrefHeight(180);

        kind.getItems().addAll(AccountKind.values());
        kind.setValue(AccountKind.SAVINGS);
        kind.valueProperty().addListener((observable, oldValue, newValue) -> applyKind());
        initialDeposit.setPromptText("0.00");

        GridPane form = Ui.grid();
        Ui.addRow(form, 0, "Account type", kind);
        form.add(kindHint, 1, 1);
        form.add(depositLabel, 0, 2);
        form.add(initialDeposit, 1, 2);
        form.add(employerNameLabel, 0, 3);
        form.add(employerName, 1, 3);
        form.add(employerAddressLabel, 0, 4);
        form.add(employerAddress, 1, 4);
        kindHint.setStyle("-fx-text-fill: #555555;");
        applyKind();

        Button open = Ui.success("Open Account");
        open.setOnAction(event -> {
            if (listener != null) {
                listener.onOpenAccount(new ViewData.AccountRequest(
                        Ui.text(customerId), kind.getValue(), Ui.text(initialDeposit),
                        Ui.text(employerName), Ui.text(employerAddress)));
            }
        });

        customerRow = new HBox(10, new Label("Customer"), customerId, load);
        openSection = Ui.card(Ui.heading("Open a new account for this customer"), form, open);

        root = Ui.page(
                Ui.heading("Accounts"),
                intro,
                Ui.card(customerRow, table),
                openSection,
                statusLabel());
    }

    /** Shows only the fields the chosen account type needs, with a one-line explanation. */
    private void applyKind() {
        boolean cheque = kind.getValue() == AccountKind.CHEQUE;
        show(depositLabel, !cheque);
        show(initialDeposit, !cheque);
        show(employerNameLabel, cheque);
        show(employerName, cheque);
        show(employerAddressLabel, cheque);
        show(employerAddress, cheque);
        kindHint.setText(switch (kind.getValue()) {
            case SAVINGS -> "Deposits only. Earns 0.05% interest a month.";
            case INVESTMENT -> "Opening deposit of at least BWP500.00. Earns 5% interest a month.";
            case CHEQUE -> "For employed customers. Employer details are required. No interest.";
        });
    }

    private static void show(javafx.scene.Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    /**
     * A customer sees only their own accounts, so the customer box and the open-account form are
     * hidden; a teller sees both. Screen behaviour only: the controller still checks the role.
     */
    public void setOwnAccountsOnly(boolean ownOnly) {
        if (ownOnly) {
            intro.setText("These are your accounts and their balances.");
        }
        customerRow.setVisible(!ownOnly);
        customerRow.setManaged(!ownOnly);
        openSection.setVisible(!ownOnly);
        openSection.setManaged(!ownOnly);
    }

    public void showAccounts(List<ViewData.AccountRow> rows) {
        table.getItems().setAll(rows);
    }

    /** Empties the open-account form after a successful opening. */
    public void clearForm() {
        initialDeposit.clear();
        employerName.clear();
        employerAddress.clear();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public Parent getRoot() {
        return root;
    }
}
