package com.alliance.diceintegration.configurer;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

// @Configuration
// public class JpaConfig {

//     @Autowired
//     private DataSource dataSource;

//     @Bean
//     public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
//         LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
//         emf.setDataSource(dataSource);
//         emf.setPackagesToScan("com.alliance.diceintegration.model");

//         HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
//         emf.setJpaVendorAdapter(vendorAdapter);

//         return emf;
//     }

//     @Bean
//     public JpaTransactionManager transactionManager(EntityManagerFactory emf) {
//         JpaTransactionManager transactionManager = new JpaTransactionManager();
//         transactionManager.setEntityManagerFactory(emf);
//         return transactionManager;
//     }

    

// }
@Configuration
public class JpaConfig {

    // Define the first EntityManagerFactory and TransactionManager beans
    @Primary
    @Bean(name = "dbobdbEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean dbobdbEntityManagerFactory(@Qualifier("dbobdbDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("com.alliance.diceintegration.model");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        emf.setJpaVendorAdapter(vendorAdapter);

        return emf;
    }

    @Primary
    @Bean(name = "dbobdbTransactionManager")
    public JpaTransactionManager dbobdbTransactionManager(@Qualifier("dbobdbEntityManagerFactory") EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }

    // Define the second EntityManagerFactory and TransactionManager beans
    @Bean(name = "vccdbEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean vccdbEntityManagerFactory(@Qualifier("vccdbDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("com.alliance.diceintegration.model");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        emf.setJpaVendorAdapter(vendorAdapter);

        return emf;
    }

    @Bean(name = "vccdbTransactionManager")
    public JpaTransactionManager vccdbTransactionManager(@Qualifier("vccdbEntityManagerFactory") EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }

    // Define other beans as needed...

}

