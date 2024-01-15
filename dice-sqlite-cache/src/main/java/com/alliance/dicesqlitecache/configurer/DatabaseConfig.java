package com.alliance.dicesqlitecache.configurer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(basePackages = "com.alliance.dicesqlitecache.repository", entityManagerFactoryRef = "sqliteEntityManagerFactory", transactionManagerRef = "sqliteTransactionManager")
public class DatabaseConfig {

    @Value("${spring.datasource.db2.url}")
    private String DB2_DATASOURCE_URL;

    @Value("${spring.datasource.db2.username}")
    private String DB2_DATASOURCE_USERNAME;

    @Value("${spring.datasource.db2.password}")
    private String DB2_DATASOURCE_PASSWORD;

    @Value("${spring.datasource.url}")
    private String SQLITE_DATASOURCE_URL;

    @Bean
    public EntityManagerFactoryBuilder entityManagerFactoryBuilder() {
        return new EntityManagerFactoryBuilder(new HibernateJpaVendorAdapter(), new HashMap<>(), null);
    }

    @Bean(name = "sqliteDataSource")
    public DataSource sqliteDataSource() {
        return DataSourceBuilder.create()
                .url(SQLITE_DATASOURCE_URL)
                .driverClassName("org.sqlite.JDBC")
                .build();
    }

    @Bean(name = "sqliteEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean sqliteEntityManagerFactory(
            EntityManagerFactoryBuilder builder, DataSource sqliteDataSource) {
        LocalContainerEntityManagerFactoryBean em = builder
                .dataSource(sqliteDataSource)
                .packages("com.alliance.dicesqlitecache.model")
                .persistenceUnit("sqlite")
                .properties(additionalProperties())
                .build();
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        return em;
    }

    private Map<String, Object> additionalProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("spring.jpa.database-platform", "org.hibernate.community.dialect.SQLiteDialect");
        properties.put("spring.jpa.hibernate.ddl-auto", "update");
        properties.put("hibernate.hbm2ddl.auto", "create");

        return properties;
    }

    @Bean(name = "sqliteTransactionManager")
    public JpaTransactionManager sqliteTransactionManager(
            EntityManagerFactory sqliteEntityManagerFactory) {
        return new JpaTransactionManager(sqliteEntityManagerFactory);
    }

    // Additional Configuration for DB2 (Secondary Database)

    @Bean(name = "db2DataSourceProperties")
    public DataSourceProperties db2DataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "db2DataSource")
    public DataSource db2DataSource(DataSourceProperties db2DataSourceProperties) {
        return DataSourceBuilder.create()
                .url(DB2_DATASOURCE_URL)
                .driverClassName("com.ibm.db2.jcc.DB2Driver")
                .username(DB2_DATASOURCE_USERNAME)
                .password(DB2_DATASOURCE_PASSWORD)
                .build();
    }

    @Bean(name = "db2EntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean db2EntityManagerFactory(
            EntityManagerFactoryBuilder builder, DataSource db2DataSource) {
        Map<String, Object> properties = new HashMap<>();
        // Customize properties if needed

        return builder
                .dataSource(db2DataSource)
                .packages("com.alliance.dicesqlitecache.model")
                .persistenceUnit("db2")
                .properties(properties)
                .build();
    }

    @Bean(name = "db2TransactionManager")
    public JpaTransactionManager db2TransactionManager(
            EntityManagerFactory db2EntityManagerFactory) {
        return new JpaTransactionManager(db2EntityManagerFactory);
    }

    @Bean(name = "db2JdbcTemplate")
    public JdbcTemplate db2JdbcTemplate(DataSource db2DataSource) {
        return new JdbcTemplate(db2DataSource);
    }

    @Bean(name = "sqliteJdbcTemplate")
    public JdbcTemplate sqliteJdbcTemplate(DataSource sqliteDataSource) {
        return new JdbcTemplate(sqliteDataSource);
    }

}
