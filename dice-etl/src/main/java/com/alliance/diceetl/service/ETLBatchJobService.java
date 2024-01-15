package com.alliance.diceetl.service;

import com.alliance.diceetl.constants.BatchConstants;
import com.alliance.diceetl.model.SendEmailRequestModel;
import com.alliance.diceetl.model.SendEmailRequestModelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;

@Service
@Slf4j
public class ETLBatchJobService {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("internetBankingActivationJob")
    private Job internetBankActivationJob;

    @Autowired
    @Qualifier("onBoardingFormsJob")
    private Job onBoardingFormsJob;

    @Autowired
    @Qualifier("ploanApplicationJob")
    private Job ploanApplicationJob;

    @Autowired
    @Qualifier("ddmSoleCCJob")
    private Job ddmSoleCCJob;


    @Scheduled(cron = "0 0 0 * * ?") // runs at 12am
    public void startInternetBankingActivationEtlJob() {
        log.info("--- startInternetBankingActivationEtlJob ---");
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis()).toJobParameters();
        try {
            jobLauncher.run(internetBankActivationJob, jobParameters);
        } catch (Exception e) {
            EmailService.sendEmail(Arrays.asList(
                    new SendEmailRequestModelBuilder()
                            .setStartDateTime(null)
                            .setEndDateTime(LocalDateTime.now())
                            .setEventType(BatchConstants.JobNames.INTERNET_BANKING_ACTIVATION)
                            .setDataCountModel(null)
                            .setExceptionMessage(e.getMessage())
                            .setFailureReasonMessage("ETL Batch Job Failed at Service Level")
                            .build()
            ));
            e.printStackTrace();
            log.error("{} FAILED: {}", BatchConstants.JobNames.INTERNET_BANKING_ACTIVATION, e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 0 * * ?") // runs at 12.00am
    public void startOnBoardingEtlJob() {
        log.info("--- startOnBoardingEtlJob ---");
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis()).toJobParameters();
        try {
            jobLauncher.run(onBoardingFormsJob, jobParameters);
        } catch (Exception e) {
            EmailService.sendEmail(Arrays.asList(
                    new SendEmailRequestModelBuilder()
                            .setStartDateTime(null)
                            .setEndDateTime(LocalDateTime.now())
                            .setEventType(BatchConstants.JobNames.ONBOARDING_FORMS)
                            .setDataCountModel(null)
                            .setExceptionMessage(e.getMessage())
                            .setFailureReasonMessage("ETL Batch Job Failed at Service Level")
                            .build()
            ));
            e.printStackTrace();
            log.error("{} FAILED: {}", BatchConstants.JobNames.ONBOARDING_FORMS, e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 0 * * ?") // runs at 12.00am
    public void startPloanActivationEtlJob() {
        log.info("--- startPloanActivationEtlJob ---");
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis()).toJobParameters();
        try {
            jobLauncher.run(ploanApplicationJob, jobParameters);
        } catch (Exception e) {
            EmailService.sendEmail(Arrays.asList(
                    new SendEmailRequestModelBuilder()
                            .setStartDateTime(null)
                            .setEndDateTime(LocalDateTime.now())
                            .setEventType(BatchConstants.JobNames.PLOAN_APPLICATION)
                            .setDataCountModel(null)
                            .setExceptionMessage(e.getMessage())
                            .setFailureReasonMessage("ETL Batch Job Failed at Service Level")
                            .build()
            ));
            e.printStackTrace();
            log.error("{} FAILED: {}", BatchConstants.JobNames.PLOAN_APPLICATION, e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 0 * * ?") // runs at 12.00am
    public void startDdmSoleCcEtlJob() {
        log.info("--- startDdmSoleCcEtlJob ---");
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis()).toJobParameters();
        try {
            jobLauncher.run(ddmSoleCCJob, jobParameters);
        } catch (Exception e) {
            EmailService.sendEmail(Arrays.asList(
                    new SendEmailRequestModelBuilder()
                            .setStartDateTime(null)
                            .setEndDateTime(LocalDateTime.now())
                            .setEventType(BatchConstants.JobNames.DDM_SOLE_CC)
                            .setDataCountModel(null)
                            .setExceptionMessage(e.getMessage())
                            .setFailureReasonMessage("ETL Batch Job Failed at Service Level")
                            .build()
            ));
            e.printStackTrace();
            log.error("{} FAILED: {}", BatchConstants.JobNames.DDM_SOLE_CC, e.getMessage());
        }

    }

}
