package nl._42.database.truncator.hsqldb;

import nl._42.database.truncator.config.DatabaseTruncatorProperties;
import nl._42.database.truncator.shared.AbstractTruncationStrategy;

import javax.sql.DataSource;

public class HsqldbTruncationStrategy extends AbstractTruncationStrategy {

    public HsqldbTruncationStrategy(DataSource dataSource, DatabaseTruncatorProperties properties) {
        super(dataSource, properties);
    }

    @Override
    public void setup() {
        // No setup required for HSQLDB
    }

    @Override
    public void executeTruncate() {
        jdbcTemplate.execute("TRUNCATE SCHEMA public AND COMMIT");
    }

}
