package uk.tsarcasm.tsorm;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class JavaSqlDBI<T extends Entity> implements DatabaseInterface<T> {
    protected DataSource dataSource;

    public JavaSqlDBI(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public abstract boolean createTable();

    protected Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }


}
