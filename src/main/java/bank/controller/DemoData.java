package bank.controller;

import bank.dao.UserDAO;
import bank.model.Bank;
import bank.model.Customer;
import bank.model.Role;
import bank.model.User;

import java.math.BigDecimal;

/** Sample customers, accounts and logins so the screens have something to show until a database is added. */
final class DemoData {

    static final String LOGIN_HINT = "Demo logins:  teller1 / teller123   or   customer1 / customer123";

    private DemoData() {
    }

    static void load(Bank bank, UserDAO users) {
        Customer kabo = bank.registerCustomer(Customer.individual(
                "Kabo", "Molefe", "Plot 123, Gaborone", "71234567", "kabo@example.com", true));
        Customer traders = bank.registerCustomer(Customer.company(
                "Kalahari Traders", "Maun", "73456789", "info@kalahari.example"));

        bank.openSavings(kabo.getCustomerId(), new BigDecimal("2000.00"));
        bank.openInvestment(kabo.getCustomerId(), new BigDecimal("1000.00"));
        bank.openCheque(kabo.getCustomerId(), "Demo Employer Ltd", "Gaborone");
        bank.openSavings(traders.getCustomerId(), new BigDecimal("5000.00"));

        users.save(new User("teller1", "teller123", Role.TELLER));
        User customerLogin = new User("customer1", "customer123", Role.CUSTOMER);
        customerLogin.setCustomer(kabo);
        users.save(customerLogin);
        User companyLogin = new User("customer2", "customer123", Role.CUSTOMER);
        companyLogin.setCustomer(traders);
        users.save(companyLogin);
    }
}
