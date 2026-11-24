package bank.model;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates unique customer IDs and account numbers (FR-02, FR-09).
 * In Part B the DAOs will call {@link #resetTo} on start-up so numbers continue
 * after the highest value stored in the database.
 */
public final class IdGenerator {

    private static final AtomicLong CUSTOMER_COUNTER = new AtomicLong(0);
    private static final AtomicLong ACCOUNT_COUNTER = new AtomicLong(0);

    private IdGenerator() {
    }

    public static String nextCustomerId() {
        return String.format("C%05d", CUSTOMER_COUNTER.incrementAndGet());
    }

    public static String nextAccountNumber() {
        return String.format("ACC%07d", ACCOUNT_COUNTER.incrementAndGet());
    }

    /** Continues numbering after values already stored (used when loading from the database). */
    public static void resetTo(long lastCustomerNumber, long lastAccountNumber) {
        CUSTOMER_COUNTER.set(lastCustomerNumber);
        ACCOUNT_COUNTER.set(lastAccountNumber);
    }
}
