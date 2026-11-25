package bank.view;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.VBox;

import java.util.function.Function;

/** Small layout helpers shared by the views, so every screen looks the same (NFR-04). */
final class Ui {

    static final String NAVY = "#0d47a1";
    static final String BLUE = "#1976d2";
    static final String GREEN = "#2e7d32";
    static final String ORANGE = "#ef6c00";
    static final String PURPLE = "#6a1b9a";
    static final String TEAL = "#00897b";
    static final String RED = "#c62828";
    static final String SLATE = "#455a64";

    static final String DEEP = "#0a1f5c";
    static final String ACCENT = "#1f6feb";
    static final String PAGE_BG = "#c3d5f2";
    static final String BANK_NAME = "Unity Bank";

    /** Pale blue page; -fx-base tints every control and table header. */
    static final String PAGE_STYLE = "-fx-base: #b4c9ec; -fx-accent: #1f6feb; -fx-focus-color: #1f6feb;"
            + " -fx-background-color: " + PAGE_BG + ";";

    /** White card with a shadow, used for the login box. */
    static final String CARD_STYLE = "-fx-base: #d6e8fa; -fx-accent: #1976d2; -fx-focus-color: #1976d2;"
            + " -fx-background-color: white; -fx-background-radius: 14; -fx-padding: 28;"
            + " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 24, 0.2, 0, 8);";

    private Ui() {
    }

    static Label heading(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + NAVY + ";");
        return label;
    }

    /** Plain white button with a blue outline, for secondary actions. */
    static Button button(String text) {
        Button button = new Button(text);
        button.setMinWidth(120);
        String base = "-fx-background-color: white; -fx-text-fill: " + NAVY + "; -fx-font-weight: bold;"
                + " -fx-border-color: #90caf9; -fx-border-radius: 6; -fx-background-radius: 6;"
                + " -fx-padding: 7 14 7 14; -fx-cursor: hand;";
        button.setStyle(base);
        button.setOnMouseEntered(event -> button.setStyle(base + " -fx-background-color: #e3f2fd;"));
        button.setOnMouseExited(event -> button.setStyle(base));
        return button;
    }

    /** A solid coloured button with white bold text. */
    static Button colored(String text, String color) {
        return colored(text, color, "");
    }

    /** Same as {@link #colored(String, String)} with extra style text such as a bigger font. */
    static Button colored(String text, String color, String extraStyle) {
        Button button = new Button(text);
        button.setMinWidth(120);
        String base = "-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold;"
                + " -fx-background-radius: 6; -fx-padding: 7 14 7 14; -fx-cursor: hand; " + extraStyle;
        button.setStyle(base);
        button.setOnMouseEntered(event -> button.setStyle(base + " -fx-opacity: 0.85;"));
        button.setOnMouseExited(event -> button.setStyle(base));
        return button;
    }

    static Button primary(String text) {
        return colored(text, BLUE);
    }

    static Button success(String text) {
        return colored(text, GREEN);
    }

    static Button warning(String text) {
        return colored(text, ORANGE);
    }

    static Button danger(String text) {
        return colored(text, RED);
    }

    /** White rounded panel with a soft shadow that groups one part of a screen. */
    static VBox card(Node... children) {
        VBox box = new VBox(10, children);
        box.setPadding(new Insets(14));
        box.setStyle("-fx-background-color: #e4edfb; -fx-background-radius: 12;"
                + " -fx-border-color: #a9bfe6; -fx-border-radius: 12;"
                + " -fx-effect: dropshadow(gaussian, rgba(10,31,92,0.25), 12, 0.1, 0, 3);");
        return box;
    }

    /** One grey line under a heading that says what the screen is for. */
    static Label subtitle(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle("-fx-font-size: 13px; -fx-text-fill: #455a64;");
        return label;
    }

    /** A big number with a caption, e.g. "12" over "Customers". */
    static VBox stat(String value, String caption, String color) {
        Label number = new Label(value);
        number.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label text = new Label(caption);
        text.setStyle("-fx-font-size: 12px; -fx-text-fill: #607d8b;");
        VBox box = new VBox(2, number, text);
        box.setPadding(new Insets(10, 18, 10, 18));
        box.setMinWidth(150);
        box.setStyle("-fx-background-color: #e4edfb; -fx-background-radius: 12;"
                + " -fx-border-color: " + color + "; -fx-border-width: 0 0 0 5; -fx-border-radius: 12;"
                + " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0.1, 0, 2);");
        return box;
    }

    /** A white bank-building icon (roof, four pillars, base) drawn with shapes, so it needs no image file. */
    static VBox logo(double scale) {
        Polygon roof = new Polygon(0, 18 * scale, 30 * scale, 0, 60 * scale, 18 * scale);
        roof.setStyle("-fx-fill: white;");
        HBox pillars = new HBox(5 * scale);
        pillars.setAlignment(Pos.CENTER);
        for (int i = 0; i < 4; i++) {
            Rectangle pillar = new Rectangle(8 * scale, 22 * scale);
            pillar.setStyle("-fx-fill: white;");
            pillars.getChildren().add(pillar);
        }
        Rectangle base = new Rectangle(60 * scale, 5 * scale);
        base.setStyle("-fx-fill: white;");
        VBox box = new VBox(3 * scale, roof, pillars, base);
        box.setAlignment(Pos.CENTER);
        box.setMaxWidth(60 * scale);
        return box;
    }

    static GridPane grid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        return grid;
    }

    static void addRow(GridPane grid, int row, String label, Node field) {
        grid.add(new Label(label), 0, row);
        grid.add(field, 1, row);
    }

    static VBox page(Node... children) {
        VBox box = new VBox(12, children);
        box.setPadding(new Insets(16));
        box.setStyle(PAGE_STYLE);
        return box;
    }

    static <T> TableColumn<T, String> column(String title, Function<T, String> value, double width) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        column.setPrefWidth(width);
        return column;
    }

    /** The trimmed text of a field, never null. */
    static String text(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }
}
