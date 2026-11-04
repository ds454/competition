package ds454.locomotive.competition.spring;

import jakarta.persistence.EntityManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableJpaRepositories(basePackages = "ds454.locomotive.competition.repository")
@EnableTransactionManagement
public class PersistenceConfig {

    private static final Logger log = LoggerFactory.getLogger(PersistenceConfig.class);

    @Bean
    public DataSource dataSource() {

        String dbUser = System.getenv().get("POSTGRES_USER");
        String dbPass = System.getenv().get("POSTGRES_PASSWORD");
        String dbName = System.getenv().get("POSTGRES_DB");
        String pgPort = System.getenv().get("PGPORT");
        String dbHost = System.getenv().get("DBHOST");

        if (dbUser == null || dbUser.isBlank()) {
            log.error("Missing required env variable 'POSTGRES_USER'");
            System.exit(-1);
        }
        if (dbPass == null || dbPass.isBlank()) {
            log.error("Missing required env variable 'POSTGRES_PASSWORD'");
            System.exit(-1);
        }
        if (dbName == null || dbName.isBlank()) {
            log.error("Missing required env variable 'POSTGRES_DB'");
            System.exit(-1);
        }
        if (pgPort == null || pgPort.isBlank()) {
            log.error("Missing required env variable 'PGPORT'");
            System.exit(-1);
        }
        if (dbHost == null || dbHost.isBlank()) {
            log.error("Missing required env variable 'DBHOST'");
            System.exit(-1);
        }

        String url = String.format("jdbc:postgresql://%s:%s/%s", dbHost, pgPort, dbName);

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(url);
        dataSource.setUsername(dbUser);
        dataSource.setPassword(dbPass);
        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan("ds454.locomotive.competition.model");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Properties properties = new Properties();
        // Automatically updates table schemas
        properties.setProperty("hibernate.hbm2ddl.auto", "update");

        // Use postgresql
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");

        // Log generated SQL queries
        properties.setProperty("hibernate.show_sql", "true");
        em.setJpaProperties(properties);

        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }
}