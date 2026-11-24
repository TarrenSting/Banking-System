package bank.dao;

import bank.model.User;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Keeps users in memory. A JDBC version replaces it when the database step is added. */
public class InMemoryUserDAO implements UserDAO {

    private final Map<String, User> users = new LinkedHashMap<>();

    @Override
    public void save(User user) {
        users.put(user.getUsername(), user);
    }

    @Override
    public User findById(String id) {
        return findByUsername(id);
    }

    @Override
    public User findByUsername(String username) {
        return username == null ? null : users.get(username.trim());
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void update(User user) {
        users.put(user.getUsername(), user);
    }

    @Override
    public void delete(String id) {
        users.remove(id);
    }
}
