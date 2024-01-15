package com.alliance.diceanalytics.configurer;

import com.alliance.diceanalytics.exception.ServiceException;
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
import java.util.stream.Collectors;

@Data
@Slf4j
@Component
public class MonthlyReportScheduler implements SchedulingConfigurer {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private ReportingService reportingService;

    @Autowired
    private RescheduleService rescheduleService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private FreemarkerUtil freemarkerUtil;

    @Value("${cron.monthly.test.report}")
    private String cron;

    @Value("${freemarker.email.template.name.monthly.referral}")
    private String EMAIL_MONTHLY_REFERRAL_TEMPLATE_NAME;

    @Value("#{'${spring.mail.monthly.report.to}'.split(',')}")
    private List<String> EMAIL_MONTHLY_TO;

    @Autowired
    MessagingService messagingService;

    @Autowired
    ExportUtil exportUtil;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(() -> {
            List<SendCommonEmailRequest> emailRequests = new ArrayList<>();
            List<FileForUpload> fileList = new ArrayList<>();


            try {
                FileForUpload currentFile = analyticsService.generateReport("monthly",1,null,null);
                if (!currentFile.getFileName().isEmpty())
                    fileList.add(currentFile);

            } catch (Exception e) {
                log.error("FAILED TO GENERATE MONTHLY REPORT TYPE 1");
                e.printStackTrace();
                rescheduleService.rescheduleTask(
                        rescheduleService.getReportRunnable("monthly",1)
                );
            }


            try {
                fileList.add(analyticsService.generateReport("monthly",2,null,null));
            } catch (Exception e) {
                log.error("FAILED TO GENERATE MONTHLY REPORT TYPE 2");
                e.printStackTrace();
                rescheduleService.rescheduleTask(
                        rescheduleService.getReportRunnable("monthly",2)
                );
            }

            try {
                fileList.add(analyticsService.generateReport("monthly",3,null,null));
            }catch (Exception e) {
                log.error("FAILED TO GENERATE MONTHLY REPORT TYPE 3");
                e.printStackTrace();
                rescheduleService.rescheduleTask(
                        rescheduleService.getReportRunnable("monthly",3)
                );
            }

            //Whatsapp Chatbot Report
            try {
                FileForUpload currentFile = analyticsService.generateReport("monthly",0,null,null);
                currentFile.setSentMft(false);
                if (!currentFile.getFileName().isEmpty())
                    fileList.add(currentFile);
            }catch (Exception e) {
                log.error("FAILED TO GENERATE MONTHLY WHATSAPP CHATBOT REFERRAL REPORT");
                e.printStackTrace();
                if (!(e instanceof ServiceException))
                rescheduleService.rescheduleTask(
                        rescheduleService.getReportRunnable("monthly",0)
                );
            }



            if (fileList.size() > 0){
                for(FileForUpload filedetail : fileList)
                    emailRequests.add(createEmailRequest(filedetail.getFileData(), filedetail.getFileName()));

                log.info("MONTHLY EMAIL FILE(S) IN LIST: " + fileList.size());
                messagingService.sendEmail(emailRequests);

                fileList = fileList
                        .stream()
                        .filter(file-> file.getSentMft())
                        .collect(Collectors.toList());


                log.info("MONTHLY MFT FILE(S) IN LIST: " + fileList.size());
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
        mailInfo.put("emailTemplate", EMAIL_MONTHLY_REFERRAL_TEMPLATE_NAME);
        mailInfo.put("title","Monthly " + reportName + " Report - "  +currentDate );

        try{
            emailRequest.setMailTo(EMAIL_MONTHLY_TO.toArray(new String[0]));
            emailRequest.setMailFrom(null);
            emailRequest.setAttachmentBytes(attachment);
            emailRequest.setMailContent(freemarkerUtil.getEmailContentFromTemplate(mailInfo).replace("{reportName}",reportName));
            emailRequest.setMailSubject("Monthly " + reportName + " Report - "  +currentDate);
            emailRequest.setAttachmentFileName(filename);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            e.printStackTrace();
        }


        return emailRequest;
    }


}
