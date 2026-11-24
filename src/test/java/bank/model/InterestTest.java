package bank.model;

import bank.exception.AccountRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InterestTest {

    private static final YearMonth OCTOBER = YearMonth.of(2026, 10);
    private static final YearMonth NOVEMBER = YearMonth.of(2026, 11);

    private Customer customer;

    private static BigDecimal bwp(String value) {
        return new BigDecimal(value);
    }

    @BeforeEach
    void setUp() {
        customer = Customer.individual("Kabo", "Molefe", "Plot 12, Gaborone", "71234567", "kabo@example.com", true);
    }

    @Test
    void investmentEarnsFivePercentAMonth() {
        InvestmentAccount investment = new InvestmentAccount(customer, "Main", bwp("1000"));
        assertEquals(bwp("50.00"), investment.calculateInterest());
        investment.payInterest(OCTOBER);
        assertEquals(bwp("1050.00"), investment.getBalance());
    }

    @Test
    void savingsEarnsPointZeroFivePercentAMonth() {
        SavingsAccount savings = new SavingsAccount(customer, "Main");
        savings.deposit(bwp("2000"));
        assertEquals(bwp("1.00"), savings.calculateInterest());
        savings.payInterest(OCTOBER);
        assertEquals(bwp("2001.00"), savings.getBalance());
    }

    @Test
    void interestIsRecordedAsItsOwnTransaction() {
        InvestmentAccount investment = new InvestmentAccount(customer, "Main", bwp("1000"));
        investment.payInterest(OCTOBER);
        Transaction last = investment.getTransactions().get(1);
        assertEquals(TransactionType.INTEREST, last.getType());
        assertEquals(bwp("50.00"), last.getAmount());
        assertEquals(bwp("1050.00"), last.getBalanceAfter());
    }

    @Test
    void interestIsRoundedHalfUp() {
        SavingsAccount savings = new SavingsAccount(customer, "Main");
        savings.deposit(bwp("1010"));
        // 1010 x 0.0005 = 0.505 -> 0.51
        assertEquals(bwp("0.51"), savings.calculateInterest());
    }

    @Test
    void accountIsNeverPaidTwiceInTheSameMonth() {
        InvestmentAccount investment = new InvestmentAccount(customer, "Main", bwp("1000"));
        investment.payInterest(OCTOBER);
        assertThrows(AccountRuleException.class, () -> investment.payInterest(OCTOBER));
        assertEquals(bwp("1050.00"), investment.getBalance());
        investment.payInterest(NOVEMBER);
        assertEquals(bwp("1102.50"), investment.getBalance());
    }

    @Test
    void emptySavingsAccountEarnsNothingAndRecordsNoTransaction() {
        SavingsAccount savings = new SavingsAccount(customer, "Main");
        savings.payInterest(OCTOBER);
        assertEquals(bwp("0.00"), savings.getBalance());
        assertTrue(savings.getTransactions().isEmpty());
    }

    @Test
    void chequeAccountEarnsNoInterest() {
        ChequeAccount cheque = new ChequeAccount(customer, "Main", "Debswana", "Jwaneng");
        assertFalse(cheque instanceof InterestBearing);
    }

    @Test
    void monthlyRunWorksOnTheInterfaceOnly() {
        SavingsAccount savings = new SavingsAccount(customer, "Main");
        savings.deposit(bwp("2000"));
        InvestmentAccount investment = new InvestmentAccount(customer, "Main", bwp("1000"));
        ChequeAccount cheque = new ChequeAccount(customer, "Main", "Debswana", "Jwaneng");
        cheque.deposit(bwp("5000"));

        List<Account> all = List.of(savings, investment, cheque);
        for (Account account : all) {
            if (account instanceof InterestBearing payer) {
                payer.payInterest(OCTOBER);
            }
        }

        assertEquals(bwp("2001.00"), savings.getBalance());
        assertEquals(bwp("1050.00"), investment.getBalance());
        assertEquals(bwp("5000.00"), cheque.getBalance());
    }
}
