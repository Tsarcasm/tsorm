package uk.tsarcasm.tsorm.modulardbi;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StringField extends Field<String> {
  @Override
  protected void _setValue(int i, PreparedStatement statement, String value) throws SQLException {
    statement.setString(i, value);
  }


  public String _getValue(String name, ResultSet results) throws SQLException {
    return results.getString(name);
  }

  @Override
  public String getType() {
    return "varchar(255)";
  }
}
