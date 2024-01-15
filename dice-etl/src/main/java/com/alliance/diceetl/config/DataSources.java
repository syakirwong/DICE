package com.alliance.diceetl.config;

import com.alliance.diceetl.constants.DataSourceConstants;
import org.springframework.boot.autoconfigure.batch.BatchDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import javax.xml.crypto.Data;
import java.util.HashMap;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "entityManager",
        transactionManagerRef = "transactionManager",
        basePackages = "com.alliance.diceetl.repository"
)
public class DataSources {
//    private final String URL_PREFIX = "jdbc:db2://";
//    private final String VCC_DATASOURCE_URL = URL_PREFIX + "198.128.102.64" +":60000/VCCDB";
//    private final String AOP_DATASOURCE_URL = URL_PREFIX + "198.128.102.22" +":50447/aopdb";
//    private final String DBOBDB_DATASOURCE_URL = URL_PREFIX + "198.128.102.64" +":60000/DBOBDB";
//    private final String PLDB_DATASOURCE_URL = URL_PREFIX + "198.128.102.64" +":60000/pldb";

    private final String DB2_DRIVER_CLASS_NAME = "com.ibm.db2.jcc.DB2Driver";

//    private final String LOCAL_URL = "jdbc:postgresql://localhost:5432/yourDatabase";
//    private final String LOCAL_USERNAME = "yourUsername";
//    private final String LOCAL_PASSWORD = "yourPassword";
//    private final String LOCAL_DRIVER_NAME = "org.postgresql.Driver";

    @Bean(name = "vccDataSource") // ONBOARDING_FORMS_VIEW
    public DataSource vccDbDataSource(){
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
//        dataSource.setDriverClassName(LOCAL_DRIVER_NAME);
//        dataSource.setUrl(LOCAL_URL);
//        dataSource.setUsername(LOCAL_USERNAME);
//        dataSource.setPassword(LOCAL_PASSWORD);
        dataSource.setDriverClassName(DB2_DRIVER_CLASS_NAME);
        dataSource.setUrl(DataSourceConstants.VCC.DATASOURCE_URL);
        dataSource.setSchema(DataSourceConstants.VCC.SCHEMA);
        dataSource.setUsername(DataSourceConstants.VCC.USERNAME);
        dataSource.setPassword(DataSourceConstants.VCC.PASSWORD);
        return dataSource;
    }

    @Bean(name = "aopDataSource") // DDM_SOLE_CC_VIEW
    public DataSource aopDataSource(){
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
//        dataSource.setDriverClassName(LOCAL_DRIVER_NAME);
//        dataSource.setUrl(LOCAL_URL);
//        dataSource.setUsername(LOCAL_USERNAME);
//        dataSource.setPassword(LOCAL_PASSWORD);
        dataSource.setDriverClassName(DB2_DRIVER_CLASS_NAME);
        dataSource.setUrl(DataSourceConstants.AOP.DATASOURCE_URL);
        dataSource.setSchema(DataSourceConstants.AOP.SCHEMA);
        dataSource.setUsername(DataSourceConstants.AOP.USERNAME);
        dataSource.setPassword(DataSourceConstants.AOP.PASSWORD);

        // Additional SSL configuration specific to this data source
        Properties connectionProperties = new Properties();
        connectionProperties.setProperty("sslConnection", "true");
        connectionProperties.setProperty("sslTrustStoreLocation", "/ssl/UATAOPDBSSL.jks");
        connectionProperties.setProperty("sslTrustStorePassword", "UATAOPDBSSL");

        dataSource.setConnectionProperties(connectionProperties);
        return dataSource;
    }

    @Bean(name = "dbobDBDataSource") // INTERNET_BANKING_ACTIVATION_VIEW
    public DataSource dbobDBDataSource(){
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
//        dataSource.setDriverClassName(LOCAL_DRIVER_NAME);
//        dataSource.setUrl(LOCAL_URL);
//        dataSource.setUsername(LOCAL_USERNAME);
//        dataSource.setPassword(LOCAL_PASSWORD);
        dataSource.setDriverClassName(DB2_DRIVER_CLASS_NAME);
        dataSource.setUrl(DataSourceConstants.DBOBDB.DATASOURCE_URL);
        dataSource.setSchema(DataSourceConstants.DBOBDB.SCHEMA);
        dataSource.setUsername(DataSourceConstants.DBOBDB.USERNAME);
        dataSource.setPassword(DataSourceConstants.DBOBDB.PASSWORD);
        return dataSource;
    }

    @Bean(name = "plDbDataSource") // PLOAN_APPLICATION_VIEW
    public DataSource plDbDataSource(){
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
//        dataSource.setDriverClassName(LOCAL_DRIVER_NAME);
//        dataSource.setUrl(LOCAL_URL);
//        dataSource.setUsername(LOCAL_USERNAME);
//        dataSource.setPassword(LOCAL_PASSWORD);
        dataSource.setDriverClassName(DB2_DRIVER_CLASS_NAME);
        dataSource.setUrl(DataSourceConstants.PLDB.DATASOURCE_URL);
        dataSource.setSchema(DataSourceConstants.PLDB.SCHEMA);
        dataSource.setUsername(DataSourceConstants.PLDB.USERNAME);
        dataSource.setPassword(DataSourceConstants.PLDB.PASSWORD);
        return dataSource;
    }

    //Destination database which is DBOBDB also but different schema
    @Bean(name = "destinationDataSource")
    @Primary
    @BatchDataSource
    public DataSource destinationDataSource(){
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(DB2_DRIVER_CLASS_NAME);
        dataSource.setUrl(DataSourceConstants.DBOBDB.DATASOURCE_URL);
        dataSource.setSchema(DataSourceConstants.DBOBDB.DESTINATION_SCHEMA);
        dataSource.setUsername(DataSourceConstants.DBOBDB.USERNAME);
        dataSource.setPassword(DataSourceConstants.DBOBDB.PASSWORD);
        return dataSource;
    }

//    @Bean(name = "dataSource") // ETL_TEMP_DATABASE
//    @Primary
//    @BatchDataSource
//    public DataSource etlDataSource(){
//        DriverManagerDataSource dataSource = new DriverManagerDataSource();
//        dataSource.setDriverClassName(LOCAL_DRIVER_NAME);
//        dataSource.setUrl(LOCAL_URL);
//        dataSource.setUsername(LOCAL_USERNAME);
//        dataSource.setPassword(LOCAL_PASSWORD);
//        dataSource.setUrl(etlDataSourceUrl);
//        dataSource.setDriverClassName("com.ibm.db2.jcc.DB2Driver");
//        dataSource.setUsername("db2inst1");
//        dataSource.setPassword("db2inst1");
//        return dataSource;
//    }

    @Bean(name = "entityManager")
    @Primary
    public LocalContainerEntityManagerFactoryBean etlEntityManager(){
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(destinationDataSource());
        em.setPackagesToScan("com.alliance.diceetl.entity");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        HashMap<String,Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto","update");

        em.setJpaPropertyMap(properties);
        return em;
    }

    @Bean(name = "transactionManager")
    @Primary
    public PlatformTransactionManager etlTransactionManager(){
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(etlEntityManager().getObject());
        return transactionManager;
    }
}
