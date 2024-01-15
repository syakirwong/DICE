package com.alliance.dicerecommendation.utility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.alliance.dicerecommendation.service.TriggerEngagementService;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableScheduling
@Slf4j
public class DynamicSchedulingConfig {
    @Autowired
    TriggerEngagementService triggerEngagementService;

    private final ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();

    public DynamicSchedulingConfig() {
        taskExecutor.setCorePoolSize(1); // Set the number of threads to 1
        taskExecutor.setMaxPoolSize(1);
        taskExecutor.setThreadNamePrefix("TriggerEngagementTaskExecutor-");
        taskExecutor.initialize();
    }

    @Scheduled(fixedDelay = 1000) // 1 seconds fixed delay
    public void triggerEngagementTask() {
        try {
            taskExecutor.execute(() -> {
                try {
                    triggerEngagementService.triggerEngagement();
                } catch (Exception ex) {
                    log.error("Error when running task: {}", ex);
                }
            });
        } catch (Exception ex) {
            log.error("Error when scheduling task: {}", ex);
        }
    }
}
