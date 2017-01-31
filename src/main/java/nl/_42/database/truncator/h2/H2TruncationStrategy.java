package nl._42.database.truncator.h2;

import nl._42.database.truncator.config.DatabaseTruncatorProperties;
import nl._42.database.truncator.shared.AbstractTruncationStrategy;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

public class H2TruncationStrategy extends AbstractTruncationStrategy {

    public static final String TABLE_NAME_KEY       = "TABLE_NAME";
    public static final String SEQUENCE_NAME_KEY    = "SEQUENCE_NAME";

    private List<String> tables;
    private List<String> sequences;

    public H2TruncationStrategy(DataSource dataSource, DatabaseTruncatorProperties properties) {
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

    protected List<String> determineTables() {
        return jdbcTemplate.queryForList("SHOW TABLES").stream()
                .map(tableInfo -> (String)tableInfo.get(TABLE_NAME_KEY))
                .filter(tableName -> !tableName.equals(LIQUIBASE_CHANGE_LOG) && !tableName.equals(LIQUIBASE_CHANGE_LOG_LOCK))
                .collect(Collectors.toList());
    }

    protected List<String> determineSequences() {
        String query = "SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_SCHEMA='PUBLIC'";
        return jdbcTemplate.queryForList(query).stream()
                .map(sequenceInfo -> (String) sequenceInfo.get(SEQUENCE_NAME_KEY))
                .collect(Collectors.toList());
    }

    public void executeTruncate() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE;");
        tables.forEach(table -> jdbcTemplate.execute("TRUNCATE TABLE " + table));
        sequences.forEach(sequence -> jdbcTemplate.execute("ALTER SEQUENCE " + sequence + " RESTART WITH 1"));
        if (properties.getResetSequences()) {
            jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE;");
        }
    }

}
