package nl._42.database.truncator;

import javax.sql.DataSource;

public class HsqldbTruncator extends AbstractDatabaseTruncator {

    public HsqldbTruncator(DataSource dataSource, DatabaseTruncatorProperties properties) {
        super(dataSource, properties);
    }

    @Override
    public void truncate() throws Exception {
        jdbcTemplate.execute("TRUNCATE SCHEMA public AND COMMIT");
    }

}
