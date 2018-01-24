package nl._42.database.truncator.postgres;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import nl._42.database.truncator.config.DatabaseTruncatorProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostgresOptimizedDeletionStrategy extends AbstractPostgresTruncationStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresOptimizedDeletionStrategy.class);

    private Map<String, Set<String>> tableConnectedTo = new HashMap<>();

    private String tablesToTruncateQuery;

    public PostgresOptimizedDeletionStrategy(DataSource dataSource, DatabaseTruncatorProperties properties) {
        super(dataSource, properties);
    }

    @Override
    public void setup() {
        super.setup();

        tableConnectedTo = determineForeignKeys();
        for (String foreignTable : tableConnectedTo.keySet()) {
            LOGGER.debug("table [" + foreignTable + "] referred to by [" + String.join(", ", tableConnectedTo.get(foreignTable)) + "]");
        }

        tablesToTruncateQuery =
                "SELECT table_name FROM ( " +
                  tables.stream()
                        .map(table -> "SELECT '" + table + "' AS table_name, COUNT(*) AS count_val FROM " + table)
                        .collect(Collectors.joining(" UNION ALL ")) +
                ") x where count_val > 0";
    }

    private Map<String, Set<String>> determineForeignKeys() {

        String fetchforeignKeySql =
                "SELECT" +
                "    tc.constraint_name, tc.table_name, kcu.column_name," +
                "    ccu.table_name AS foreign_table_name," +
                "    ccu.column_name AS foreign_column_name " +
                "FROM" +
                "    information_schema.table_constraints AS tc" +
                "    JOIN information_schema.key_column_usage AS kcu" +
                "      ON tc.constraint_name = kcu.constraint_name" +
                "    JOIN information_schema.constraint_column_usage AS ccu" +
                "      ON ccu.constraint_name = tc.constraint_name " +
                "WHERE constraint_type = 'FOREIGN KEY';";

        List<Map<String, Object>> records = jdbcTemplate.queryForList(fetchforeignKeySql);

        Map<String, Set<String>> tableConnectedTo = new HashMap<>();

        for (Map<String, Object> record : records) {
            String tableName = (String)record.get("table_name");
            String foreignTableName = (String)record.get("foreign_table_name");

            Set<String> connectedTables = tableConnectedTo.computeIfAbsent(foreignTableName, k -> new HashSet<>());
            connectedTables.add(tableName);
        }

        return tableConnectedTo;
    }

    protected List<String> tablesToTruncate() {
        return jdbcTemplate.queryForList(tablesToTruncateQuery, String.class);
    }

    protected Set<String> tablesToHaveTriggersDisabled(List<String> tablesToTruncate) {
        Set<String> tablesToHaveTriggersDisabled = new HashSet<>();
        for (String foreignTable : tablesToTruncate) {
            Set<String> connectedTables = tableConnectedTo.get(foreignTable);
            if (connectedTables != null) {
                tablesToHaveTriggersDisabled.addAll(connectedTables);
            }
        }
        tablesToHaveTriggersDisabled.addAll(tablesToTruncate);
        return tablesToHaveTriggersDisabled;
    }

    @Override
    public void executePostgresTruncate() {
        List<String> tablesToTruncate = tablesToTruncate();
        LOGGER.debug("Truncate tables: " + String.join(", ", tablesToTruncate));
        Set<String> tablesToHaveTriggersDisabled = tablesToHaveTriggersDisabled(tablesToTruncate);
        LOGGER.debug("Disable triggers for: " + String.join(", ", tablesToHaveTriggersDisabled));
        executeSql(tablesToHaveTriggersDisabled,"disable triggers", "ALTER TABLE %s DISABLE TRIGGER ALL;");
        executeSql(tablesToTruncate,"delete tables", "DELETE FROM %s;");
        executeSql(tablesToHaveTriggersDisabled,"enable triggers", "ALTER TABLE %s ENABLE TRIGGER ALL;");
    }
}
