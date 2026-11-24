package bank.dao;

import bank.model.Transaction;
import bank.model.TransactionType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/** MySQL storage of transactions: rows are only ever added. */
public class JdbcTransactionDAO implements TransactionDAO {

    private final Database db;

    public JdbcTransactionDAO(Database db) {
        this.db = db;
    }

    @Override
    public void save(Transaction t) {
        db.update("INSERT INTO transactions (transaction_id, account_number, tx_type, amount, balance_after,"
                        + " tx_time, reference) VALUES (?, ?, ?, ?, ?, ?, ?)",
                t.getTransactionId(), t.getAccountNumber(), t.getType().name(), t.getAmount(),
                t.getBalanceAfter(), t.getTime(), t.getReference());
    }

    @Override
    public Transaction findById(String id) {
        return db.queryOne("SELECT * FROM transactions WHERE transaction_id = ?", JdbcTransactionDAO::map, id);
    }

    @Override
    public List<Transaction> findAll() {
        return db.query("SELECT * FROM transactions ORDER BY seq", JdbcTransactionDAO::map);
    }

    @Override
    public List<Transaction> findByAccount(String accountNumber) {
        return db.query("SELECT * FROM transactions WHERE account_number = ? ORDER BY seq",
                JdbcTransactionDAO::map, accountNumber);
    }

    @Override
    public void update(Transaction item) {
        throw new UnsupportedOperationException("A saved transaction cannot be changed.");
    }

    @Override
    public void delete(String id) {
        throw new UnsupportedOperationException("A saved transaction cannot be deleted.");
    }

    private static Transaction map(ResultSet r) throws SQLException {
        return new Transaction(r.getString("transaction_id"), r.getString("account_number"),
                TransactionType.valueOf(r.getString("tx_type")), r.getBigDecimal("amount"),
                r.getObject("tx_time", java.time.LocalDateTime.class), r.getBigDecimal("balance_after"),
                r.getString("reference"));
    }
}
