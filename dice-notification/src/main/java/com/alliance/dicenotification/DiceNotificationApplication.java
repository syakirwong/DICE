package com.alliance.dicenotification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

import com.alliance.dicenotification.constant.CassandraConnection;
import com.alliance.dicenotification.utility.SystemParam;

import lombok.extern.slf4j.Slf4j;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Slf4j
@EnableSwagger2
@SpringBootApplication
@EnableAutoConfiguration(exclude = {BatchAutoConfiguration.class, DataSourceAutoConfiguration.class, JpaRepositoriesAutoConfiguration.class})
@EnableCassandraRepositories(basePackages = "com.alliance.dicenotification.repository")
public class DiceNotificationApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(DiceNotificationApplication.class, args);
        SystemParam.getInstance();
        CassandraConnection.getInstance();
        // ActiveMQPoolsUtil.init();
		log.info("---NOTIFICATION SERVICE STARTED---");
	}

    @Bean
    Docket productApi() {
        return new Docket(DocumentationType.SWAGGER_2).select()
                .apis(RequestHandlerSelectors.basePackage("com.alliance")).build();
    }

}
