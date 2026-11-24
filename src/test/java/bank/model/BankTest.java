package bank.model;

import bank.exception.AccountRuleException;
import bank.exception.BankException;
import bank.exception.InvalidAmountException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BankTest {

    private static final YearMonth OCTOBER = YearMonth.of(2026, 10);

    private Bank bank;
    private Customer kabo;
    private Customer company;

    private static BigDecimal bwp(String value) {
        return new BigDecimal(value);
    }

    @BeforeEach
    void setUp() {
        bank = new Bank("Gaborone Main");
        kabo = bank.registerCustomer(
                Customer.individual("Kabo", "Molefe", "Plot 12, Gaborone", "71234567", "kabo@example.com", true));
        company = bank.registerCustomer(
                Customer.company("Kalahari Traders", "Maun", "73456789", "info@kt.co.bw"));
    }

    @Test
    void savingsOpeningRequiresAFirstDeposit() {
        SavingsAccount savings = bank.openSavings(kabo.getCustomerId(), bwp("100"));
        assertEquals(bwp("100.00"), savings.getBalance());
        assertThrows(InvalidAmountException.class, () -> bank.openSavings(kabo.getCustomerId(), bwp("0")));
        assertThrows(InvalidAmountException.class, () -> bank.openSavings(kabo.getCustomerId(), null));
    }

    @Test
    void investmentOpeningChecksTheMinimum() {
        assertThrows(AccountRuleException.class, () -> bank.openInvestment(kabo.getCustomerId(), bwp("499.99")));
        assertEquals(0, kabo.getAccounts().size(), "a refused opening leaves nothing behind");
        assertNotNull(bank.openInvestment(kabo.getCustomerId(), bwp("500")));
    }

    @Test
    void chequeOpeningRecordsEmployerDetails() {
        ChequeAccount cheque = bank.openCheque(kabo.getCustomerId(), "Debswana", "Jwaneng");
        assertEquals("Debswana", cheque.getEmployerName());
        assertThrows(AccountRuleException.class, () -> bank.openCheque(company.getCustomerId(), "X", "Y"));
    }

    @Test
    void customerHoldsSeveralAccountsAndTheyCanBeFoundByNumber() {
        Account savings = bank.openSavings(kabo.getCustomerId(), bwp("100"));
        Account investment = bank.openInvestment(kabo.getCustomerId(), bwp("1000"));
        assertEquals(2, bank.accountsOf(kabo.getCustomerId()).size());
        assertSame(savings, bank.findAccount(savings.getAccountNumber()));
        assertSame(investment, bank.findAccount(investment.getAccountNumber()));
        assertThrows(BankException.class, () -> bank.findAccount("ACC9999999"));
    }

    @Test
    void companiesCanBeFoundByName() {
        List<Customer> found = bank.searchCustomers("kal");
        assertEquals(List.of(company), found);
        assertEquals(List.of(kabo), bank.searchCustomers("Mol"));
    }

    @Test
    void depositGoesThroughTheBank() {
        Account savings = bank.openSavings(kabo.getCustomerId(), bwp("100"));
        bank.deposit(savings.getAccountNumber(), bwp("50"));
        assertEquals(bwp("150.00"), savings.getBalance());
    }

    @Test
    void withdrawalFromSavingsIsRejectedAndNothingChanges() {
        Account savings = bank.openSavings(kabo.getCustomerId(), bwp("100"));
        assertThrows(AccountRuleException.class, () -> bank.withdraw(savings.getAccountNumber(), bwp("10")));
        assertEquals(bwp("100.00"), savings.getBalance());
        assertEquals(1, savings.getTransactions().size());
    }

    @Test
    void withdrawalWorksOnInvestmentAndCheque() {
        Account investment = bank.openInvestment(kabo.getCustomerId(), bwp("1000"));
        Account cheque = bank.openCheque(kabo.getCustomerId(), "Debswana", "Jwaneng");
        bank.deposit(cheque.getAccountNumber(), bwp("300"));
        bank.withdraw(investment.getAccountNumber(), bwp("200"));
        bank.withdraw(cheque.getAccountNumber(), bwp("100"));
        assertEquals(bwp("800.00"), investment.getBalance());
        assertEquals(bwp("200.00"), cheque.getBalance());
    }

    @Test
    void monthlyRunPaysSavingsAndInvestmentOnly() {
        Account savings = bank.openSavings(kabo.getCustomerId(), bwp("2000"));
        Account investment = bank.openInvestment(kabo.getCustomerId(), bwp("1000"));
        Account cheque = bank.openCheque(kabo.getCustomerId(), "Debswana", "Jwaneng");
        bank.deposit(cheque.getAccountNumber(), bwp("5000"));

        List<Account> paid = bank.payMonthlyInterest(OCTOBER);

        assertEquals(2, paid.size());
        assertEquals(bwp("2001.00"), savings.getBalance());
        assertEquals(bwp("1050.00"), investment.getBalance());
        assertEquals(bwp("5000.00"), cheque.getBalance());
    }

    @Test
    void runningTheMonthlyRunTwiceNeverPaysTwice() {
        Account investment = bank.openInvestment(kabo.getCustomerId(), bwp("1000"));
        bank.payMonthlyInterest(OCTOBER);
        List<Account> second = bank.payMonthlyInterest(OCTOBER);
        assertTrue(second.isEmpty());
        assertEquals(bwp("1050.00"), investment.getBalance());
    }

    @Test
    void unknownCustomerIsRefused() {
        assertThrows(BankException.class, () -> bank.openSavings("C99999", bwp("10")));
    }

    @Test
    void sameCustomerCannotBeRegisteredTwice() {
        assertThrows(BankException.class, () -> bank.registerCustomer(kabo));
    }
}
