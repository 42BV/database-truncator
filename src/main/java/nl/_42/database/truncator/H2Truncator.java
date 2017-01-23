package nl._42.database.truncator;

import org.springframework.jdbc.support.MetaDataAccessException;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class H2Truncator extends AbstractDatabaseTruncator {

    public static final String TABLE_NAME_KEY = "TABLE_NAME";

    List<String> tables = new ArrayList<>();

    public H2Truncator(DataSource dataSource) {
        super(dataSource);
        determineTables();
    }

    private void determineTables() {
        this.tables.addAll(jdbcTemplate.queryForList("SHOW TABLES").stream()
                .map(tableInfo -> (String)tableInfo.get(TABLE_NAME_KEY))
                .filter(tableName -> !tableName.equals(LIQUIBASE_CHANGE_LOG) && !tableName.equals(LIQUIBASE_CHANGE_LOG_LOCK))
                .collect(Collectors.toList()));
    }

    public void truncate() throws MetaDataAccessException {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE;");
        tables.forEach(table -> jdbcTemplate.execute("TRUNCATE TABLE " + table));
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE;");
    }

}
