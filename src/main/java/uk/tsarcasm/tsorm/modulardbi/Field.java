package uk.tsarcasm.tsorm.modulardbi;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class Field<T> {
    protected final T defaultValue;
    protected boolean nullable;

    public Field() {
        this.defaultValue = null;
        this.nullable = true;
    }

    public Field(T defaultValue) {
        this.defaultValue = defaultValue;
        this.nullable = true;
    }

    public static Field<?> notNull(Field<?> f) {
        f.nullable = false;
        if (f.defaultValue == null)
            throw new IllegalArgumentException("A not null field cannot have a null default value");
        return f;
    }

    boolean isNullable() {
        return nullable;
    }

    public String getDefaultValue() {
        return defaultValue == null ? null : defaultValue.toString();
    }

    public void setValue(int i, PreparedStatement statement, FieldValue<?> value) throws SQLException {
        @SuppressWarnings("unchecked") T val = (T) value.getValue();
        setupStatement(i, statement, val);
    }

    public FieldValue<T> getValue(String name, ResultSet results) throws SQLException {
        return new FieldValue<>(getResult(name, results));
    }

    public abstract String getType();

    protected abstract void setupStatement(int i, PreparedStatement statement, T value) throws SQLException;

    protected abstract T getResult(String name, ResultSet results) throws SQLException;


}
