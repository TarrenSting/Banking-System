package bank.view;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Login screen (use case: Log In). Collects a username and password and hands them to the controller. */
public class LoginView extends BaseView {

    /** Implemented by the login controller. */
    public interface Listener {
        void onLogin(String username, String password);
    }

    private final VBox root;
    private final TextField username = new TextField();
    private final PasswordField password = new PasswordField();
    private Listener listener;

    public LoginView() {
        username.setPromptText("Username");
        password.setPromptText("Password");

        Button login = Ui.primary("Log In");
        login.setDefaultButton(true);
        login.setOnAction(event -> {
            if (listener != null) {
                listener.onLogin(Ui.text(username), password.getText());
            }
        });

        GridPane form = Ui.grid();
        Ui.addRow(form, 0, "Username", username);
        Ui.addRow(form, 1, "Password", password);

        root = Ui.page(Ui.heading("CSE202 Banking System"), Ui.subtitle("Log in to continue."), form, login, statusLabel());
        root.setStyle(Ui.CARD_STYLE);
        root.setMaxWidth(400);
        root.setMaxHeight(Region.USE_PREF_SIZE);
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
