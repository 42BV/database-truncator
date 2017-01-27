package nl._42.database.truncator;

import org.springframework.jdbc.support.MetaDataAccessException;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

public class H2Truncator extends AbstractDatabaseTruncator {

    public static final String TABLE_NAME_KEY = "TABLE_NAME";
    public static final String SEQUENCE_NAME_KEY = "SEQUENCE_NAME";

    public H2Truncator(DataSource dataSource, DatabaseTruncatorProperties properties) {
        super(dataSource, properties);
    }

    @Override
    protected List<String> determineTables() {
        return jdbcTemplate.queryForList("SHOW TABLES").stream()
                .map(tableInfo -> (String)tableInfo.get(TABLE_NAME_KEY))
                .filter(tableName -> !tableName.equals(LIQUIBASE_CHANGE_LOG) && !tableName.equals(LIQUIBASE_CHANGE_LOG_LOCK))
                .collect(Collectors.toList());
    }

    @Override
    protected List<String> determineSequences() {
        String query = "SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_SCHEMA='PUBLIC'";
        return jdbcTemplate.queryForList(query).stream()
                .map(sequenceInfo -> (String) sequenceInfo.get(SEQUENCE_NAME_KEY))
                .collect(Collectors.toList());
    }

    public void truncate() throws MetaDataAccessException {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE;");
        getTables().forEach(table -> jdbcTemplate.execute("TRUNCATE TABLE " + table));
        getSequences().forEach(sequence -> jdbcTemplate.execute("ALTER SEQUENCE " + sequence + " RESTART WITH 1"));
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE;");
    }

}
