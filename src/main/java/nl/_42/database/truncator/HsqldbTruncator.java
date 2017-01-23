package nl._42.database.truncator;

import javax.sql.DataSource;

public class HsqldbTruncator extends AbstractDatabaseTruncator {

    public HsqldbTruncator(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void truncate() throws Exception {
        jdbcTemplate.execute("TRUNCATE SCHEMA public AND COMMIT");
    }

}
