package bank.dao;

import bank.model.User;

/** Storage of logins (use case: Log In). */
public interface UserDAO extends GenericDAO<User> {

    /** The user with this username, or null when there is none. */
    User findByUsername(String username);
}
