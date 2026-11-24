package bank.view;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Map;
import java.util.Set;

/** Main menu. The controller says which screens this user may open (FR-07). */
public class DashboardView extends BaseView {

    /** Implemented by the controller that handles navigation. */
    public interface Listener {
        void onOpenScreen(Screen screen);
    }

    private final VBox root;
    private final Label welcome = new Label();
    private final FlowPane buttons = new FlowPane(12, 12);
    private final HBox summary = new HBox(12);
    private Listener listener;

    public DashboardView() {
        buttons.setPrefWrapLength(520);
        welcome.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0d47a1;");
        root = Ui.page(welcome, summary, Ui.heading("What would you like to do?"), Ui.subtitle("Pick a card below. Use Back to Menu at the top of any screen to return here."), buttons, statusLabel());
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    /** Shows a row of "at a glance" numbers, e.g. Customers 12. Display only. */
    public void setSummary(Map<String, String> figures) {
        summary.getChildren().clear();
        String[] colors = {Ui.TEAL, Ui.BLUE, Ui.ORANGE, Ui.PURPLE};
        int i = 0;
        for (Map.Entry<String, String> figure : figures.entrySet()) {
            summary.getChildren().add(Ui.stat(figure.getValue(), figure.getKey(), colors[i % colors.length]));
            i++;
        }
    }

    public void setWelcome(String text) {
        welcome.setText(text);
    }

    /** Shows one button for each screen the user may open. */
    public void setAvailableScreens(Set<Screen> screens) {
        buttons.getChildren().clear();
        for (Screen screen : Screen.values()) {
            if (screens.contains(screen)) {
                Button button = Ui.colored(screen.getLabel() + "\n" + screen.getDescription(), screen.getColor(),
                        "-fx-font-size: 15px; -fx-background-radius: 12; -fx-text-alignment: center;");
                button.setMinWidth(250);
                button.setMinHeight(90);
                button.setOnAction(event -> {
                    if (listener != null) {
                        listener.onOpenScreen(screen);
                    }
                });
                buttons.getChildren().add(button);
            }
        }
    }

    @Override
    public Parent getRoot() {
        return root;
    }
}
