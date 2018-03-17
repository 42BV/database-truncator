package nl._42.database.truncator.mariadb;

import nl._42.database.truncator.config.DatabaseTruncatorProperties;
import nl._42.database.truncator.shared.AbstractTruncationStrategy;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MariaDbTruncationStrategy extends AbstractTruncationStrategy {

    private final List<String> tablesToTruncate = new ArrayList<>();

    public MariaDbTruncationStrategy(DataSource dataSource, DatabaseTruncatorProperties properties) {
        super(dataSource, properties);
    }

    @Override
    public void setup() {
        // Get all tables in the database
        List<String> tablesInDatabase = getTablesInDatabase(getDatabaseName());

        // Remove tables which have been excluded from the config
        List<String> tablesToTruncate = super.filterExcludedTables(tablesInDatabase);

        // Remove Liquibase tables and add remaining tables to the stored list of tables.
        for (String tableName : tablesToTruncate) {
            if (isTruncatableTable(tableName)) {
                this.tablesToTruncate.add(tableName);
            }
        }
    }

    @Override
    public void executeTruncate() {
        for (String tableName : this.tablesToTruncate) {
            // Count if there are any items in the database table.
            int amountOfItems = jdbcTemplate.query(String.format("SELECT COUNT(*) FROM %s;", tableName), resultSet -> {
                int count = 0;

                if (resultSet.next()) {
                    count = resultSet.getInt(1);
                }

                return count;
            });

            // If there are items, go ahead and truncate the table.
            if (amountOfItems > 0) {
                jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0;");
                jdbcTemplate.execute(String.format("TRUNCATE TABLE %s;", tableName));
                jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1;");
            }
        }
    }

    private String getDatabaseName() {
        try {
            return jdbcTemplate.getDataSource().getConnection().getCatalog();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private List<String> getTablesInDatabase(String databaseName) {
        return jdbcTemplate.query("SELECT table_name " +
                "FROM INFORMATION_SCHEMA.TABLES " +
                "WHERE TABLE_SCHEMA = '" + databaseName + "' " +
                "AND TABLE_TYPE = 'BASE TABLE';", (resultSet, i) -> resultSet.getString(1));
    }

    private boolean isTruncatableTable(String tableName) {
        return !tableName.equalsIgnoreCase(LIQUIBASE_CHANGE_LOG) && !tableName.equalsIgnoreCase(LIQUIBASE_CHANGE_LOG_LOCK);
    }
}
