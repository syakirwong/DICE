package com.alliance.dicecampaign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

import com.alliance.dicecampaign.constant.CassandraConnection;
import com.alliance.dicecampaign.utility.SystemParam;

import lombok.extern.slf4j.Slf4j;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Slf4j
@EnableSwagger2
@SpringBootApplication
@EnableAutoConfiguration(exclude = {BatchAutoConfiguration.class, DataSourceAutoConfiguration.class})
@EnableCassandraRepositories(basePackages = "com.alliance.dicecampaign.repository")
public class MainApplication {

	public static void main(String[] args) {
		SpringApplication.run(MainApplication.class, args);
        SystemParam.getInstance();
        CassandraConnection.getInstance();
        log.info("---CAMPAIGN SERVICE STARTED---");
	}
    
    @Bean
    Docket productApi() {
        return new Docket(DocumentationType.SWAGGER_2).select()
                .apis(RequestHandlerSelectors.basePackage("com.alliance")).build();
    }
}



