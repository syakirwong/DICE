package com.alliance.diceanalytics.service;

import com.alliance.diceanalytics.constant.ReportDetails;
import com.alliance.diceanalytics.constant.ReportDetails.ReportDuration;
import com.alliance.diceanalytics.model.FileForUpload;
import com.alliance.diceanalytics.request.BaseReportInfoRequest;
import com.alliance.diceanalytics.request.SendCommonEmailRequest;
import com.alliance.diceanalytics.response.ReportFailureResponse;
import com.alliance.diceanalytics.utility.DateUtil;
import com.alliance.diceanalytics.utility.MailUtil;
import com.alliance.diceanalytics.utility.SFTPUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Service
@Slf4j
public class RescheduleService {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private ReportingService reportingService;

    @Autowired
    private DateUtil dateUtil;

    @Autowired
    private MailUtil mailUtil;

    @Autowired
    private MessagingService messagingService;

    @Value("${report.retry.interval.minutes}")
    private Integer retryInterval;

    @Value("${report.max.retry}")
    private Integer MAX_RETRY;

    private static final Map<String, Integer> reportGenerationCount = new HashMap<>();

    private TaskScheduler scheduler;



    public void rescheduleTask(Runnable runnable) {
        Date scheduleDate =  Date.from(LocalDateTime.now().plusMinutes(retryInterval).atZone(ZoneId.systemDefault()).toInstant());

        ScheduledExecutorService localExecutor = Executors.newSingleThreadScheduledExecutor();
        scheduler = new ConcurrentTaskScheduler(localExecutor);
        scheduler.schedule(runnable, scheduleDate);
        log.info("Report has been rescheduled at " + scheduleDate);
    }

    public Runnable getReportRunnable(String duration, Integer reportType){
        return  () -> {
            retryReportProcess(duration,reportType);
        };

    }

    public void retryReportProcess(String duration, Integer reportType)  {
        BaseReportInfoRequest request = DateUtil.getReportDateRange(duration);

        //CHECK MAX RETRY
        if (reportGenerationCount.containsKey(duration+reportType)==false)
            reportGenerationCount.put(duration+reportType,1);
        else
            reportGenerationCount.put(duration+reportType,reportGenerationCount.get(duration+reportType)+1);

        log.info(duration + " Report Type " + reportType +" is regenerating for " + reportGenerationCount.get(duration+reportType) + " time(s)");
        log.info("__________Retrying__________");


        FileForUpload fileForUpload = null;
        try {

             fileForUpload = analyticsService.generateReport(duration, reportType, request.getStartDate(), request.getEndDate());


            if (fileForUpload!=null){
                SFTPUtil sftpUtil = new SFTPUtil();
                sftpUtil.transferFile(new ByteArrayInputStream(fileForUpload.getFileData()), fileForUpload.getFileName());

                List<SendCommonEmailRequest> emailRequestList = new ArrayList<>();
                emailRequestList.add(
                            mailUtil.createEmailRequest(
                                    fileForUpload.getFileData(),
                                    fileForUpload.getFileName(),
                                    duration)
                 );
                    messagingService.sendEmail(emailRequestList);
                    reportGenerationCount.remove(duration+reportType);
            }
        log.info("________Retrying Success________");
        //RETRY ON EXCEPTION
        } catch (Exception e) {
            log.info("________Retrying Failed________");
            String failureMessage = "";
            if (e.getMessage()!=null)
                failureMessage =e.getMessage();

            List<SendCommonEmailRequest> emailRequestList = new ArrayList<>();
            if (reportGenerationCount.get(duration+reportType)== MAX_RETRY){
                log.error("MAX RETRY HAS BEEN REACHED FOR " + duration + " REPORT TYPE " + reportType);
                reportGenerationCount.remove(duration+reportType);

                emailRequestList.add(
                        mailUtil.createFailReportEmailRequest(new ReportFailureResponse(
                        new ReportDetails().getReportName(ReportDuration.valueOf(duration),reportType),
                        duration,
                        failureMessage
                )));
                messagingService.sendEmail(emailRequestList);
                return;
            }
            else
                rescheduleTask(getReportRunnable(duration,reportType));
            e.printStackTrace();
        }
    }



}
