package nl._42.database.truncator;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class DatabaseTruncatorIntegrationTest {

  @Autowired
  private DataSource dataSource;

  @Autowired
  private DatabaseTruncator truncator;

  @Test
  public void testTruncate() throws Exception {
    final JdbcTemplate template = new JdbcTemplate(dataSource);

    template.execute("INSERT INTO my_table (col1) values (1)");
    Assert.assertEquals(1, getCount(template));

    truncator.truncate();
    Assert.assertEquals(0, getCount(template));
  }

  private int getCount(JdbcTemplate template) {
    return template.queryForObject("SELECT COUNT(1) FROM my_table", Integer.class);
  }

}
