package bank.controller;

import bank.exception.BankException;
import bank.model.Bank;
import bank.model.Customer;
import bank.view.CustomerView;
import bank.view.ViewData;

import java.util.List;

/** Handles Register Customer, Update Customer Details and customer search (tellers only). */
public class CustomerController implements CustomerView.Listener {

    private final CustomerView view;
    private final Session session;

    public CustomerController(CustomerView view, Session session) {
        this.view = view;
        this.session = session;
    }

    /** Fills the table with every customer when the screen opens. */
    public void start() {
        onSearch("");
    }

    @Override
    public void onRegister(ViewData.CustomerForm form) {
        run(() -> {
            session.requireTeller();
            Customer customer = form.company()
                    ? Customer.company(form.companyName(), form.address(), form.phone(), form.email())
                    : Customer.individual(form.firstName(), form.surname(), form.address(), form.phone(),
                            form.email(), form.employed());
            bank().registerCustomer(customer);
            view.clearForm();
            show(bank().getCustomers());
            view.showMessage("Registered " + customer.getFullName() + " with customer ID "
                    + customer.getCustomerId() + ".");
        });
    }

    @Override
    public void onUpdate(String customerId, ViewData.CustomerForm form) {
        run(() -> {
            session.requireTeller();
            Customer customer = bank().updateCustomer(customerId, form.address(), form.phone(), form.email());
            show(bank().getCustomers());
            view.showMessage("Updated the contact details of " + customer.getFullName() + ".");
        });
    }

    @Override
    public void onSearch(String text) {
        run(() -> {
            session.requireTeller();
            List<Customer> found = bank().searchCustomers(text);
            show(found);
            view.showMessage(found.isEmpty() ? "No customers found."
                    : found.size() + " customer(s) shown.");
        });
    }

    private void show(List<Customer> customers) {
        view.showCustomers(customers.stream().map(Rows::customer).toList());
    }

    private Bank bank() {
        return session.getBank();
    }

    private void run(Runnable action) {
        try {
            action.run();
        } catch (BankException e) {
            view.showError(e.getMessage());
        }
    }
}
