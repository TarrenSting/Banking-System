package bank.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Login screen (use case: Log In). A brand panel on the left and a login card on the right; the
 * Customer / Teller switch changes the wording only. The controller decides if the login is valid.
 */
public class LoginView extends BaseView {

    /** Implemented by the login controller. */
    public interface Listener {
        void onLogin(String username, String password, boolean asTeller);
    }

    private final HBox root;
    private final TextField username = new TextField();
    private final PasswordField password = new PasswordField();
    private final Label cardTitle = new Label();
    private final Label slogan = new Label();
    private final Label sloganSmall = new Label();
    private final Button customerTab = new Button("Customer");
    private final Button tellerTab = new Button("Teller");
    private boolean teller;
    private Listener listener;

    public LoginView() {
        useLightMessages();
        username.setPromptText("Username");
        password.setPromptText("Password");
        username.setPrefHeight(38);
        password.setPrefHeight(38);

        customerTab.setOnAction(event -> setTeller(false));
        tellerTab.setOnAction(event -> setTeller(true));
        customerTab.setMaxWidth(Double.MAX_VALUE);
        tellerTab.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(customerTab, Priority.ALWAYS);
        HBox.setHgrow(tellerTab, Priority.ALWAYS);
        HBox tabs = new HBox(6, customerTab, tellerTab);

        cardTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");
        Button login = Ui.colored("Login", Ui.ACCENT, "-fx-font-size: 15px;");
        login.setMaxWidth(Double.MAX_VALUE);
        login.setPrefHeight(40);
        login.setDefaultButton(true);
        login.setOnAction(event -> {
            if (listener != null) {
                listener.onLogin(Ui.text(username), password.getText(), teller);
            }
        });

        VBox card = new VBox(14, tabs, cardTitle, username, password, login, statusLabel());
        card.setPadding(new Insets(28));
        card.setMaxWidth(340);
        card.setStyle("-fx-background-color: #10246b; -fx-background-radius: 14;"
                + " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 24, 0.2, 0, 8);");

        Label name = new Label(Ui.BANK_NAME);
        name.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label tag = new Label("Bank Smarter. Live Better.");
        tag.setStyle("-fx-font-size: 13px; -fx-text-fill: #cfe0ff;");
        slogan.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");
        slogan.setWrapText(true);
        sloganSmall.setStyle("-fx-font-size: 14px; -fx-text-fill: #cfe0ff;");
        Region gap = new Region();
        gap.setMinHeight(40);
        VBox brand = new VBox(6, Ui.logo(1.6), name, tag, gap, slogan, sloganSmall);
        brand.setAlignment(Pos.CENTER_LEFT);
        brand.setPadding(new Insets(60));
        brand.setMinWidth(430);
        brand.setStyle("-fx-background-color: linear-gradient(to bottom right, #0a1f5c, #1b4fb5);");

        VBox right = new VBox(card);
        right.setAlignment(Pos.CENTER);
        right.setPadding(new Insets(30));
        right.setStyle("-fx-background-color: #0a1f5c;");
        HBox.setHgrow(right, Priority.ALWAYS);

        root = new HBox(brand, right);
        root.setStyle("-fx-base: #dbe7fb; -fx-accent: #1f6feb; -fx-focus-color: #1f6feb;");
        setTeller(false);
    }

    /** Changes only the wording and the highlighted tab. */
    private void setTeller(boolean teller) {
        this.teller = teller;
        cardTitle.setText(teller ? "Teller Login" : "Customer Login");
        slogan.setText(teller ? "Serve. Assist. Empower." : "Your Banking. Our Priority.");
        sloganSmall.setText(teller ? "Staff access to customers, accounts and transactions."
                : "Secure. Simple. Convenient.");
        String on = "-fx-background-color: #1f6feb; -fx-text-fill: white; -fx-font-weight: bold;"
                + " -fx-background-radius: 8; -fx-cursor: hand;";
        String off = "-fx-background-color: transparent; -fx-text-fill: #cfe0ff; -fx-font-weight: bold;"
                + " -fx-border-color: #3d5bb0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;";
        customerTab.setStyle(teller ? off : on);
        tellerTab.setStyle(teller ? on : off);
        clearStatus();
        clearPassword();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    /** Empties the password box, e.g. after a failed attempt. */
    public void clearPassword() {
        password.clear();
    }

    @Override
    public Parent getRoot() {
        return root;
    }
}
