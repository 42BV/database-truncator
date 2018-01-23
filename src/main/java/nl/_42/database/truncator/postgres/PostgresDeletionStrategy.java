package nl._42.database.truncator.postgres;

import javax.sql.DataSource;

import nl._42.database.truncator.config.DatabaseTruncatorProperties;

public class PostgresDeletionStrategy extends AbstractPostgresTruncationStrategy {

    public PostgresDeletionStrategy(DataSource dataSource, DatabaseTruncatorProperties properties) {
        super(dataSource, properties);
    }

    @Override
    public void executePostgresTruncate() {
        executeSql(tables,"disable triggers", "ALTER TABLE %s DISABLE TRIGGER ALL;");
        executeSql(tables,"delete tables", "DELETE FROM %s;");
        executeSql(tables,"enable triggers", "ALTER TABLE %s ENABLE TRIGGER ALL;");
    }
}
