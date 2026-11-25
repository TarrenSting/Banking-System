package bank.view;

import javafx.scene.Parent;
import javafx.scene.control.Label;

/**
 * Common part of every screen: a root node and one status line for messages.
 * Views only display what a controller tells them to; they never decide anything.
 */
public abstract class BaseView {

    private final Label status = new Label();
    private boolean darkBackground;

    protected BaseView() {
        status.setWrapText(true);
    }

    /** The node to put in a scene. */
    public abstract Parent getRoot();

    /** The status line, for subclasses to place in their layout. */
    protected Label statusLabel() {
        return status;
    }

    /** Call for a screen with a dark background, so messages use lighter colours. */
    protected void useLightMessages() {
        darkBackground = true;
    }

    /** Shows a success or information message in green. */
    public void showMessage(String text) {
        status.setStyle("-fx-text-fill: " + (darkBackground ? "#7ee2a0" : "#1b6e2f") + "; -fx-font-weight: bold;");
        status.setText(text);
    }

    /** Shows an error message in red. */
    public void showError(String text) {
        status.setStyle("-fx-text-fill: " + (darkBackground ? "#ff9a9a" : "#b3261e") + "; -fx-font-weight: bold;");
        status.setText(text);
    }

    public void clearStatus() {
        status.setText("");
    }
}
