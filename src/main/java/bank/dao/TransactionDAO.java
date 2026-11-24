package bank.dao;

import bank.model.Transaction;

import java.util.List;

/**
 * Storage of transactions. A saved transaction can never be changed or removed (NFR-02),
 * so update and delete are not supported.
 */
public interface TransactionDAO extends GenericDAO<Transaction> {

    /** Every transaction of the account, oldest first. */
    List<Transaction> findByAccount(String accountNumber);
}
