package nl._42.database.truncator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@SpringBootTest
public class DatabaseTruncatorIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private DatabaseTruncator truncator;

    private static final String MY_TABLE = "my_table";
    private static final String IGNORED_TABLE = "ignored_table";

    @Test
    public void testTruncate() throws Exception {
        final JdbcTemplate template = new JdbcTemplate(dataSource);

        template.execute("INSERT INTO my_table (col1) values (1)");
        Assertions.assertEquals(1, getCount(template, MY_TABLE));

        truncator.truncate();
        Assertions.assertEquals(0, getCount(template, MY_TABLE));
    }

    @Test
    public void testTruncateOfExcludedTable_shouldNotTruncate() throws Exception {
        // Table is excluded in src/test/resources/application.properties (database-truncator.exclude).
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        jdbcTemplate.execute("INSERT INTO ignored_table (col1) values (42)");

        Assertions.assertEquals(1, getCount(jdbcTemplate, IGNORED_TABLE));
        truncator.truncate();
        Assertions.assertEquals(1, getCount(jdbcTemplate, IGNORED_TABLE));
    }

    private int getCount(JdbcTemplate template, String tableName) {
        return template.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
    }

}
