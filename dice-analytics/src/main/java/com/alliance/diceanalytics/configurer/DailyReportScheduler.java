package com.alliance.diceanalytics.configurer;

import com.alliance.diceanalytics.model.FileForUpload;
import com.alliance.diceanalytics.request.SendCommonEmailRequest;
import com.alliance.diceanalytics.service.AnalyticsService;
import com.alliance.diceanalytics.service.MessagingService;
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
import java.util.stream.Collectors;

@Data
@Slf4j
@Component
public class DailyReportScheduler implements SchedulingConfigurer {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private RescheduleService rescheduleService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private FreemarkerUtil freemarkerUtil;

    @Autowired
    private ExportUtil exportUtil;

    @Value("${cron.daily.test.report}")
    private String cron;
    @Value("${freemarker.email.template.name.daily.referral}")
    private String EMAIL_DAILY_REFERRAL_TEMPLATE_NAME;
    @Value("#{'${spring.mail.daily.referral.report.to}'.split(',')}")
    private List<String> EMAIL_DAILY_REFERRAL_TO;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {

        taskRegistrar.addTriggerTask(() -> {
            List<SendCommonEmailRequest> emailRequests = new ArrayList<>();
            List<FileForUpload> fileList = new ArrayList<>();

            try {
                 FileForUpload currentFile = analyticsService.generateReport("daily",1,null,null);
                 if (!currentFile.getFileName().isEmpty())
                     fileList.add(currentFile);

            } catch (Exception e) {
                log.error("FAILED TO GENERATE DAILY REPORT TYPE 1");
                e.printStackTrace();
                rescheduleService.rescheduleTask(
                        rescheduleService.getReportRunnable("daily",1)
                );
            }


            try {
                FileForUpload currentFile =analyticsService.generateReport("daily",7,null,null);
                fileList.add(currentFile);
            } catch (Exception e) {
                log.error("FAILED TO GENERATE DAILY REPORT TYPE 7");
                e.printStackTrace();
                rescheduleService.rescheduleTask(
                        rescheduleService.getReportRunnable("daily",7)
                );
            }


            log.info("FILE(S) IN LIST: " + fileList.size());

            if (fileList.size() > 0){
                for(FileForUpload filedetail : fileList)
                        emailRequests.add(createEmailRequest(filedetail.getFileData(), filedetail.getFileName()));

                messagingService.sendEmail(emailRequests);



                fileList = fileList
                        .stream()
                        .filter(file-> file.getSentMft())
                        .collect(Collectors.toList());

                log.info("MFT FILE(S) IN LIST: " + fileList.size());
                if ( fileList.size() > 0){
                    SFTPUtil util = new SFTPUtil();
                    util.transferFiles(fileList);
                }
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
        mailInfo.put("emailTemplate", EMAIL_DAILY_REFERRAL_TEMPLATE_NAME);
        mailInfo.put("title", messageSource.getMessage("spring.mail.subject.daily.referral", new String[] {currentDate}, Locale.ENGLISH));



        try{
            emailRequest.setMailTo(EMAIL_DAILY_REFERRAL_TO.toArray(new String[0]));
            emailRequest.setMailFrom(null);
            emailRequest.setAttachmentBytes(attachment);
            emailRequest.setMailContent(freemarkerUtil.getEmailContentFromTemplate(mailInfo).replace("{reportName}",reportName));
            emailRequest.setMailSubject("Daily " + reportName + " Report - "  +currentDate);
            emailRequest.setAttachmentFileName(filename);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            e.printStackTrace();
        }


        return emailRequest;
    }
}