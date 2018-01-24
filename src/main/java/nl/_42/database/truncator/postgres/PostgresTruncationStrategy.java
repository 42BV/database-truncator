package nl._42.database.truncator.postgres;

import javax.sql.DataSource;

import nl._42.database.truncator.config.DatabaseTruncatorProperties;

public class PostgresTruncationStrategy extends AbstractPostgresTruncationStrategy {

    public PostgresTruncationStrategy(DataSource dataSource, DatabaseTruncatorProperties properties) {
        super(dataSource, properties);
    }

    @Override
    public void executePostgresTruncate() {
        executeSql(tables, "truncate tables", "TRUNCATE TABLE %s CASCADE;");
    }
}
