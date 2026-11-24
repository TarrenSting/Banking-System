package bank.view;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Two tabs: "Find and update" (search, pick a customer, change contact details) and
 * "Register new" (a short form that only shows the fields that apply). Use cases: Register
 * Customer, Update Customer Details. Every decision is left to the controller.
 */
public class CustomerView extends BaseView {

    /** Implemented by the customer controller. */
    public interface Listener {
        void onRegister(ViewData.CustomerForm form);

        void onUpdate(String customerId, ViewData.CustomerForm form);

        void onSearch(String text);
    }

    private final VBox root;
    private final ComboBox<String> kind = new ComboBox<>();
    private final Label firstNameLabel = new Label("First name");
    private final Label surnameLabel = new Label("Surname");
    private final Label companyLabel = new Label("Company name");
    private final TextField firstName = new TextField();
    private final TextField surname = new TextField();
    private final TextField companyName = new TextField();
    private final TextField address = new TextField();
    private final TextField phone = new TextField();
    private final TextField email = new TextField();
    private final CheckBox employed = new CheckBox("Currently employed (needed for a Cheque account)");

    private final TextField searchText = new TextField();
    private final TableView<ViewData.CustomerRow> table = new TableView<>();
    private final Label selected = new Label("Select a customer in the table to change their contact details.");
    private final TextField editAddress = new TextField();
    private final TextField editPhone = new TextField();
    private final TextField editEmail = new TextField();
    private final Button save = Ui.warning("Save Changes");
    private String selectedId;
    private Listener listener;

    public CustomerView() {
        TabPane tabs = new TabPane();
        Tab find = new Tab("Find and update", buildFindTab());
        Tab register = new Tab("Register new", buildRegisterTab());
        find.setClosable(false);
        register.setClosable(false);
        tabs.getTabs().add(find);
        tabs.getTabs().add(register);

        root = Ui.page(Ui.heading("Customers"),
                Ui.subtitle("Search for a customer to change their details, or register a new one."),
                Ui.card(tabs), statusLabel());
    }

    private VBox buildFindTab() {
        searchText.setPromptText("Surname or company name (leave empty to show everyone)");
        searchText.setPrefWidth(380);
        searchText.setOnAction(event -> search());
        Button search = Ui.primary("Search");
        search.setOnAction(event -> search());

        table.getColumns().add(Ui.<ViewData.CustomerRow>column("ID", ViewData.CustomerRow::id, 90));
        table.getColumns().add(Ui.<ViewData.CustomerRow>column("Name", ViewData.CustomerRow::name, 200));
        table.getColumns().add(Ui.<ViewData.CustomerRow>column("Phone", ViewData.CustomerRow::phone, 110));
        table.getColumns().add(Ui.<ViewData.CustomerRow>column("Email", ViewData.CustomerRow::email, 200));
        table.setPrefHeight(220);
        table.getSelectionModel().selectedItemProperty().addListener((observable, oldRow, row) -> {
            if (row != null) {
                selectedId = row.id();
                selected.setText("Editing " + row.id() + " - " + row.name());
                editAddress.setText(row.address());
                editPhone.setText(row.phone());
                editEmail.setText(row.email());
                save.setDisable(false);
            }
        });

        save.setDisable(true);
        save.setOnAction(event -> {
            if (selectedId == null) {
                showError("Select a customer in the table first.");
            } else if (listener != null) {
                listener.onUpdate(selectedId, new ViewData.CustomerForm(false, "", "", "",
                        Ui.text(editAddress), Ui.text(editPhone), Ui.text(editEmail), false));
            }
        });

        GridPane edit = Ui.grid();
        Ui.addRow(edit, 0, "Address", editAddress);
        Ui.addRow(edit, 1, "Phone", editPhone);
        Ui.addRow(edit, 2, "Email", editEmail);

        VBox box = new VBox(12, new HBox(10, searchText, search), table, selected, edit, save);
        box.setStyle("-fx-padding: 12;");
        return box;
    }

    private VBox buildRegisterTab() {
        kind.getItems().addAll("Individual", "Company");
        kind.setValue("Individual");
        kind.valueProperty().addListener((observable, oldValue, newValue) -> applyKind());

        GridPane form = Ui.grid();
        form.add(new Label("Customer type"), 0, 0);
        form.add(kind, 1, 0);
        form.add(firstNameLabel, 0, 1);
        form.add(firstName, 1, 1);
        form.add(surnameLabel, 0, 2);
        form.add(surname, 1, 2);
        form.add(companyLabel, 0, 3);
        form.add(companyName, 1, 3);
        Ui.addRow(form, 4, "Address", address);
        Ui.addRow(form, 5, "Phone", phone);
        Ui.addRow(form, 6, "Email", email);
        form.add(employed, 1, 7);
        applyKind();

        Button register = Ui.success("Register Customer");
        register.setOnAction(event -> {
            if (listener != null) {
                listener.onRegister(readForm());
            }
        });
        Button clear = Ui.button("Clear");
        clear.setOnAction(event -> clearForm());

        VBox box = new VBox(12, form, new HBox(10, register, clear));
        box.setStyle("-fx-padding: 12;");
        return box;
    }

    private void search() {
        if (listener != null) {
            listener.onSearch(Ui.text(searchText));
        }
    }

    /** Shows only the fields that apply to the chosen customer type. */
    private void applyKind() {
        boolean company = "Company".equals(kind.getValue());
        show(firstNameLabel, !company);
        show(firstName, !company);
        show(surnameLabel, !company);
        show(surname, !company);
        show(employed, !company);
        show(companyLabel, company);
        show(companyName, company);
    }

    private static void show(javafx.scene.Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private ViewData.CustomerForm readForm() {
        return new ViewData.CustomerForm(
                "Company".equals(kind.getValue()),
                Ui.text(firstName), Ui.text(surname), Ui.text(companyName),
                Ui.text(address), Ui.text(phone), Ui.text(email),
                employed.isSelected());
    }

    /** Empties the registration form. */
    public void clearForm() {
        firstName.clear();
        surname.clear();
        companyName.clear();
        address.clear();
        phone.clear();
        email.clear();
        employed.setSelected(false);
    }

    public void showCustomers(List<ViewData.CustomerRow> rows) {
        table.getItems().setAll(rows);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public Parent getRoot() {
        return root;
    }
}
