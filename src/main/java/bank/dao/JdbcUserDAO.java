package bank.dao;

import bank.model.Customer;
import bank.model.Role;
import bank.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;

/** MySQL storage of logins. Only the password hash is stored, never the password. */
public class JdbcUserDAO implements UserDAO {

    private final Database db;
    private final Function<String, Customer> customers;

    public JdbcUserDAO(Database db, Function<String, Customer> customers) {
        this.db = db;
        this.customers = customers;
    }

    @Override
    public void save(User u) {
        db.update("INSERT INTO users (username, password_hash, role, customer_id) VALUES (?, ?, ?, ?)",
                u.getUsername(), u.getPasswordHash(), u.getRole().name(), customerId(u));
    }

    @Override
    public User findById(String id) {
        return findByUsername(id);
    }

    @Override
    public User findByUsername(String username) {
        return username == null ? null
                : db.queryOne("SELECT * FROM users WHERE username = ?", this::map, username.trim());
    }

    @Override
    public List<User> findAll() {
        return db.query("SELECT * FROM users ORDER BY username", this::map);
    }

    @Override
    public void update(User u) {
        db.update("UPDATE users SET password_hash = ?, role = ?, customer_id = ? WHERE username = ?",
                u.getPasswordHash(), u.getRole().name(), customerId(u), u.getUsername());
    }

    @Override
    public void delete(String id) {
        db.update("DELETE FROM users WHERE username = ?", id);
    }

    private static String customerId(User u) {
        return u.getCustomer() == null ? null : u.getCustomer().getCustomerId();
    }

    private User map(ResultSet r) throws SQLException {
        User user = User.fromStored(r.getString("username"), r.getString("password_hash"),
                Role.valueOf(r.getString("role")));
        String customerId = r.getString("customer_id");
        if (customerId != null) {
            user.setCustomer(customers.apply(customerId));
        }
        return user;
    }
}
