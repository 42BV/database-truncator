package nl._42.database.truncator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.AntPathMatcher;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractDatabaseTruncator implements DatabaseTruncator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDatabaseTruncator.class);

    public static final String LIQUIBASE_CHANGE_LOG =       "databasechangelog";
    public static final String LIQUIBASE_CHANGE_LOG_LOCK =  "databasechangeloglock";

    protected final JdbcTemplate jdbcTemplate;

    protected final DatabaseTruncatorProperties properties;

    private final List<String> tables;
    private final List<String> sequences;

    AbstractDatabaseTruncator(DataSource dataSource, DatabaseTruncatorProperties properties) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.properties = properties;
        logList("Exclude prefixes", properties.getExclude());
        tables = filterExcludedTables(determineTables());
        logList("Tables to truncate", getTables());
        sequences = determineSequences();
        logList("Sequences to truncate", getSequences());
    }

    private void logList(String title, List<String> list) {
        LOGGER.debug(title);
        for (String item : list) {
            LOGGER.debug("- " + item);
        }
    }

    protected List<String> filterExcludedTables(List<String> tables) {
        AntPathMatcher matcher = new AntPathMatcher();
        List<String> selectedTables = new ArrayList<String>(tables);
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

    private boolean mustBeExcluded(String table) {
        return properties.getExclude().contains(table);
    }

    protected List<String> determineTables() {
        return Collections.emptyList();
    }

    protected List<String> determineSequences() {
        return Collections.emptyList();
    }

    public List<String> getTables() {
        return tables;
    }

    public List<String> getSequences() {
        return sequences;
    }

}
