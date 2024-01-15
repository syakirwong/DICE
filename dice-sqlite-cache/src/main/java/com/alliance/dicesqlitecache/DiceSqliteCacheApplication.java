package com.alliance.dicesqlitecache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import lombok.extern.slf4j.Slf4j;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Slf4j
@EnableCaching
@SpringBootApplication
@ComponentScan(basePackages = "com.alliance.dicesqlitecache")
@EntityScan(basePackages = "com.alliance.dicesqlitecache.model")
public class DiceSqliteCacheApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiceSqliteCacheApplication.class, args);
		log.info("--- SQLITE CACHE SERVICE STARTED ---");


	}

	@Bean
	public Docket productApi() {
		return new Docket(DocumentationType.SWAGGER_2).select()
				.apis(RequestHandlerSelectors.basePackage("com.alliance")).build();
	}

}
