package bank.dao;

import bank.exception.DataAccessException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * The one MySQL connection of the program and the small helpers the JDBC DAOs share.
 * Settings are read from db.properties (db.url, db.user, db.password). Every SQL error is turned
 * into a {@link DataAccessException}, so controllers only ever catch BankException.
 */
public class Database {

    /** Turns one row of a result set into an object. */
    @FunctionalInterface
    public interface RowMapper<T> {
        T map(ResultSet row) throws SQLException;
    }

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/bank_db"
            + "?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private final String url;
    private final String user;
    private final String password;
    private Connection connection;

    public Database(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /** Reads db.properties: from -Dbank.config, the working folder, or up to three folders above it. */
    public static Database fromConfig() {
        Properties props = new Properties();
        Path file = findConfig();
        if (file != null) {
            try (InputStream in = Files.newInputStream(file)) {
                props.load(in);
            } catch (IOException e) {
                throw new DataAccessException("Could not read " + file + ": " + e.getMessage(), e);
            }
        }
        return new Database(props.getProperty("db.url", DEFAULT_URL),
                props.getProperty("db.user", "root"), props.getProperty("db.password", ""));
    }

    private static Path findConfig() {
        String given = System.getProperty("bank.config");
        if (given != null && Files.isRegularFile(Paths.get(given))) {
            return Paths.get(given);
        }
        Path dir = Paths.get("").toAbsolutePath();
        for (int i = 0; i < 4 && dir != null; i++, dir = dir.getParent()) {
            Path candidate = dir.resolve("db.properties");
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    /** Opens the connection and creates the tables when they do not exist yet. */
    public void connect() {
        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            String hint = e.getMessage() != null && e.getMessage().contains("No suitable driver")
                    ? "The MySQL driver jar is not in the lib folder."
                    : e.getMessage();
            throw new DataAccessException("Cannot connect to MySQL: " + hint, e);
        }
        createTables();
    }

    private void createTables() {
        for (String sql : Schema.STATEMENTS) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
            } catch (SQLException e) {
                throw new DataAccessException("Could not create the tables: " + e.getMessage(), e);
            }
        }
    }

    public int update(String sql, Object... params) {
        try (PreparedStatement statement = prepare(sql, params)) {
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Database error: " + e.getMessage(), e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) {
        try (PreparedStatement statement = prepare(sql, params); ResultSet rows = statement.executeQuery()) {
            List<T> result = new ArrayList<>();
            while (rows.next()) {
                result.add(mapper.map(rows));
            }
            return result;
        } catch (SQLException e) {
            throw new DataAccessException("Database error: " + e.getMessage(), e);
        }
    }

    /** The first row, or null when there is none. */
    public <T> T queryOne(String sql, RowMapper<T> mapper, Object... params) {
        List<T> rows = query(sql, mapper, params);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** Runs the work as one database transaction: all of it is saved, or none of it. */
    public void inTransaction(Runnable work) {
        try {
            connection.setAutoCommit(false);
            try {
                work.run();
                connection.commit();
            } catch (RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error: " + e.getMessage(), e);
        }
    }

    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ignored) {
            // nothing more to do
        }
    }

    private PreparedStatement prepare(String sql, Object[] params) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
        return statement;
    }
}
