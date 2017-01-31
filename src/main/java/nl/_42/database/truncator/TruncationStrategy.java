package nl._42.database.truncator;

import nl._42.database.truncator.config.DatabaseTruncatorProperties;
import nl._42.database.truncator.h2.H2TruncationStrategy;
import nl._42.database.truncator.hsqldb.HsqldbTruncationStrategy;
import nl._42.database.truncator.postgres.PostgresDeletionStrategy;
import nl._42.database.truncator.postgres.PostgresOptimizedDeletionStrategy;
import nl._42.database.truncator.postgres.PostgresTruncationStrategy;
import nl._42.database.truncator.shared.AbstractTruncationStrategy;

import javax.sql.DataSource;

public enum TruncationStrategy {
    H2_TRUNCATION {
        @Override
        public AbstractTruncationStrategy createTruncator(DataSource dataSource, DatabaseTruncatorProperties databaseTruncatorProperties) {
            return new H2TruncationStrategy(dataSource, databaseTruncatorProperties);
        }
    },
    HSQLDB_TRUNCATION {
        @Override
        public AbstractTruncationStrategy createTruncator(DataSource dataSource, DatabaseTruncatorProperties databaseTruncatorProperties) {
            return new HsqldbTruncationStrategy(dataSource, databaseTruncatorProperties);
        }
    },
    POSTGRES_TRUNCATION {
        @Override
        public AbstractTruncationStrategy createTruncator(DataSource dataSource, DatabaseTruncatorProperties databaseTruncatorProperties) {
            return new PostgresTruncationStrategy(dataSource, databaseTruncatorProperties);
        }
    },
    POSTGRES_DELETION {
        @Override
        public AbstractTruncationStrategy createTruncator(DataSource dataSource, DatabaseTruncatorProperties databaseTruncatorProperties) {
            return new PostgresDeletionStrategy(dataSource, databaseTruncatorProperties);
        }
    },
    POSTGRES_DELETION_OPTIMIZED {
        @Override
        public AbstractTruncationStrategy createTruncator(DataSource dataSource, DatabaseTruncatorProperties databaseTruncatorProperties) {
            return new PostgresOptimizedDeletionStrategy(dataSource, databaseTruncatorProperties);
        }
    };

    public abstract AbstractTruncationStrategy createTruncator(
            DataSource dataSource,
            DatabaseTruncatorProperties databaseTruncatorProperties);
}
