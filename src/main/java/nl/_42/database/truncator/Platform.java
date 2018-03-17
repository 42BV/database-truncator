package nl._42.database.truncator;

import nl._42.database.truncator.config.DatabaseTruncatorProperties;
import nl._42.database.truncator.shared.AbstractTruncationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

import static nl._42.database.truncator.TruncationStrategy.*;

public enum Platform {
    H2(H2_TRUNCATION),
    HSQLDB(HSQLDB_TRUNCATION),
    POSTGRESQL(POSTGRES_DELETION_OPTIMIZED, POSTGRES_DELETION, POSTGRES_TRUNCATION),
    MYSQL(MARIADB_TRUNCATION),
    MARIADB(MARIADB_TRUNCATION);

    private static final Logger LOGGER = LoggerFactory.getLogger(Platform.class);

    private final String platformText;

    private final List<TruncationStrategy> applicableStrategies;

    private final TruncationStrategy defaultStrategy;

    Platform(TruncationStrategy... applicableStrategies) {
        this.platformText = name().toLowerCase();
        this.applicableStrategies = Arrays.asList(applicableStrategies);
        this.defaultStrategy = applicableStrategies[0];
    }

    public static Platform determinePlatform(String platformText) {
        for (Platform platform : values()) {
            if (platform.platformText.equals(platformText)) {
                return platform;
            }
        }
        throw new ExceptionInInitializerError(
                "The Database Truncator cannot be configured. Illegal platform: " + platformText + ". Set " +
                "spring.datasource.platform value to non-empty, valid value");
    }

    public TruncationStrategy determineStrategy(TruncationStrategy strategy) {
        if (strategy != null) {
            if (!applicableStrategies.contains(strategy)) {
                LOGGER.warn(
                        "The strategy : " + strategy.name() + " is not applicable for Platform " + name() + ". " +
                        "Choosing default strategy " + defaultStrategy.name());
                return defaultStrategy;
            }
            return strategy;
        }
        return defaultStrategy;
    }

    public DatabaseTruncator createTruncator(DataSource dataSource,
                                             DatabaseTruncatorProperties databaseTruncatorProperties) {
        TruncationStrategy strategy = determineStrategy(databaseTruncatorProperties.getStrategy());
        LOGGER.info("Creating database truncator [" + strategy.name() + "] for platform [" + name() + "]");
        AbstractTruncationStrategy truncator = strategy.createTruncator(dataSource, databaseTruncatorProperties);
        truncator.setup();
        return truncator;
    }

}
