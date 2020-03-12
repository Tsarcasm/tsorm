package uk.tsarcasm.tsorm;

import java.util.Collection;

/**
 * @param <T> Entity subclass the DBI represents in the database
 */
public interface DatabaseInterface<T extends Entity> {

    /**
     * Create a new entry in the database for an entity. This will create a new primary key for that entity
     *
     * @param obj The entity to create in the database
     * @return A copy of the entity with a new primary key
     */
    T insert(T obj); // create

    /**
     * Instantiate an entity for the matching entry in the database
     *
     * @param pk The primary key of the entry in the database to get
     * @return A new instance of that entity. Returns null if no entity for that PK is found
     */
    T load(int pk); // read


    /**
     * Saves a copy of an entity in the database
     *
     * @param obj The entity to save to the database
     * @return Boolean, true if the operation was successful
     */
    boolean save(T obj); // update

    boolean delete(T obj); // delete

    T refreshRelations(T obj);

    Collection<T> loadAll();
}
