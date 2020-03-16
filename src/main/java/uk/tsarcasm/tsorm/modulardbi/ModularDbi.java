package uk.tsarcasm.tsorm.modulardbi;

import javafx.util.Pair;
import uk.tsarcasm.tsorm.Entity;
import uk.tsarcasm.tsorm.JavaSqlDBI;
import uk.tsarcasm.tsorm.modulardbi.fields.IntField;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public abstract class ModularDbi<T extends Entity> extends JavaSqlDBI<T> {
    public final boolean canDelete;
    public String name;
    protected List<Pair<String, Field<?>>> fields;
    private HashMap<String, FieldValue<?>> valueSet;
    private String createTableSql;
    private String insertSql;
    private String deleteSql;
    private String updateSql;
    private String loadSql;
    private String loadAllSql;

    public ModularDbi(DataSource dataSource, boolean canDelete) {
        super(dataSource);
        this.canDelete = canDelete;
        fields = new ArrayList<>();
    }

    // This must be called at the end of the constructor of subclasses
    protected void setupQueryStrings() {
        this.createTableSql = createTableQuery();
        this.insertSql = insertQuery();
        this.deleteSql = deleteQuery();
        this.updateSql = updateQuery();
        this.loadSql = loadQuery();
        this.loadAllSql = loadAllQuery();
    }


    protected void addPk() {
        fields.add(new Pair<>("pk", new IntField()));
    }

    protected void addField(String name, Field<?> field) {
        fields.add(new Pair<>(name, field));
    }

    private String createTableQuery() {
        StringBuilder string = new StringBuilder().append("CREATE TABLE IF NOT EXISTS ").append(name).append(" (");

        // Add each field to the query
        for (int i = 0; i < fields.size(); i++) {
            String name = fields.get(i).getKey();
            Field<?> field = fields.get(i).getValue();
            // Add the name
            string.append(name).append(" ").append(field.getType()).append(" ");
            // If this is the first field then it is the primary key
            if (i == 0) {
                string.append("NOT NULL AUTO_INCREMENT ");
            } else {
                // Else add the default value and null flag
                if (!field.isNullable()) {
                    string.append("NOT NULL DEFAULT ");
                    string.append(field.getDefaultValue());
                } else {
                    string.append("NULL DEFAULT ");
                    // Specify the default if we have one
                    if (field.getDefaultValue() != null) {
                        string.append(field.getDefaultValue());
                    } else {
                        // Otherwise default null
                        string.append("NULL ");
                    }
                }
            }
            string.append(", ");
        }
        // Finally specify primary key
        string.append("PRIMARY KEY (").append(fields.get(0).getKey()).append(") )");
        return string.toString();
    }

    private String insertQuery() {
        // "INSERT INTO name (field, field, field) VALUES (?, ?, ?)",
        StringBuilder query = new StringBuilder("INSERT INTO ").append(name).append(" (");

        // First specify field names
        for (int i = 0; i < fields.size(); i++) {
            String name = fields.get(i).getKey();
            query.append(name);
            if (i < fields.size() - 1) query.append(", ");
        }

        // Then add value substitutes
        query.append(") VALUES (");
        for (int i = 0; i < fields.size(); i++) {
            query.append("?");
            if (i < fields.size() - 1) query.append(", ");
        }
        query.append(")");
        return query.toString();
    }

    private String deleteQuery() {
        // DELETE FROM name WHERE pk = ?
        return "DELETE FROM " + name + " WHERE " + fields.get(0).getKey() + " = ?";
    }

    private String updateQuery() {
        //  "UPDATE table SET player_uuid = ?, balance = ?, share = ?, account_pk = ? WHERE pk = ?");
        StringBuilder query = new StringBuilder("UPDATE ").append(name).append(" SET ");
        // First specify field names
        for (int i = 0; i < fields.size(); i++) {
            String name = fields.get(i).getKey();
            query.append(name).append(" = ?");
            if (i < fields.size() - 1) query.append(", ");
        }
        // Add where clause for PK
        query.append(" WHERE ").append(fields.get(0).getKey()).append(" = ?");
        return query.toString();
    }

    private String loadQuery() {
        // Add where clause
        return loadAllQuery() + " WHERE " + fields.get(0).getKey() + " = ?";
    }

    private String loadAllQuery() {
        //"SELECT player_uuid, balance, share, account_pk FROM holding WHERE pk = ?");

        StringBuilder query = new StringBuilder("SELECT ");

        // First specify field names
        for (int i = 0; i < fields.size(); i++) {
            String name = fields.get(i).getKey();
            query.append(name);
            if (i < fields.size() - 1) query.append(", ");
        }

        query.append(" FROM ").append(name);
        return query.toString();
    }


    protected final <V> V getValue(String name) {
        @SuppressWarnings("unchecked") V value = (V) valueSet.get(name).getValue();
        return value;
    }

    protected final <V> void setValue(String name, V value) {
        valueSet.put(name, new FieldValue<>(value));
    }


    protected abstract T instantiateSelect();

    protected abstract T instantiateInsert(int pk);

    protected abstract void entityToFieldValues(T obj);

    private HashMap<String, FieldValue<?>> resultSetToFieldValues(ResultSet resultSet) throws SQLException {
        HashMap<String, FieldValue<?>> values = new HashMap<>();
        for (Pair<String, Field<?>> kvp : fields) {
            // Find the value for each field and set it in the statment
            String name = kvp.getKey();
            Field<?> field = kvp.getValue();
            values.put(name, field.getValue(name, resultSet));
        }
        return values;
    }

    @Override
    protected boolean createTable() {
        try {
            try (Connection conn = getConnection()) {
                conn.createStatement().executeUpdate(createTableSql);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public T insert(T obj) {
        PreparedStatement statement;
        try {
            try (Connection conn = getConnection()) {
                //Setup the SQL statement
                statement = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);

                // Populate all values in the query
                int i = 1;
                // Get a all the values to fill the query with
                valueSet.clear();
                entityToFieldValues(obj);
                for (Pair<String, Field<?>> kvp : fields) {
                    // Find the value for each field and set it in the statment
                    String name = kvp.getKey();
                    Field<?> field = kvp.getValue();
                    field.setValue(i, statement, valueSet.get(name));
                    i++;
                }
                statement.executeUpdate();
                ResultSet rs = statement.getGeneratedKeys();

                if (rs.next()) {
                    int newPk = rs.getInt(1);
                    return instantiateInsert(newPk);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public T load(int pk) {
        try {
            try (Connection conn = getConnection()) {
                PreparedStatement statement = conn.prepareStatement(loadSql);
                statement.setInt(1, pk);
                ResultSet results = statement.executeQuery();
                if (results.next()) {
                    //Get all accounts
                    valueSet = resultSetToFieldValues(results);
                    return instantiateSelect();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean update(T obj) {
        try {
            try (Connection conn = getConnection()) {
                PreparedStatement statement = conn.prepareStatement(updateSql);
                // Populate all values in the query
                int i = 1;
                // Get a all the values to fill the query with
                valueSet.clear();
                entityToFieldValues(obj);
                for (Pair<String, Field<?>> kvp : fields) {
                    // Find the value for each field and set it in the statment
                    String name = kvp.getKey();
                    Field<?> field = kvp.getValue();
                    field.setValue(i, statement, valueSet.get(name));
                    i++;
                }
                // Set the pk to update
                statement.setInt(i, obj.pk);
                statement.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(T obj) {
        if (canDelete) try {
            try (Connection conn = getConnection()) {
                PreparedStatement statement = conn.prepareStatement(deleteSql);
                statement.setInt(1, obj.pk);
                statement.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public Collection<T> loadAll() {
        Collection<T> objects = new ArrayList<>();
        try {
            try (Connection conn = getConnection()) {
                PreparedStatement statement = conn.prepareStatement(loadAllSql);

                ResultSet results = statement.executeQuery();
                //Get all objects
                while (results.next()) {
                    // Instantiate one by one
                    valueSet = resultSetToFieldValues(results);
                    objects.add(instantiateSelect());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return objects;
    }
}
