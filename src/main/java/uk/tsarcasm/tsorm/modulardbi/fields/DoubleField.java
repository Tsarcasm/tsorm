package uk.tsarcasm.tsorm.modulardbi.fields;

import uk.tsarcasm.tsorm.modulardbi.Field;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DoubleField extends Field<Double> {
    @Override
    public String getType() {
        return "double";
    }

    @Override
    protected void setupStatement(int i, PreparedStatement statement, Double value) throws SQLException {
        statement.setDouble(i, value);
    }

    @Override
    protected Double getResult(String name, ResultSet results) throws SQLException {
        return results.getDouble(name);
    }
}
