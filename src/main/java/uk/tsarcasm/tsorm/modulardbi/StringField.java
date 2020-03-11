package uk.tsarcasm.tsorm.modulardbi;

import uk.tsarcasm.tsorm.Entity;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StringField<E extends Entity> extends Field<String, E> {
  @Override
  public void setValue(int i, PreparedStatement statement) throws SQLException {
    statement.setString(i, "test");
  }

  @Override
  public String getType() {
    return "varchar(255)";
  }
}
