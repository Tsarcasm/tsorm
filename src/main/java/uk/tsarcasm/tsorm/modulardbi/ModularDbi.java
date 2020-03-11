package uk.tsarcasm.tsorm.modulardbi;

import javafx.util.Pair;
import uk.tsarcasm.tsorm.Entity;
import uk.tsarcasm.tsorm.JavaSqlDBI;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.List;

public class ModularDbi<T extends Entity> extends JavaSqlDBI<T> {

  public String name;
  List<Pair<String, Field<Object, T>>> fields;

  private final String createTableSql;
  private final String insertSql;
  private final String deleteSql;
  private final String saveSql;
  private final String loadSql;
  private final String loadAllSql;

  public ModularDbi(DataSource dataSource) {
    super(dataSource);

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
      Field field = fields.get(i).getValue();
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


  @Override
  protected boolean createTable() {
    return false;
  }

  @Override
  public T insert(T obj) {
    return null;
  }

  @Override
  public T load(int pk) {
    return null;
  }

  @Override
  public boolean save(T obj) {
    return false;
  }

  @Override
  public boolean delete(T obj) {
    return false;
  }

  @Override
  public T refreshRelations(T obj) {
    return null;
  }

  @Override
  public Collection<T> loadAll() {
    return null;
  }
}
