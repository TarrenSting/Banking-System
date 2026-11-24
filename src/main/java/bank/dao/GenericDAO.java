package bank.dao;

import java.util.List;

/**
 * The create, read, update and delete operations declared once for every stored type.
 * Controllers depend on these interfaces, so the store can change (memory, H2, MySQL)
 * without editing a controller (NFR-10).
 */
public interface GenericDAO<T> {

    /** Stores a new item. */
    void save(T item);

    /** The item with this key, or null when there is none. */
    T findById(String id);

    List<T> findAll();

    /** Stores the changed state of an item that already exists. */
    void update(T item);

    void delete(String id);
}
