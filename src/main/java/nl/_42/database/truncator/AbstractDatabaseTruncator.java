package nl._42.database.truncator;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

public abstract class AbstractDatabaseTruncator implements DatabaseTruncator {

    public static final String LIQUIBASE_CHANGE_LOG =       "databasechangelog";
    public static final String LIQUIBASE_CHANGE_LOG_LOCK =  "databasechangeloglock";

    protected final JdbcTemplate jdbcTemplate;

    AbstractDatabaseTruncator(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

}
