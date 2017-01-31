package nl._42.database.truncator.config;

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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ConditionalOnBean(DataSource.class)
@ConditionalOnProperty(value= "spring.datasource.platform", matchIfMissing = true)
@AutoConfigureAfter({ DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
@EnableConfigurationProperties(DatabaseTruncatorProperties.class)
public class DatabaseTruncatorAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseTruncatorAutoConfiguration.class);

    @Configuration
    @ConditionalOnMissingBean(DatabaseTruncator.class)
    @EnableConfigurationProperties({DataSourceProperties.class, DatabaseTruncatorProperties.class})
    public static class DatabaseTruncatorConfiguration {

        private final DataSource dataSource;

        private final DataSourceProperties dataSourceProperties;

        private final DatabaseTruncatorProperties databaseTruncatorProperties;

        @SuppressWarnings("SpringJavaAutowiringInspection")
        public DatabaseTruncatorConfiguration(DataSource dataSource, DataSourceProperties dataSourceProperties,
                                              DatabaseTruncatorProperties databaseTruncatorProperties) {
            LOGGER.info("Configuring DatabaseTruncator");
            this.dataSource = dataSource;
            this.dataSourceProperties = dataSourceProperties;
            this.databaseTruncatorProperties = databaseTruncatorProperties;
        }

        @Bean
        public DatabaseTruncator databaseTruncator() {
            Platform platform = Platform.determinePlatform(dataSourceProperties.getPlatform());
            return platform.createTruncator(dataSource, databaseTruncatorProperties);
        }

    }

}
