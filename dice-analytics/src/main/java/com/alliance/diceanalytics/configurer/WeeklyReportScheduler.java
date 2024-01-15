package com.alliance.diceanalytics.configurer;

import com.alliance.diceanalytics.model.FileForUpload;
import com.alliance.diceanalytics.request.SendCommonEmailRequest;
import com.alliance.diceanalytics.service.AnalyticsService;
import com.alliance.diceanalytics.service.MessagingService;
import com.alliance.diceanalytics.service.ReportingService;
import com.alliance.diceanalytics.service.RescheduleService;
import com.alliance.diceanalytics.utility.ExportUtil;
import com.alliance.diceanalytics.utility.FreemarkerUtil;
import com.alliance.diceanalytics.utility.SFTPUtil;
import freemarker.template.TemplateException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Data
@Slf4j
@Component
public class WeeklyReportScheduler implements SchedulingConfigurer {


    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private ReportingService reportingService;

    @Autowired
    private ExportUtil exportUtil;

    @Autowired
    private RescheduleService rescheduleService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private FreemarkerUtil freemarkerUtil;

    @Autowired
    MessagingService messagingService;

    @Value("${freemarker.email.template.name.weekly.personal.info}")
    private String EMAIL_WEEKLY_PERSONAL_INFO_TEMPLATE_NAME;
    @Value("#{'${spring.mail.weekly.report.to}'.split(',')}")
    private List<String> EMAIL_WEEKLY_TO;


    @Value("${cron.weekly.test.report}")
    private String cron;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(() -> {
            List<FileForUpload> fileList = new ArrayList<>();
            List<SendCommonEmailRequest> emailRequests = new ArrayList<>();
            try {
                fileList.add(analyticsService.generateReport("weekly",1,null,null));
            } catch (Exception e) {
                log.error("FAILED TO GENERATE WEEKLY REPORT TYPE 1");
                e.printStackTrace();
                rescheduleService.rescheduleTask(
                        rescheduleService.getReportRunnable("weekly",1)
                );
            }

            try
            {
                    fileList.add(analyticsService.generateReport("weekly",2,null,null));
            } catch (Exception e) {
                log.error("FAILED TO GENERATE WEEKLY REPORT TYPE 2");
                rescheduleService.rescheduleTask(
                        rescheduleService.getReportRunnable("weekly",2)
                );
                e.printStackTrace();
            }

            try{
                fileList.add(analyticsService.generateReport("weekly",3,null,null));
            } catch (Exception e) {
                log.error("FAILED TO GENERATE WEEKLY REPORT TYPE 3");
                e.printStackTrace();
                rescheduleService.rescheduleTask(
                        rescheduleService.getReportRunnable("weekly",3)
                );
            }

            //PERSONAL INFO REPORT
            try{
                fileList.add(analyticsService.generateReport("weekly",6,null,null));
            } catch (Exception e) {
                log.error("FAIL TO GENERATE WEEKLY PERSONAL INFO UPDATE REPORT");
                e.printStackTrace();
                rescheduleService.rescheduleTask(
                        rescheduleService.getReportRunnable("weekly",6)
                );
            }

            if (fileList.size()>0){
                SFTPUtil util = new SFTPUtil();
                for(FileForUpload fileDetail : fileList)
                    emailRequests.add(createEmailRequest(fileDetail.getFileData(),fileDetail.getFileName()));
                util.transferFiles(fileList);
                messagingService.sendEmail(emailRequests);
            }

        }, triggerContext -> {
            CronTrigger cronTrigger = new CronTrigger(cron);
            Date nextExecutionTime = cronTrigger.nextExecutionTime(triggerContext);
            return nextExecutionTime;
        });
    }

    private SendCommonEmailRequest createEmailRequest(byte[] attachment, String filename){
        SendCommonEmailRequest emailRequest = new SendCommonEmailRequest();
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String reportName = filename.replace(".xlsx","").split("_")[0];

        Map<String, Object> mailInfo = new HashMap<String, Object>();
        mailInfo.put("emailTemplate", EMAIL_WEEKLY_PERSONAL_INFO_TEMPLATE_NAME);
        mailInfo.put("title", "Weekly " + reportName + " Report - "  +currentDate);




        try {
            emailRequest.setMailTo(EMAIL_WEEKLY_TO.toArray(new String[0]));
            emailRequest.setMailFrom(null);
            emailRequest.setAttachmentBytes(attachment);
            emailRequest.setMailContent(freemarkerUtil.getEmailContentFromTemplate(mailInfo).replace("{reportName}",reportName));
            emailRequest.setMailSubject("Weekly " + reportName + " Report - "  +currentDate);
            emailRequest.setAttachmentFileName(filename);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            e.printStackTrace();
        }


        return emailRequest;
    }
}
