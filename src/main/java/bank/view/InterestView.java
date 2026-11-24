package bank.view;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.YearMonth;
import java.util.List;

/** Starts the monthly interest run with one action and lists the accounts that were paid (use case: Pay Monthly Interest). */
public class InterestView extends BaseView {

    /** Implemented by the interest controller. */
    public interface Listener {
        /** The month as typed, in the form yyyy-MM. The controller parses it. */
        void onRunInterest(String month);
    }

    private final VBox root;
    private final TextField month = new TextField(YearMonth.now().toString());
    private final TableView<ViewData.AccountRow> paid = new TableView<>();
    private final Label summary = new Label();
    private Listener listener;

    public InterestView() {
        month.setPromptText("yyyy-MM");
        month.setText(java.time.YearMonth.now().toString());
        Button run = Ui.success("Pay Interest");
        run.setOnAction(event -> {
            if (listener != null) {
                listener.onRunInterest(Ui.text(month));
            }
        });

        paid.getColumns().add(Ui.<ViewData.AccountRow>column("Account number", ViewData.AccountRow::number, 160));
        paid.getColumns().add(Ui.<ViewData.AccountRow>column("Type", ViewData.AccountRow::type, 120));
        paid.getColumns().add(Ui.<ViewData.AccountRow>column("New balance (BWP)", ViewData.AccountRow::balance, 160));
        paid.setPrefHeight(260);

        root = Ui.page(
                Ui.heading("Monthly Interest"),
                Ui.subtitle("Pays this month's interest to every Savings and Investment account. An account is never paid twice for the same month."),
                Ui.card(new HBox(10, new Label("Month"), month, run), summary, paid),
                statusLabel());
    }

    /** Lists the accounts paid in this run and a one-line summary. */
    public void showResults(List<ViewData.AccountRow> rows, String summaryText) {
        paid.getItems().setAll(rows);
        summary.setText(summaryText);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public Parent getRoot() {
        return root;
    }
}
