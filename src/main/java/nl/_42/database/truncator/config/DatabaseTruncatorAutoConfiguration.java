package nl._42.database.truncator.config;

import javax.sql.DataSource;

import nl._42.database.truncator.DatabaseTruncator;
import nl._42.database.truncator.Platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBean(DataSource.class)
@ConditionalOnProperty(value = "spring.sql.init.platform", matchIfMissing = true)
@AutoConfigureAfter({ DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
@EnableConfigurationProperties(DatabaseTruncatorProperties.class)
public class DatabaseTruncatorAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseTruncatorAutoConfiguration.class);

    @Configuration
    @ConditionalOnMissingBean(DatabaseTruncator.class)
    @EnableConfigurationProperties({DataSourceProperties.class, DatabaseTruncatorProperties.class})
    public static class DatabaseTruncatorConfiguration {

        private final DataSource dataSource;

        private final SqlInitializationProperties sqlInitializationProperties;

        private final DatabaseTruncatorProperties databaseTruncatorProperties;

        @SuppressWarnings("SpringJavaAutowiringInspection")
        public DatabaseTruncatorConfiguration(DataSource dataSource, SqlInitializationProperties sqlInitializationProperties,
                DatabaseTruncatorProperties databaseTruncatorProperties) {
            LOGGER.info("Configuring DatabaseTruncator");
            this.dataSource = dataSource;
            this.sqlInitializationProperties = sqlInitializationProperties;
            this.databaseTruncatorProperties = databaseTruncatorProperties;
        }

        @Bean
        public DatabaseTruncator databaseTruncator() {
            Platform platform = Platform.determinePlatform(sqlInitializationProperties.getPlatform());
            return platform.createTruncator(dataSource, databaseTruncatorProperties);
        }

    }

}
