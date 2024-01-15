package com.alliance.diceetl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@EnableScheduling
public class DiceEtlApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiceEtlApplication.class, args);
        log.info("--- DICE ETL SERVICE STARTED ---");
    }

}
