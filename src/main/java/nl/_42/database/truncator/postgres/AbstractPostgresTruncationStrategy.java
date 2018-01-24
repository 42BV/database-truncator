package nl._42.database.truncator.postgres;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import nl._42.database.truncator.config.DatabaseTruncatorProperties;
import nl._42.database.truncator.shared.AbstractTruncationStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

public abstract class AbstractPostgresTruncationStrategy extends AbstractTruncationStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPostgresTruncationStrategy.class);

    public static final String PUBLIC_SCHEMA =              "public";
    public static final String TABLE_NAME_KEY =             "tablename";
    public static final String SEQUENCE_NAME_KEY =          "relname";
    public static final String SCHEMA_NAME_KEY =            "schemaname";

    protected List<String> tables;
    protected List<String> sequences;

    public AbstractPostgresTruncationStrategy(DataSource dataSource, DatabaseTruncatorProperties properties) {
        super(dataSource, properties);
    }

    @Override
    public void setup() {
        logList("Exclude prefixes", properties.getExclude());
        this.tables = filterExcludedTables(determineTables());
        logList("Tables to truncate", tables);
        this.sequences = determineSequences();
        logList("Sequences to truncate", sequences);
    }

    public void executeTruncate() {
        executePostgresTruncate();
        if (properties.getResetSequences()) {
            sequences.forEach(sequence -> jdbcTemplate.execute("ALTER SEQUENCE " + sequence + " RESTART WITH 1"));
        }
    }

    public abstract void executePostgresTruncate();

    protected  List<String> determineTables() {
    return jdbcTemplate.queryForList("SELECT * FROM pg_catalog.pg_tables").stream()
            .filter(tableInfo -> tableInfo.get(SCHEMA_NAME_KEY).equals(PUBLIC_SCHEMA))
            .map(tableInfo -> (String)tableInfo.get(TABLE_NAME_KEY))
            .filter(AbstractPostgresTruncationStrategy::isTruncatableTable)
            .collect(Collectors.toList());
    }

    protected List<String> determineSequences() {
        String query = "SELECT c.relname FROM pg_class c WHERE c.relkind = 'S'";
        return jdbcTemplate.queryForList(query).stream()
                .map(sequenceInfo -> (String) sequenceInfo.get(SEQUENCE_NAME_KEY))
                .collect(Collectors.toList());
    }

    private static boolean isTruncatableTable(String tableName) {
        return
                !tableName.equals(LIQUIBASE_CHANGE_LOG) &&
                !tableName.equals(LIQUIBASE_CHANGE_LOG_LOCK);
    }

    void executeSql(Collection<String> records, String title, String sql) {
        StopWatch stopWatch = null;
        if (title != null) {
            stopWatch = new StopWatch();
            stopWatch.start();
        }

        String query =
                "START TRANSACTION;\n" +
                    records.stream()
                           .map(record -> String.format(sql, record))
                           .collect(Collectors.joining("\n")) +
                "\nCOMMIT;";
        jdbcTemplate.execute(query);

        if (stopWatch != null) {
            stopWatch.stop();
            LOGGER.debug(title + " took: " + stopWatch.getTotalTimeMillis() + " ms");
        }
    }
}
