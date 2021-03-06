package uk.tsarcasm.tsorm;

public class SyncDatabaseStore<T extends Entity> extends DatabaseStore<T> {
    @Override
    protected boolean db_save(T obj) {
        return dbi.update(obj);
    }

    @Override
    protected boolean db_delete(T obj) {
        return dbi.delete(obj);
    }

    @Override
    protected T db_create(T obj) {
        return dbi.insert(obj);
    }
}
