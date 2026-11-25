package bank.view;

import bank.controller.AppController;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * JavaFX entry point and screen frame: a navy side menu, a top bar with the user's name and Log Out,
 * and the current screen in the middle.
 *
 * <p>This class only builds screens and moves between them. Everything that decides what a click
 * means is in the controllers: it asks {@link AppController} to connect each new screen to its
 * controller, and imports no model or data-store class.
 */
public class MainApp extends Application {

    private final AppController controller = new AppController();
    private Stage stage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle(Ui.BANK_NAME + " - CSE202 Banking System");
        showLogin();
        stage.show();
    }

    private void setContent(Parent root) {
        if (stage.getScene() == null) {
            stage.setScene(new Scene(root, 1120, 720));
        } else {
            stage.getScene().setRoot(root);
        }
    }

    private void showLogin() {
        LoginView view = new LoginView();
        controller.bindLogin(view, this::showDashboard);
        setContent(view.getRoot());
    }

    private void showDashboard() {
        DashboardView view = new DashboardView();
        view.setTitle(controller.dashboardTitle());
        view.setWelcome(controller.welcomeText());
        view.setSummary(controller.summary());
        view.setAccountCards(controller.accountRows());
        view.setRecent(controller.recentTransactions(), !controller.recentTransactions().isEmpty()
                || controller.allowedScreens().contains(Screen.DEPOSIT));
        view.setAvailableScreens(controller.allowedScreens());
        view.setListener(this::showScreen);
        setContent(frame("Dashboard", view, null));
    }

    private void showScreen(Screen screen) {
        if (!controller.allowedScreens().contains(screen)) {
            return;
        }
        BaseView view;
        switch (screen) {
            case CUSTOMERS -> {
                CustomerView customers = new CustomerView();
                controller.bindCustomers(customers);
                view = customers;
            }
            case ACCOUNTS -> {
                AccountView accounts = new AccountView();
                controller.bindAccounts(accounts);
                view = accounts;
            }
            case DEPOSIT -> {
                DepositView deposit = new DepositView();
                controller.bindDeposit(deposit);
                view = deposit;
            }
            case WITHDRAW -> {
                WithdrawView withdraw = new WithdrawView();
                controller.bindWithdraw(withdraw);
                view = withdraw;
            }
            case HISTORY -> {
                HistoryView history = new HistoryView();
                controller.bindHistory(history);
                view = history;
            }
            default -> {
                InterestView interest = new InterestView();
                controller.bindInterest(interest);
                view = interest;
            }
        }
        setContent(frame(screen.getLabel(), view, screen));
    }

    /** The side menu, top bar and a scrolling area around the screen. {@code active} is null for the dashboard. */
    private BorderPane frame(String title, BaseView view, Screen active) {
        BorderPane frame = new BorderPane();
        frame.setLeft(sideMenu(active));
        frame.setTop(topBar(title));
        ScrollPane scroll = new ScrollPane(view.getRoot());
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + Ui.PAGE_BG + "; -fx-background-color: " + Ui.PAGE_BG + ";");
        frame.setCenter(scroll);
        return frame;
    }

    private VBox sideMenu(Screen active) {
        Label name = new Label(Ui.BANK_NAME);
        name.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
        HBox brand = new HBox(10, Ui.logo(0.5), name);
        brand.setAlignment(Pos.CENTER_LEFT);
        brand.setPadding(new Insets(0, 0, 18, 6));

        VBox menu = new VBox(6, brand, navButton("Dashboard", active == null, this::showDashboard));
        for (Screen screen : Screen.values()) {
            if (controller.allowedScreens().contains(screen)) {
                menu.getChildren().add(navButton(screen.getLabel(), screen == active, () -> showScreen(screen)));
            }
        }
        menu.setPadding(new Insets(18, 12, 18, 12));
        menu.setPrefWidth(230);
        menu.setMinWidth(230);
        menu.setStyle("-fx-background-color: " + Ui.DEEP + ";");
        return menu;
    }

    private Button navButton(String text, boolean selected, Runnable action) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        String base = "-fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 8;"
                + " -fx-padding: 10 14 10 14; -fx-cursor: hand; -fx-background-color: ";
        String on = base + Ui.ACCENT + "; -fx-font-weight: bold;";
        String off = base + "transparent;";
        button.setStyle(selected ? on : off);
        if (!selected) {
            button.setOnMouseEntered(event -> button.setStyle(base + "#1a3a8a;"));
            button.setOnMouseExited(event -> button.setStyle(off));
        }
        button.setOnAction(event -> action.run());
        return button;
    }

    private HBox topBar(String title) {
        Label heading = new Label(title);
        heading.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + Ui.DEEP + ";");
        Label user = new Label(controller.welcomeText());
        user.setStyle("-fx-font-size: 13px; -fx-text-fill: #455a64;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button logOut = Ui.danger("Log Out");
        logOut.setOnAction(event -> {
            controller.logOut();
            showLogin();
        });
        HBox bar = new HBox(14, heading, spacer, user, logOut);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12, 20, 12, 20));
        bar.setStyle("-fx-background-color: #d6e3f8; -fx-border-color: transparent transparent #a9bfe6 transparent;");
        return bar;
    }
}
