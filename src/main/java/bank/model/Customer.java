package bank.model;

import bank.exception.AccountRuleException;
import bank.exception.BankException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An individual or a company that holds one or more accounts (FR-01, FR-10).
 * Individuals have a first name and surname; companies have a company name.
 */
public class Customer {

    private final String customerId;
    private String firstName;
    private String surname;
    private String companyName;
    private String address;
    private String phone;
    private String email;
    private boolean employed;
    private final List<Account> accounts = new ArrayList<>();

    private Customer(String customerId, String firstName, String surname, String companyName,
                     String address, String phone, String email, boolean employed) {
        this.customerId = customerId;
        this.firstName = firstName;
        this.surname = surname;
        this.companyName = companyName;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.employed = employed;
    }

    /** Registers an individual with a generated customer ID (FR-01, FR-02). */
    public static Customer individual(String firstName, String surname, String address,
                                      String phone, String email, boolean employed) {
        return individual(IdGenerator.nextCustomerId(), firstName, surname, address, phone, email, employed);
    }

    /** Overload with a given ID, used when loading from the database. */
    public static Customer individual(String customerId, String firstName, String surname, String address,
                                      String phone, String email, boolean employed) {
        require(firstName, "first name");
        require(surname, "surname");
        require(address, "address");
        return new Customer(customerId, firstName, surname, null, address, phone, email, employed);
    }

    /** Registers a company with a generated customer ID (FR-01, FR-02). */
    public static Customer company(String companyName, String address, String phone, String email) {
        return company(IdGenerator.nextCustomerId(), companyName, address, phone, email);
    }

    /** Overload with a given ID, used when loading from the database. */
    public static Customer company(String customerId, String companyName, String address,
                                   String phone, String email) {
        require(companyName, "company name");
        require(address, "address");
        return new Customer(customerId, null, null, companyName, address, phone, email, false);
    }

    private static void require(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BankException("The " + field + " is required.");
        }
    }

    /** Updates contact details (FR-03). The ID and name cannot be changed here. */
    public void updateContact(String address, String phone, String email) {
        require(address, "address");
        this.address = address;
        this.phone = phone;
        this.email = email;
    }

    public String getFullName() {
        return isCompany() ? companyName : firstName + " " + surname;
    }

    public boolean isCompany() {
        return companyName != null;
    }

    /** Adds an account this customer holds. The account must name this customer as its holder. */
    public void addAccount(Account account) {
        if (account.getHolder() != this) {
            throw new AccountRuleException("That account is held by a different customer.");
        }
        if (accounts.contains(account)) {
            throw new AccountRuleException("Account " + account.getAccountNumber() + " is already added.");
        }
        accounts.add(account);
    }

    /** The customer's accounts. The list cannot be changed from outside. */
    public List<Account> getAccounts() {
        return Collections.unmodifiableList(accounts);
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getSurname() {
        return surname;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public boolean isEmployed() {
        return employed;
    }

    public void setEmployed(boolean employed) {
        this.employed = employed;
    }

    @Override
    public String toString() {
        return customerId + " " + getFullName();
    }
}
