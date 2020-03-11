package uk.tsarcasm.tsorm.modulardbi;

import uk.tsarcasm.tsorm.Entity;

import java.sql.PreparedStatement;
import java.util.function.Function;

public abstract class Field<T, E extends Entity> {
  protected final T defaultValue;
  protected boolean nullable;

  boolean isNullable() {
    return nullable;
  }

  public String getDefaultValue() {
    return defaultValue == null ? null : defaultValue.toString();
  }

  public abstract void setValue(int i, PreparedStatement statement);

  public abstract String getType();

  public Field() {
    this.defaultValue = null;
    this.nullable = true;
  }

  public Field(T defaultValue) {
    this.defaultValue = defaultValue;
    this.nullable = true;
  }

  public static Field notNull(Field f) {
    f.nullable = false;
    if (f.defaultValue == null) throw new IllegalArgumentException("A not null field cannot have a null default value");
    return f;
  }


}
