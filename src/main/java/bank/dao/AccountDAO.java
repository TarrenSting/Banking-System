package bank.dao;

import bank.model.Account;
import bank.model.Customer;

import java.util.List;
import java.util.function.Function;

/** Storage of accounts. A loaded account comes back with its transactions and balance. */
public interface AccountDAO extends GenericDAO<Account> {

    /** Tells the DAO how to find the one Customer object that holds each account. */
    void setHolderLookup(Function<String, Customer> holders);

    List<Account> findByCustomer(String customerId);
}
