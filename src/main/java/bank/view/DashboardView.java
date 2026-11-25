package bank.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Dashboard: headline numbers, the customer's account cards, quick-action buttons and recent
 * transactions. The controller supplies every figure and says which screens this user may open
 * (FR-07); this class only displays them.
 */
public class DashboardView extends BaseView {

    /** Implemented by the controller that handles navigation. */
    public interface Listener {
        void onOpenScreen(Screen screen);
    }

    private final VBox root;
    private final Label title = new Label();
    private final Label welcome = new Label();
    private final HBox summary = new HBox(12);
    private final Label accountsHeading = Ui.heading("Your accounts");
    private final HBox accountCards = new HBox(12);
    private final VBox actions = new VBox(8);
    private final VBox recentRows = new VBox(6);
    private final VBox recentCard;
    private Listener listener;

    public DashboardView() {
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + Ui.DEEP + ";");
        welcome.setStyle("-fx-font-size: 13px; -fx-text-fill: #455a64;");

        VBox quickCard = Ui.card(Ui.heading("Quick Actions"), actions);
        quickCard.setMinWidth(260);
        recentCard = Ui.card(Ui.heading("Recent Transactions"), recentRows);
        recentCard.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(recentCard, Priority.ALWAYS);
        HBox lower = new HBox(14, quickCard, recentCard);

        root = Ui.page(new VBox(2, title, welcome), summary, accountsHeading, accountCards, lower, statusLabel());
        setAccountCards(List.of());
        setRecent(List.of(), false);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setTitle(String text) {
        title.setText(text);
    }

    public void setWelcome(String text) {
        welcome.setText(text);
    }

    /** Shows a row of headline numbers, e.g. Customers 12. Display only. */
    public void setSummary(Map<String, String> figures) {
        summary.getChildren().clear();
        String[] colors = {Ui.TEAL, Ui.BLUE, Ui.ORANGE, Ui.PURPLE};
        int i = 0;
        for (Map.Entry<String, String> figure : figures.entrySet()) {
            summary.getChildren().add(Ui.stat(figure.getValue(), figure.getKey(), colors[i % colors.length]));
            i++;
        }
    }

    /** One card per account of a customer, with its balance. Hidden when there are none. */
    public void setAccountCards(List<ViewData.AccountRow> rows) {
        accountCards.getChildren().clear();
        for (ViewData.AccountRow row : rows) {
            String color = "Savings".equals(row.type()) ? Ui.BLUE : "Cheque".equals(row.type()) ? Ui.TEAL : Ui.ORANGE;
            accountCards.getChildren().add(Ui.stat("BWP" + row.balance(), row.type() + "  " + row.number(), color));
        }
        boolean any = !rows.isEmpty();
        accountsHeading.setVisible(any);
        accountsHeading.setManaged(any);
        accountCards.setVisible(any);
        accountCards.setManaged(any);
    }

    /** The newest transactions. The card is hidden when {@code show} is false (tellers). */
    public void setRecent(List<ViewData.TransactionRow> rows, boolean show) {
        recentRows.getChildren().clear();
        if (rows.isEmpty()) {
            recentRows.getChildren().add(Ui.subtitle("No transactions yet."));
        }
        for (ViewData.TransactionRow row : rows) {
            boolean out = "Withdrawal".equals(row.type());
            Label type = new Label(row.type());
            type.setStyle("-fx-font-weight: bold; -fx-text-fill: #1b2a4a;");
            Label time = new Label(row.time());
            time.setStyle("-fx-font-size: 11px; -fx-text-fill: #78909c;");
            Label amount = new Label((out ? "-BWP" : "+BWP") + row.amount());
            amount.setStyle("-fx-font-weight: bold; -fx-text-fill: " + (out ? Ui.RED : Ui.GREEN) + ";");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            HBox line = new HBox(8, new VBox(1, type, time), spacer, amount);
            line.setAlignment(Pos.CENTER_LEFT);
            line.setPadding(new Insets(4, 0, 4, 0));
            recentRows.getChildren().add(line);
        }
        recentCard.setVisible(show);
        recentCard.setManaged(show);
    }

    /** Shows one quick-action button for each screen the user may open. */
    public void setAvailableScreens(Set<Screen> screens) {
        actions.getChildren().clear();
        for (Screen screen : Screen.values()) {
            if (screens.contains(screen)) {
                Button button = Ui.button(screen.getLabel());
                button.setMaxWidth(Double.MAX_VALUE);
                button.setAlignment(Pos.CENTER_LEFT);
                button.setOnAction(event -> {
                    if (listener != null) {
                        listener.onOpenScreen(screen);
                    }
                });
                actions.getChildren().add(button);
            }
        }
    }

    @Override
    public Parent getRoot() {
        return root;
    }
}
