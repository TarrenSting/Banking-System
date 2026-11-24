package bank.model;

import bank.exception.BankException;
import bank.security.PasswordHasher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerAndUserTest {

    // ---- Customer (FR-01 to FR-03) ----

    @Test
    void individualGetsAUniqueId() {
        Customer a = Customer.individual("Kabo", "Molefe", "Gaborone", "71234567", "a@x.com", true);
        Customer b = Customer.individual("Neo", "Sebina", "Francistown", "72345678", "b@x.com", false);
        assertNotEquals(a.getCustomerId(), b.getCustomerId());
        assertEquals("Kabo Molefe", a.getFullName());
        assertFalse(a.isCompany());
    }

    @Test
    void companyUsesItsCompanyName() {
        Customer company = Customer.company("Kalahari Traders", "Maun", "73456789", "info@kt.co.bw");
        assertTrue(company.isCompany());
        assertEquals("Kalahari Traders", company.getFullName());
    }

    @Test
    void registrationRejectsMissingNameOrAddress() {
        assertThrows(BankException.class, () -> Customer.individual("", "Molefe", "Gaborone", "1", "a@x.com", true));
        assertThrows(BankException.class, () -> Customer.individual("Kabo", "Molefe", " ", "1", "a@x.com", true));
        assertThrows(BankException.class, () -> Customer.company(null, "Maun", "1", "a@x.com"));
    }

    @Test
    void contactDetailsCanBeUpdatedButNotTheId() {
        Customer c = Customer.individual("Kabo", "Molefe", "Gaborone", "71234567", "a@x.com", true);
        String id = c.getCustomerId();
        c.updateContact("Lobatse", "79999999", "new@x.com");
        assertEquals("Lobatse", c.getAddress());
        assertEquals("79999999", c.getPhone());
        assertEquals("new@x.com", c.getEmail());
        assertEquals(id, c.getCustomerId());
    }

    // ---- User and passwords (FR-05, FR-06, NFR-01) ----

    @Test
    void passwordIsNeverStoredInPlainText() {
        User user = new User("teller1", "S3cret!pw", Role.TELLER);
        assertFalse(user.getPasswordHash().contains("S3cret!pw"));
        assertTrue(user.getPasswordHash().contains(":"));
    }

    @Test
    void samePasswordGivesADifferentHashEachTime() {
        assertNotEquals(PasswordHasher.hash("abc12345"), PasswordHasher.hash("abc12345"));
    }

    @Test
    void correctPasswordLogsIn() {
        User user = new User("teller1", "S3cret!pw", Role.TELLER);
        assertTrue(user.login("S3cret!pw"));
        assertTrue(user.isLoggedIn());
        user.logout();
        assertFalse(user.isLoggedIn());
    }

    @Test
    void wrongPasswordDoesNotLogIn() {
        User user = new User("teller1", "S3cret!pw", Role.TELLER);
        assertFalse(user.login("wrong"));
        assertFalse(user.isLoggedIn());
        assertTrue(user.login("S3cret!pw"), "a wrong attempt does not block the right password");
    }

    @Test
    void userRebuiltFromStoredHashStillLogsIn() {
        User original = new User("kabo", "pw123456", Role.CUSTOMER);
        User loaded = User.fromStored("kabo", original.getPasswordHash(), Role.CUSTOMER);
        assertTrue(loaded.login("pw123456"));
        assertFalse(loaded.login("other"));
    }

    @Test
    void customerLoginCanBeLinkedToACustomer() {
        Customer c = Customer.individual("Kabo", "Molefe", "Gaborone", "71234567", "a@x.com", true);
        User user = new User("kabo", "pw123456", Role.CUSTOMER);
        user.setCustomer(c);
        assertSame(c, user.getCustomer());
    }
}
