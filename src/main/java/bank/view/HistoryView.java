package bank.view;

import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;

/**
 * Transaction history screen (use cases: View Transaction History, Filter By Date). One "Show"
 * drop-down filters by period: all time, last 7 days, last 30 days or this month.
 */
public class HistoryView extends AccountSelectView {

    /** Implemented by the history controller. */
    public interface Listener extends AccountSelectView.Finder {
        /** Either date may be null when the user wants no limit on that side. */
        void onShowHistory(String accountNumber, LocalDate from, LocalDate to);
    }

    private static final String ALL_TIME = "All time";
    private static final String LAST_7 = "Last 7 days";
    private static final String LAST_30 = "Last 30 days";
    private static final String THIS_MONTH = "This month";

    private final VBox root;
    private final ComboBox<String> period = new ComboBox<>();
    private final TableView<ViewData.TransactionRow> history = new TableView<>();
    private boolean resettingPeriod;
    private boolean accountShown;
    private Listener listener;

    public HistoryView() {
        period.getItems().addAll(ALL_TIME, LAST_7, LAST_30, THIS_MONTH);
        period.setValue(ALL_TIME);
        period.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!resettingPeriod && accountShown && listener != null) {
                listener.onShowHistory(selectedAccount(), periodStart(newValue), null);
            }
        });

        history.getColumns().add(Ui.<ViewData.TransactionRow>column("Date and time", ViewData.TransactionRow::time, 170));
        history.getColumns().add(Ui.<ViewData.TransactionRow>column("Type", ViewData.TransactionRow::type, 100));
        history.getColumns().add(Ui.<ViewData.TransactionRow>column("Amount", ViewData.TransactionRow::amount, 100));
        history.getColumns().add(Ui.<ViewData.TransactionRow>column("Balance after", ViewData.TransactionRow::balanceAfter, 120));
        history.getColumns().add(Ui.<ViewData.TransactionRow>column("Note", ViewData.TransactionRow::reference, 140));
        history.setPrefHeight(300);

        root = Ui.page(
                Ui.heading("Transaction History"),
                Ui.subtitle("Choose an account to see its deposits, withdrawals and interest, newest first."),
                accountSection(),
                Ui.card(new HBox(10, new Label("Show"), period), history),
                statusLabel());
    }

    /** The first day of the chosen period, or null for all time. */
    private static LocalDate periodStart(String choice) {
        LocalDate today = LocalDate.now();
        if (LAST_7.equals(choice)) {
            return today.minusDays(6);
        }
        if (LAST_30.equals(choice)) {
            return today.minusDays(29);
        }
        if (THIS_MONTH.equals(choice)) {
            return today.withDayOfMonth(1);
        }
        return null;
    }

    /** A newly found account shows its full history, so the period goes back to "All time". */
    @Override
    public void showAccount(ViewData.AccountRow row) {
        super.showAccount(row);
        accountShown = true;
        resettingPeriod = true;
        period.setValue(ALL_TIME);
        resettingPeriod = false;
    }

    public void showHistory(List<ViewData.TransactionRow> rows) {
        history.getItems().setAll(rows);
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
