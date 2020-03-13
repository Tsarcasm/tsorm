package uk.tsarcasm.tsorm.modulardbi.fields;

import uk.tsarcasm.tsorm.modulardbi.Field;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IntField extends Field<Integer> {

    @Override
    public String getType() {
        return "int(11)";
    }

    @Override
    protected void setupStatement(int i, PreparedStatement statement, Integer value) throws SQLException {
        statement.setInt(i, value);
    }

    @Override
    protected Integer getResult(String name, ResultSet results) throws SQLException {
        return results.getInt(name);
    }
}
