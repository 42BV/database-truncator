package nl._42.database.truncator.postgres;

import nl._42.database.truncator.config.DatabaseTruncatorProperties;

import javax.sql.DataSource;

public class PostgresTruncationStrategy extends AbstractPostgresTruncationStrategy {

    public PostgresTruncationStrategy(DataSource dataSource, DatabaseTruncatorProperties properties) {
        super(dataSource, properties);
    }

    @Override
    public void executePostgresTruncate() {
        tables.forEach(table -> jdbcTemplate.execute("TRUNCATE TABLE " + table + " CASCADE"));
    }

}
