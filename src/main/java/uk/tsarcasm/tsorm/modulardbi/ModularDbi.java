package uk.tsarcasm.tsorm.modulardbi;

import javafx.util.Pair;
import uk.tsarcasm.tsorm.Entity;
import uk.tsarcasm.tsorm.JavaSqlDBI;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public abstract class ModularDbi<T extends Entity> extends JavaSqlDBI<T> {
    public final boolean canDelete;
    public String name;
    protected EntityMeta<T> entityMeta;
    private String createTableSql;
    private String insertSql;
    private String deleteSql;
    private String updateSql;
    private String loadSql;
    private String loadAllSql;

    public ModularDbi(DataSource dataSource, EntityMeta<T> entityMeta, boolean canDelete) {
        super(dataSource);
        this.entityMeta = entityMeta;
        this.canDelete = canDelete;
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
    private String createTableQuery() {
        StringBuilder string = new StringBuilder().append("CREATE TABLE IF NOT EXISTS ").append(name).append(" (");

        // Add each field to the query
        for (int i = 0; i < entityMeta.getFields().size(); i++) {
            String name = entityMeta.getFields().get(i).getKey();
            Field<?> field = entityMeta.getFields().get(i).getValue();
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
        string.append("PRIMARY KEY (").append(entityMeta.getFields().get(0).getKey()).append(") )");
        return string.toString();
    }

    private String insertQuery() {
        // "INSERT INTO name (field, field, field) VALUES (?, ?, ?)",
        StringBuilder query = new StringBuilder("INSERT INTO ").append(name).append(" (");

        // First specify field names
        for (int i = 0; i < entityMeta.getFields().size(); i++) {
            String name = entityMeta.getFields().get(i).getKey();
            query.append(name);
            if (i < entityMeta.getFields().size() - 1) query.append(", ");
        }

        // Then add value substitutes
        query.append(") VALUES (");
        for (int i = 0; i < entityMeta.getFields().size(); i++) {
            query.append("?");
            if (i < entityMeta.getFields().size() - 1) query.append(", ");
        }
        query.append(")");
        return query.toString();
    }

    private String deleteQuery() {
        // DELETE FROM name WHERE pk = ?
        return "DELETE FROM " + name + " WHERE " + entityMeta.getFields().get(0).getKey() + " = ?";
    }

    private String updateQuery() {
        //  "UPDATE table SET player_uuid = ?, balance = ?, share = ?, account_pk = ? WHERE pk = ?");
        StringBuilder query = new StringBuilder("UPDATE ").append(name).append(" SET ");
        // First specify field names
        for (int i = 0; i < entityMeta.getFields().size(); i++) {
            String name = entityMeta.getFields().get(i).getKey();
            query.append(name).append(" = ?");
            if (i < entityMeta.getFields().size() - 1) query.append(", ");
        }
        // Add where clause for PK
        query.append(" WHERE ").append(entityMeta.getFields().get(0).getKey()).append(" = ?");
        return query.toString();
    }

    private String loadQuery() {
        // Add where clause
        return loadAllQuery() + " WHERE " + entityMeta.getFields().get(0).getKey() + " = ?";
    }

    private String loadAllQuery() {
        //"SELECT player_uuid, balance, share, account_pk FROM holding WHERE pk = ?");

        StringBuilder query = new StringBuilder("SELECT ");

        // First specify field names
        for (int i = 0; i < entityMeta.getFields().size(); i++) {
            String name = entityMeta.getFields().get(i).getKey();
            query.append(name);
            if (i < entityMeta.getFields().size() - 1) query.append(", ");
        }

        query.append(" FROM ").append(name);
        return query.toString();
    }

    private HashMap<String, FieldValue<?>> resultSetToFieldValues(ResultSet resultSet) throws SQLException {
        HashMap<String, FieldValue<?>> values = new HashMap<>();
        for (Pair<String, Field<?>> kvp : entityMeta.getFields()) {
            // Find the value for each field and set it in the statement
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
                HashMap<String, FieldValue<?>> values = entityMeta.getValues(obj);
                for (Pair<String, Field<?>> kvp : entityMeta.getFields()) {
                    // Find the value for each field and set it in the statment
                    String name = kvp.getKey();
                    Field<?> field = kvp.getValue();
                    field.setValue(i, statement, values.get(name));
                    i++;
                }
                statement.executeUpdate();
                ResultSet rs = statement.getGeneratedKeys();

                if (rs.next()) {
                    int newPk = rs.getInt(1);
                    return entityMeta.instantiate(values);
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
                    return entityMeta.instantiate(resultSetToFieldValues(results));
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
                HashMap<String, FieldValue<?>> values = entityMeta.getValues(obj);
                for (Pair<String, Field<?>> kvp : entityMeta.getFields()) {
                    // Find the value for each field and set it in the statment
                    String name = kvp.getKey();
                    Field<?> field = kvp.getValue();
                    field.setValue(i, statement, values.get(name));
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
                    objects.add(entityMeta.instantiate(resultSetToFieldValues(results)));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return objects;
    }
}
