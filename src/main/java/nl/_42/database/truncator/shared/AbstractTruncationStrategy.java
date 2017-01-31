package nl._42.database.truncator.shared;

import nl._42.database.truncator.DatabaseTruncator;
import nl._42.database.truncator.config.DatabaseTruncatorProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StopWatch;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTruncationStrategy implements DatabaseTruncator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTruncationStrategy.class);

    protected static final String LIQUIBASE_CHANGE_LOG =       "databasechangelog";
    protected static final String LIQUIBASE_CHANGE_LOG_LOCK =  "databasechangeloglock";

    protected final JdbcTemplate jdbcTemplate;

    protected final DatabaseTruncatorProperties properties;

    public AbstractTruncationStrategy(DataSource dataSource,
                                      DatabaseTruncatorProperties properties) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.properties = properties;
    }

    public void truncate() {
        StopWatch total = new StopWatch();
        total.start();
        executeTruncate();
        total.stop();
        LOGGER.debug("Total truncation took: " + total.getTotalTimeMillis() + " ms");
    }

    protected void logList(String title, List<String> list) {
        LOGGER.debug(title);
        for (String item : list) {
            LOGGER.debug("- " + item);
        }
    }

    protected List<String> filterExcludedTables(List<String> tables) {
        AntPathMatcher matcher = new AntPathMatcher();
        List<String> selectedTables = new ArrayList<>(tables);
        for (String table : tables) {
            for (String exclude : properties.getExclude()) {
                if (matcher.match(exclude, table) && selectedTables.contains(table)) {
                    LOGGER.debug(
                            "Match for [" + table + "] on pattern [" + exclude + "], removing from tables");
                    selectedTables.remove(table);
                }
            }
        }
        return selectedTables;
    }

    public abstract void setup();

    public abstract void executeTruncate();

}
