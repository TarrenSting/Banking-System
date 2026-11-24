package bank.view;

import bank.controller.AppController;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;


/**
 * JavaFX entry point and screen frame (title bar, Back and Log Out buttons).
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
        stage.setTitle("CSE202 Banking System");
        showLogin();
        stage.show();
    }

    private void setContent(Parent root) {
        if (stage.getScene() == null) {
            stage.setScene(new Scene(root, 960, 680));
        } else {
            stage.getScene().setRoot(root);
        }
    }

    private void showLogin() {
        LoginView view = new LoginView();
        controller.bindLogin(view, this::showDashboard);
        BorderPane frame = new BorderPane(view.getRoot());
        frame.setPadding(new Insets(40));
        frame.setStyle("-fx-background-color: linear-gradient(to bottom right, #0d47a1, #42a5f5);");
        setContent(frame);
    }

    private void showDashboard() {
        DashboardView view = new DashboardView();
        view.setWelcome(controller.welcomeText());
        view.setAvailableScreens(controller.allowedScreens());
        view.setSummary(controller.summary());
        view.setListener(this::showScreen);
        setContent(frame("Main Menu", view, false, Ui.NAVY));
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
        setContent(frame(screen.getLabel(), view, true, screen.getColor()));
    }

    /** Wraps a view with a title bar holding the Back and Log Out buttons. */
    private BorderPane frame(String title, BaseView view, boolean withBack, String color) {
        Label heading = new Label(withBack ? "Main Menu   >   " + title : "Banking System   |   " + title);
        heading.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bar = new HBox(10, heading, spacer);
        bar.setAlignment(Pos.CENTER_LEFT);
        if (withBack) {
            Button back = Ui.button("Back to Menu");
            back.setOnAction(event -> showDashboard());
            bar.getChildren().add(back);
        }
        Button logOut = Ui.danger("Log Out");
        logOut.setOnAction(event -> {
            controller.logOut();
            showLogin();
        });
        bar.getChildren().add(logOut);
        bar.setPadding(new Insets(12, 16, 12, 16));
        bar.setStyle("-fx-background-color: linear-gradient(to right, " + color + ", derive(" + color + ", 35%));");

        BorderPane frame = new BorderPane(view.getRoot());
        frame.setTop(bar);
        return frame;
    }
}
