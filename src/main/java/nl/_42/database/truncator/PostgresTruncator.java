package nl._42.database.truncator;

import org.springframework.jdbc.support.MetaDataAccessException;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

public class PostgresTruncator extends AbstractDatabaseTruncator {

    public static final String PUBLIC_SCHEMA =              "public";
    public static final String TABLE_NAME_KEY =             "tablename";
    public static final String SEQUENCE_NAME_KEY =          "relname";
    public static final String SCHEMA_NAME_KEY =            "schemaname";

    public PostgresTruncator(DataSource dataSource, DatabaseTruncatorProperties properties) {
        super(dataSource, properties);
        createTruncateMethod();
    }

    private void createTruncateMethod() {
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
    }

    @Override
    protected  List<String> determineTables() {
        return jdbcTemplate.queryForList("SELECT * FROM pg_catalog.pg_tables").stream()
                .filter(tableInfo -> tableInfo.get(SCHEMA_NAME_KEY).equals(PUBLIC_SCHEMA))
                .map(tableInfo -> (String)tableInfo.get(TABLE_NAME_KEY))
                .filter(PostgresTruncator::isTruncatableTable)
                .collect(Collectors.toList());
    }

    @Override
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

    public void truncate() throws MetaDataAccessException {
        getTables().forEach(table -> jdbcTemplate.execute("SELECT truncate_tables('" + table + "')"));
//        getSequences().forEach(sequence -> jdbcTemplate.execute("ALTER SEQUENCE " + sequence + " RESTART WITH 1"));
    }

}
