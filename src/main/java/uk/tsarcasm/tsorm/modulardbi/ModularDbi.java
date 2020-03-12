package uk.tsarcasm.tsorm.modulardbi;

import javafx.util.Pair;
import uk.tsarcasm.tsorm.Entity;
import uk.tsarcasm.tsorm.JavaSqlDBI;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public abstract class ModularDbi<T extends Entity> extends JavaSqlDBI<T> {

  public final boolean canDelete;
  private final String createTableSql;
  private final String insertSql;
  private final String deleteSql;
  private final String saveSql;
  private final String loadSql;
  private final String loadAllSql;
  public String name;
  List<Pair<String, Field<?>>> fields;

  public ModularDbi(DataSource dataSource, boolean canDelete) {
    super(dataSource);
    this.canDelete = canDelete;
    // Generate and cache queries
    this.createTableSql = createTableQuery();
    this.insertSql = insertQuery();
    this.deleteSql = deleteQuery();
    this.saveSql = saveQuery();
    this.loadSql = loadQuery();
    this.loadAllSql = loadAllQuery();
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

  private String saveQuery() {
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


  protected abstract T instantiate_select(HashMap<String, FieldValue<?>> valueSet);

  protected abstract T instantiate_insert(HashMap<String, FieldValue<?>> valueSet, int pk);

  protected abstract HashMap<String, FieldValue<?>> getFieldValuesFromEntity(T entity);

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
        HashMap<String, FieldValue<?>> values = getFieldValuesFromEntity(obj);
        for (Pair<String, Field<?>> kvp : fields) {
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
          return instantiate_insert(getFieldValuesFromEntity(obj), newPk);
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
          return instantiate_select(resultSetToFieldValues(results));
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public boolean save(T obj) {
    try {
      try (Connection conn = getConnection()) {
        PreparedStatement statement = conn.prepareStatement(saveSql);
        // Populate all values in the query
        int i = 1;
        // Get a all the values to fill the query with
        HashMap<String, FieldValue<?>> values = getFieldValuesFromEntity(obj);
        for (Pair<String, Field<?>> kvp : fields) {
          // Find the value for each field and set it in the statment
          String name = kvp.getKey();
          Field<?> field = kvp.getValue();
          field.setValue(i, statement, values.get(name));
          i++;
        }
        // Set the pk to save
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
          objects.add(instantiate_select(resultSetToFieldValues(results)));
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return objects;
  }
}
