package nl._42.database.truncator;

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
public class DatabaseTruncatorAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseTruncatorAutoConfiguration.class);

    @Configuration
    @ConditionalOnMissingBean(DatabaseTruncator.class)
    @EnableConfigurationProperties(DataSourceProperties.class)
    public static class DatabaseTruncatorConfiguration {

        private final DataSource dataSource;

        private final DataSourceProperties dataSourceProperties;

        @SuppressWarnings("SpringJavaAutowiringInspection")
        public DatabaseTruncatorConfiguration(DataSource dataSource, DataSourceProperties dataSourceProperties) {
            LOGGER.info("Configuring DatabaseTruncator");
            this.dataSource = dataSource;
            this.dataSourceProperties = dataSourceProperties;
        }

        @Bean
        public DatabaseTruncator databaseTruncator() {
            String platform = dataSourceProperties.getPlatform();
            if (platform == null || platform.equals("")) {
                LOGGER.error("spring.datasource.platform does not have a valid value");
                throw new ExceptionInInitializerError(
                        "The Database Truncator cannot be configured. Set spring.datasource.platform value to non-empty, valid value");
            }
            if (platform.equals("postgresql")) {
                logCreation(platform,"Postgres Truncator");
                return new PostgresTruncator(dataSource);
            } if (platform.equals("h2")) {
                logCreation(platform,"H2 Truncator");
                return new H2Truncator(dataSource);
            } if (platform.equals("hsqldb")) {
                logCreation(platform,"HSQLDB Truncator");
                return new HsqldbTruncator(dataSource);
            }
            LOGGER.error("spring.datasource.platform does not have a valid value");
            throw new ExceptionInInitializerError(
                    "The Database Truncator cannot be configured. Set [spring.datasource.platform=" + platform + "] value to valid value");
        }

        private void logCreation(String platform, String title) {
            LOGGER.info("Platform [" + platform + "] > Creating " + title);
        }

    }

}
