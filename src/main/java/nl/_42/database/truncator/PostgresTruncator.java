package nl._42.database.truncator;

import org.springframework.jdbc.support.MetaDataAccessException;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PostgresTruncator extends AbstractDatabaseTruncator {

    public static final String SQL_GET_TABLENAMES =         "SELECT * FROM pg_catalog.pg_tables";
    public static final String PUBLIC_SCHEMA =              "public";
    public static final String TABLE_NAME_KEY =             "tablename";
    public static final String SCHEMA_NAME_KEY =            "schemaname";

    private final List<String> tables = new ArrayList<>();

    public PostgresTruncator(DataSource dataSource) {
        super(dataSource);
        determineTables();
    }

    private void determineTables() {
        jdbcTemplate.execute("CREATE OR REPLACE FUNCTION truncate_tables(_username text)\n" +
                "  RETURNS void AS\n" +
                "$func$\n" +
                "BEGIN\n" +
                "   RAISE NOTICE '%', \n" +
                "   -- EXECUTE  -- dangerous, test before you execute!\n" +
                "  (SELECT 'TRUNCATE TABLE '\n" +
                "       || string_agg(quote_ident(schemaname) || '.' || quote_ident(tablename), ', ')\n" +
                "       || ' CASCADE'\n" +
                "   FROM   pg_tables\n" +
                "   WHERE  tableowner = _username\n" +
                "   AND    schemaname = 'public'\n" +
                "   );\n" +
                "END\n" +
                "$func$ LANGUAGE plpgsql;");

        List<Map<String,Object>> result = jdbcTemplate.queryForList(SQL_GET_TABLENAMES);

        this.tables.addAll(jdbcTemplate.queryForList(SQL_GET_TABLENAMES).stream()
                .filter(tableInfo -> tableInfo.get(SCHEMA_NAME_KEY).equals(PUBLIC_SCHEMA))
                .map(tableInfo -> (String)tableInfo.get(TABLE_NAME_KEY))
                .filter(PostgresTruncator::isTruncatableTable)
                .collect(Collectors.toList()));
    }

    private static boolean isTruncatableTable(String tableName) {
        return
                !tableName.equals(LIQUIBASE_CHANGE_LOG) &&
                !tableName.equals(LIQUIBASE_CHANGE_LOG_LOCK);
    }

    public void truncate() throws MetaDataAccessException {
        tables.forEach(table -> jdbcTemplate.execute("SELECT truncate_tables('" + table + "')"));
    }

}
