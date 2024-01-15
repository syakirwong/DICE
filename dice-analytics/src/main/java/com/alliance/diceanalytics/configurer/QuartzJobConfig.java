//package com.alliance.diceanalytics.configurer;
//
//import javax.annotation.PostConstruct;
//
//import org.quartz.CronScheduleBuilder;
//import org.quartz.DisallowConcurrentExecution;
//import org.quartz.JobBuilder;
//import org.quartz.JobDetail;
//import org.quartz.Trigger;
//import org.quartz.TriggerBuilder;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import com.datastax.oss.driver.api.core.CqlSession;
//
//@Configuration
//@DisallowConcurrentExecution
//public class QuartzJobConfig {
//    @Value("${cron.daily.test.report}")
//    private String CRON_DAILY_TEST_REPORT;
//
//    @Bean
//    public JobDetail dailyTestReportJobDetail() {
//        return JobBuilder
//                .newJob(CustomQuartzJob.class).withIdentity(CustomQuartzJob.QUARTZ_DAILY_TEST_REPORT)
//                .storeDurably().build();
//    }
//
//
//    @Bean
//    public Trigger dailyTestReportTrigger() {
//        return TriggerBuilder.newTrigger().forJob(dailyTestReportJobDetail())
//                .withIdentity("MonthlyMerchantStatementTrigger")
//                .withSchedule(CronScheduleBuilder.cronSchedule(CRON_DAILY_TEST_REPORT))
//                .build();
//    }
//
//    // @Bean
//    // public CqlSession cqlSession() {
//    //     return CqlSession.builder().build();
//    // }
//
//    // @PostConstruct
//    // public void init() {
//    //     CqlSession cqlSession = cqlSession();
//    //     CassandraKeyspaceInitializer.createKeyspaceAndTable(cqlSession);
//    //     cqlSession.close();
//    // }
//}