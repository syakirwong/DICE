//package com.alliance.diceanalytics.configurer;
//
//import org.quartz.JobExecutionContext;
//import org.quartz.JobExecutionException;
//import org.springframework.scheduling.quartz.QuartzJobBean;
//import org.springframework.stereotype.Component;
//
//import com.alliance.diceanalytics.service.AnalyticsService;
//import com.alliance.diceanalytics.utility.SystemParam;
//import com.datastax.oss.driver.api.core.CqlSession;
//import com.datastax.oss.driver.api.core.cql.ResultSet;
//import com.datastax.oss.driver.api.core.cql.Row;
//
//import lombok.extern.slf4j.Slf4j;
//
//
//
//@Slf4j
//@Component
//public class CustomQuartzJob extends QuartzJobBean {
//    public static final String QUARTZ_DAILY_TEST_REPORT = "DailyTestReportJob";
//
//    private AnalyticsService analyticsService;
//
//    private CqlSession cqlSession;
//
//    public CustomQuartzJob(AnalyticsService analyticsService, CqlSession cqlSession) {
//        this.analyticsService = analyticsService;
//        this.cqlSession = cqlSession;
//    }
//
//    @Override
//    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
//        try {
//            String id = jobExecutionContext.getJobDetail().getKey().getName();
//            log.info("{} Running", id);
//
//            String cronExpression = getCronExpressionFromCassandra(id);
//            if (cronExpression != null) {
//                switch (id) {
//                    case QUARTZ_DAILY_TEST_REPORT:
//                    analyticsService.generateReport("daily",1);;
//                        break;
//                    default:
//                        break;
//                }
//
//                log.info("Completed job on service-app " + id);
//            } else {
//                log.warn("No cron expression found for job {}", id);
//            }
//        } catch (Exception e) {
//            throw new JobExecutionException(e);
//        }
//    }
//
//    private String getCronExpressionFromCassandra(String jobId) {
//        String selectQuery = String.format("SELECT cron_expression FROM %s.EVT_QRTZ_CRON_TRIGGERS WHERE trigger_name = ? AND trigger_group = ? ALLOW FILTERING", SystemParam.getInstance().getKeyspace());
//        ResultSet resultSet = cqlSession.execute(selectQuery, jobId, CustomQuartzJob.class.getSimpleName());
//        Row row = resultSet.one();
//        if (row != null) {
//            return row.getString("cron_expression");
//        }
//        return null;
//    }
//}
