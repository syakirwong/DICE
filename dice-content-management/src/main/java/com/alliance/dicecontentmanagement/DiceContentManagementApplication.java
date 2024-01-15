package com.alliance.dicecontentmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import lombok.extern.slf4j.Slf4j;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Slf4j
@EnableSwagger2
@SpringBootApplication
public class DiceContentManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiceContentManagementApplication.class, args);
		log.info("---CONTENT MANAGEMENT SERVICE STARTED---");
	}

    @Bean
    Docket productApi() {
        return new Docket(DocumentationType.SWAGGER_2).select()
                .apis(RequestHandlerSelectors.basePackage("com.alliance")).build();
    }

}
