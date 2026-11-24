package bank.dao;

import bank.model.Customer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/** MySQL storage of customers. */
public class JdbcCustomerDAO implements CustomerDAO {

    private final Database db;

    public JdbcCustomerDAO(Database db) {
        this.db = db;
    }

    @Override
    public void save(Customer c) {
        db.update("INSERT INTO customers (customer_id, first_name, surname, company_name, address, phone,"
                        + " email, employed) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                c.getCustomerId(), c.getFirstName(), c.getSurname(), c.getCompanyName(), c.getAddress(),
                c.getPhone(), c.getEmail(), c.isEmployed());
    }

    @Override
    public Customer findById(String id) {
        return db.queryOne("SELECT * FROM customers WHERE customer_id = ?", JdbcCustomerDAO::map, id);
    }

    @Override
    public List<Customer> findAll() {
        return db.query("SELECT * FROM customers ORDER BY customer_id", JdbcCustomerDAO::map);
    }

    @Override
    public void update(Customer c) {
        db.update("UPDATE customers SET address = ?, phone = ?, email = ?, employed = ? WHERE customer_id = ?",
                c.getAddress(), c.getPhone(), c.getEmail(), c.isEmployed(), c.getCustomerId());
    }

    @Override
    public void delete(String id) {
        db.update("DELETE FROM customers WHERE customer_id = ?", id);
    }

    private static Customer map(ResultSet r) throws SQLException {
        String company = r.getString("company_name");
        if (company != null) {
            return Customer.company(r.getString("customer_id"), company, r.getString("address"),
                    r.getString("phone"), r.getString("email"));
        }
        return Customer.individual(r.getString("customer_id"), r.getString("first_name"),
                r.getString("surname"), r.getString("address"), r.getString("phone"),
                r.getString("email"), r.getBoolean("employed"));
    }
}
