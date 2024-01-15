package com.alliance.diceetl.config.springbatchlistener;

import com.alliance.diceetl.constants.BatchConstants;
import com.alliance.diceetl.model.BatchTrackingModel;
import com.alliance.diceetl.model.DataCountModel;
import com.alliance.diceetl.model.SendEmailRequestModelBuilder;
import com.alliance.diceetl.service.EmailService;
import com.alliance.diceetl.service.SqlLiteCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;


@Slf4j
public class JobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        BatchTrackingModel.getInstance().resetCountsByJobName(jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        SqlLiteCacheService sqlLiteCacheService = new SqlLiteCacheService();

        String jobName = jobExecution.getJobInstance().getJobName();
        BatchTrackingModel batchTrackingModel = BatchTrackingModel.getInstance();
        String sqliteTrigger = BatchConstants.getSqliteTriggerMap(jobName);

        AtomicLong writeCount = batchTrackingModel.getDataCountByBatchJobAndOperation(jobName, BatchConstants.OperationType.WRITE);
        AtomicLong readCount = batchTrackingModel.getDataCountByBatchJobAndOperation(jobName, BatchConstants.OperationType.READ);
        AtomicLong skipCount = batchTrackingModel.getDataCountByBatchJobAndOperation(jobName, BatchConstants.OperationType.SKIP);

        if(jobExecution.getStatus().equals(BatchStatus.COMPLETED)){
            log.info("After Job Listener: [Batch Job Done for {}] Read Count: {}, Skip Count: {}, Write Count: {}",
                    jobExecution.getJobInstance().getJobName(), readCount.toString(), skipCount.toString(), writeCount.toString());

            if (writeCount.get() == 0 || readCount.get() != writeCount.get()) {
                EmailService.sendEmail(
                        Arrays.asList(new SendEmailRequestModelBuilder()
                                .setStartDateTime(jobExecution.getStartTime())
                                .setEndDateTime(jobExecution.getEndTime())
                                .setFailureReasonMessage("Batch Failed After Job Listener")
                                .setExceptionMessage("Write Count is 0 OR Read Count does not match Write Count")
                                .setEventType(jobExecution.getJobInstance().getJobName())
                                .setDataCountModel(new DataCountModel(readCount, skipCount, writeCount))
                                .build()
                        ));
            }

            log.info("After Job Listener: Calling Sqlite Service to start Second ETL for {}", jobExecution.getJobInstance().getJobName());
            sqlLiteCacheService.startEvent(sqliteTrigger);
            log.info("After Job Listener: Done processing for Sqlite Service: {}", jobExecution.getJobInstance().getJobName());
        } else {
            String failureReason = "[After Job Listener] Batch Job " + jobExecution.getStatus();
            EmailService.sendEmail(
                    Arrays.asList(new SendEmailRequestModelBuilder()
                            .setStartDateTime(jobExecution.getStartTime())
                            .setEndDateTime(jobExecution.getEndTime())
                            .setFailureReasonMessage(failureReason)
                            .setExceptionMessage(jobExecution.getAllFailureExceptions().toString())
                            .setEventType(jobExecution.getJobInstance().getJobName())
                            .setDataCountModel(new DataCountModel(readCount, skipCount, writeCount))
                            .build()
                    ));
            log.error("After Job Listener: [{} has FAILED]", jobExecution.getJobInstance().getJobName());
        }
    }
}
