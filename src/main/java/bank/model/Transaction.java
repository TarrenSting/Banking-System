package bank.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * One saved deposit, withdrawal or interest payment (FR-17).
 * Immutable: there are getters only and no setters, so a saved record cannot be edited (NFR-02).
 */
public final class Transaction {

    private final String transactionId;
    private final String accountNumber;
    private final TransactionType type;
    private final BigDecimal amount;
    private final LocalDateTime time;
    private final BigDecimal balanceAfter;
    private final String reference;

    /** Creates a new transaction with a generated ID and the current time. */
    public Transaction(String accountNumber, TransactionType type, BigDecimal amount,
                       BigDecimal balanceAfter, String reference) {
        this(UUID.randomUUID().toString(), accountNumber, type, amount,
                LocalDateTime.now(), balanceAfter, reference);
    }

    /** Overload used when a stored transaction is loaded back from the database. */
    public Transaction(String transactionId, String accountNumber, TransactionType type,
                       BigDecimal amount, LocalDateTime time, BigDecimal balanceAfter,
                       String reference) {
        this.transactionId = transactionId;
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.time = time;
        this.balanceAfter = balanceAfter;
        this.reference = reference;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    /** Optional note such as a salary reference; may be null. */
    public String getReference() {
        return reference;
    }

    @Override
    public String toString() {
        return time + " " + type + " " + amount + " (balance " + balanceAfter + ")";
    }
}
