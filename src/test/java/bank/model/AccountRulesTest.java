package bank.model;

import bank.exception.AccountRuleException;
import bank.exception.InsufficientFundsException;
import bank.exception.InvalidAmountException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AccountRulesTest {

    private Customer worker;
    private Customer unemployed;

    private static BigDecimal bwp(String value) {
        return new BigDecimal(value);
    }

    @BeforeEach
    void setUp() {
        worker = Customer.individual("Kabo", "Molefe", "Plot 12, Gaborone", "71234567", "kabo@example.com", true);
        unemployed = Customer.individual("Neo", "Sebina", "Plot 9, Gaborone", "72345678", "neo@example.com", false);
    }

    // ---- Savings (FR-11) ----

    @Test
    void savingsAcceptsDeposits() {
        SavingsAccount savings = new SavingsAccount(worker, "Main");
        savings.deposit(bwp("250"));
        assertEquals(bwp("250.00"), savings.getBalance());
    }

    @Test
    void savingsHasNoWithdrawMethod() {
        SavingsAccount savings = new SavingsAccount(worker, "Main");
        assertFalse(savings instanceof Withdrawable);
    }

    // ---- Investment (FR-12) ----

    @Test
    void investmentOpensWithMinimumDeposit() {
        InvestmentAccount investment = new InvestmentAccount(worker, "Main", bwp("500.00"));
        assertEquals(bwp("500.00"), investment.getBalance());
        assertEquals(1, investment.getTransactions().size());
        assertEquals(TransactionType.DEPOSIT, investment.getTransactions().get(0).getType());
    }

    @Test
    void investmentRefusesOpeningBelowMinimum() {
        assertThrows(AccountRuleException.class, () -> new InvestmentAccount(worker, "Main", bwp("300.00")));
        assertThrows(AccountRuleException.class, () -> new InvestmentAccount(worker, "Main", bwp("499.99")));
        assertThrows(AccountRuleException.class, () -> new InvestmentAccount(worker, "Main", null));
    }

    @Test
    void investmentAllowsWithdrawal() {
        InvestmentAccount investment = new InvestmentAccount(worker, "Main", bwp("1000"));
        investment.withdraw(bwp("400"));
        assertEquals(bwp("600.00"), investment.getBalance());
    }

    // ---- Cheque (FR-13) ----

    @Test
    void chequeOpensForWorkingCustomer() {
        ChequeAccount cheque = new ChequeAccount(worker, "Main", "Debswana", "Jwaneng");
        assertEquals("Debswana", cheque.getEmployerName());
        assertEquals("Jwaneng", cheque.getEmployerAddress());
        assertEquals(bwp("0.00"), cheque.getBalance());
    }

    @Test
    void chequeRefusedForCustomerWhoIsNotWorking() {
        assertThrows(AccountRuleException.class,
                () -> new ChequeAccount(unemployed, "Main", "Debswana", "Jwaneng"));
    }

    @Test
    void chequeNeedsEmployerDetails() {
        assertThrows(AccountRuleException.class, () -> new ChequeAccount(worker, "Main", "", "Jwaneng"));
        assertThrows(AccountRuleException.class, () -> new ChequeAccount(worker, "Main", "Debswana", null));
    }

    @Test
    void chequeAllowsDepositAndWithdrawal() {
        ChequeAccount cheque = new ChequeAccount(worker, "Main", "Debswana", "Jwaneng");
        cheque.deposit(bwp("8000"), "Salary");
        cheque.withdraw(bwp("2500.50"));
        assertEquals(bwp("5499.50"), cheque.getBalance());
        assertEquals("Salary", cheque.getTransactions().get(0).getReference());
    }

    // ---- Deposits and withdrawals (FR-14, FR-15, FR-16, FR-17, FR-18) ----

    @Test
    void depositMustBeGreaterThanZero() {
        SavingsAccount savings = new SavingsAccount(worker, "Main");
        assertThrows(InvalidAmountException.class, () -> savings.deposit(bwp("0")));
        assertThrows(InvalidAmountException.class, () -> savings.deposit(bwp("-5")));
        assertThrows(InvalidAmountException.class, () -> savings.deposit(null));
        assertEquals(bwp("0.00"), savings.getBalance());
        assertTrue(savings.getTransactions().isEmpty());
    }

    @Test
    void withdrawalCannotTakeBalanceBelowZero() {
        ChequeAccount cheque = new ChequeAccount(worker, "Main", "Debswana", "Jwaneng");
        cheque.deposit(bwp("100"));
        assertThrows(InsufficientFundsException.class, () -> cheque.withdraw(bwp("100.01")));
        assertEquals(bwp("100.00"), cheque.getBalance());
        assertEquals(1, cheque.getTransactions().size());
    }

    @Test
    void withdrawalAmountMustBeGreaterThanZero() {
        InvestmentAccount investment = new InvestmentAccount(worker, "Main", bwp("500"));
        assertThrows(InvalidAmountException.class, () -> investment.withdraw(bwp("0")));
    }

    @Test
    void amountsAreRoundedHalfUpToTwoDecimals() {
        SavingsAccount savings = new SavingsAccount(worker, "Main");
        savings.deposit(bwp("10.005"));
        assertEquals(bwp("10.01"), savings.getBalance());
    }

    @Test
    void everyTransactionRecordsTypeAmountAndBalanceAfter() {
        InvestmentAccount investment = new InvestmentAccount(worker, "Main", bwp("1000"));
        investment.withdraw(bwp("250"));
        Transaction last = investment.getTransactions().get(1);
        assertEquals(TransactionType.WITHDRAWAL, last.getType());
        assertEquals(bwp("250.00"), last.getAmount());
        assertEquals(bwp("750.00"), last.getBalanceAfter());
        assertEquals(investment.getAccountNumber(), last.getAccountNumber());
        assertNotNull(last.getTime());
        assertNotNull(last.getTransactionId());
    }

    @Test
    void transactionListCannotBeEditedFromOutside() {
        SavingsAccount savings = new SavingsAccount(worker, "Main");
        savings.deposit(bwp("10"));
        assertThrows(UnsupportedOperationException.class, () -> savings.getTransactions().clear());
    }

    // ---- Holders and multiple accounts (FR-09, FR-10) ----

    @Test
    void accountCannotExistWithoutAHolder() {
        assertThrows(AccountRuleException.class, () -> new SavingsAccount(null, "Main"));
    }

    @Test
    void accountNumbersAreUnique() {
        SavingsAccount a = new SavingsAccount(worker, "Main");
        SavingsAccount b = new SavingsAccount(worker, "Main");
        assertNotEquals(a.getAccountNumber(), b.getAccountNumber());
    }

    @Test
    void customerCanHoldAllThreeAccountTypes() {
        worker.addAccount(new SavingsAccount(worker, "Main"));
        worker.addAccount(new InvestmentAccount(worker, "Main", bwp("500")));
        worker.addAccount(new ChequeAccount(worker, "Main", "Debswana", "Jwaneng"));
        assertEquals(3, worker.getAccounts().size());
    }

    @Test
    void customerCannotAddAnotherCustomersAccount() {
        SavingsAccount theirs = new SavingsAccount(unemployed, "Main");
        assertThrows(AccountRuleException.class, () -> worker.addAccount(theirs));
    }

    @Test
    void polymorphismWithdrawalOnlyWhereAllowed() {
        Account[] accounts = {
                new SavingsAccount(worker, "Main"),
                new InvestmentAccount(worker, "Main", bwp("500")),
                new ChequeAccount(worker, "Main", "Debswana", "Jwaneng")
        };
        int withdrawable = 0;
        for (Account account : accounts) {
            if (account instanceof Withdrawable) {
                withdrawable++;
            }
        }
        assertEquals(2, withdrawable);
        assertEquals("Savings", accounts[0].getAccountType());
        assertEquals("Investment", accounts[1].getAccountType());
        assertEquals("Cheque", accounts[2].getAccountType());
    }
}
