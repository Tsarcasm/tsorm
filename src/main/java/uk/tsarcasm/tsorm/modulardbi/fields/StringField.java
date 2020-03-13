package uk.tsarcasm.tsorm.modulardbi.fields;

import uk.tsarcasm.tsorm.modulardbi.Field;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StringField extends Field<String> {
    @Override
    protected void setupStatement(int i, PreparedStatement statement, String value) throws SQLException {
        statement.setString(i, value);
    }


    public String getResult(String name, ResultSet results) throws SQLException {
        return results.getString(name);
    }

    @Override
    public String getType() {
        return "varchar(255)";
    }
}
