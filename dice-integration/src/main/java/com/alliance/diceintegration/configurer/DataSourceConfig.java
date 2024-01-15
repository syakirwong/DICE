package com.alliance.diceintegration.configurer;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

// @Configuration
// public class DataSourceConfig {

//     @Bean
//     public DataSource dataSource(DataSourceProperties dataSourceProperties) {
//         return DataSourceBuilder.create()
//                 .url(dataSourceProperties.getUrl())
//                 .username(dataSourceProperties.getUsername())
//                 .password(dataSourceProperties.getPassword())
//                 .driverClassName(dataSourceProperties.getDriverClassName())
//                 .build();
//     }

// }

@Configuration
public class DataSourceConfig {

    @Primary
    @Bean(name = "dbobdbDataSourceProperties")
    @ConfigurationProperties(prefix = "spring.datasource.dbobdb")
    public DataSourceProperties dbobdbDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean(name = "dbobdbDataSource")
    public DataSource dbobdbDataSource(@Qualifier("dbobdbDataSourceProperties") DataSourceProperties dataSourceProperties) {
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }

    @Bean(name = "vccdbDataSourceProperties")
    @ConfigurationProperties(prefix = "spring.datasource.vccdb")
    public DataSourceProperties vccdbDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "vccdbDataSource")
    public DataSource vccdbDataSource(@Qualifier("vccdbDataSourceProperties") DataSourceProperties dataSourceProperties) {
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }
}

