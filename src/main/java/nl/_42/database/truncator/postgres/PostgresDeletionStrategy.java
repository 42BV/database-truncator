package nl._42.database.truncator.postgres;

import nl._42.database.truncator.config.DatabaseTruncatorProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import javax.sql.DataSource;
import java.util.List;

public class PostgresDeletionStrategy extends AbstractPostgresTruncationStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresDeletionStrategy.class);

    public PostgresDeletionStrategy(DataSource dataSource, DatabaseTruncatorProperties properties) {
        super(dataSource, properties);
    }

    @Override
    public void executePostgresTruncate() {
        executeSql(tables,"disable triggers", "ALTER TABLE %s DISABLE TRIGGER ALL");
        executeSql(tables,"delete tables", "DELETE FROM %s");
        executeSql(tables,"enable triggers", "ALTER TABLE %s ENABLE TRIGGER ALL");
    }

    private void executeSql(List<String> records, String title, String sql) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        records.forEach(record -> executeSql(null, String.format(sql, record)));
        stopWatch.stop();
        LOGGER.debug(title + " took: " + stopWatch.getTotalTimeMillis() + " ms");
    }

    private void executeSql(String title, String sql) {
        StopWatch stopWatch = null;
        if (title != null) {
            stopWatch = new StopWatch();
            stopWatch.start();
        }
        jdbcTemplate.execute(sql);
        if (stopWatch != null) {
            stopWatch.stop();
            LOGGER.debug(title + " took: " + stopWatch.getTotalTimeMillis() + " ms");
        }
    }

}
