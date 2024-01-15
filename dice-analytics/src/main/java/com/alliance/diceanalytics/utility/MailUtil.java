package com.alliance.diceanalytics.utility;

import com.alliance.diceanalytics.request.SendCommonEmailRequest;
import com.alliance.diceanalytics.response.ReportFailureResponse;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
public class MailUtil {

    @Value("${freemarker.email.template.name.daily.referral}")
    private String EMAIL_DAILY_REFERRAL_TEMPLATE_NAME;

    @Value("#{'${spring.mail.daily.referral.report.to}'.split(',')}")
    private List<String> EMAIL_DAILY_REFERRAL_TO;

    @Value("${freemarker.email.template.name.monthly.referral}")
    private String EMAIL_MONTHLY_REFERRAL_TEMPLATE_NAME;

    @Value("#{'${spring.mail.monthly.report.to}'.split(',')}")
    private List<String> EMAIL_MONTHLY_TO;

    @Value("${freemarker.email.template.name.weekly.personal.info}")
    private String EMAIL_WEEKLY_PERSONAL_INFO_TEMPLATE_NAME;

    @Value("#{'${spring.mail.weekly.report.to}'.split(',')}")
    private List<String> EMAIL_WEEKLY_TO;

    @Value("${freemarker.report.email.template.name.failure.handle.alert}")
    private String EMAIL_FAILURE_HANDLE_ALERT_TEMPLATE_NAME;

    @Autowired
    private FreemarkerUtil freemarkerUtil;

    @Autowired
    private MessageSource messageSource;


    public SendCommonEmailRequest createEmailRequest(byte[] attachment, String filename, String reportType){
        log.info("Creating Mail Request");
        SendCommonEmailRequest emailRequest = new SendCommonEmailRequest();
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String reportName = filename.replace(".xlsx","").split("_")[0];

        Map<String, Object> mailInfo = new HashMap<String, Object>();

        if (reportType.equalsIgnoreCase("Daily")){
            mailInfo.put("emailTemplate", EMAIL_DAILY_REFERRAL_TEMPLATE_NAME);
            mailInfo.put("title", messageSource.getMessage("spring.mail.subject.daily.referral", new String[] {currentDate}, Locale.ENGLISH));
            emailRequest.setMailTo(EMAIL_DAILY_REFERRAL_TO.toArray(new String[0]));
            emailRequest.setMailSubject("Daily " + reportName + " Report - "  +currentDate);
        }
        else if (reportType.equalsIgnoreCase("Weekly")){
            mailInfo.put("emailTemplate", EMAIL_WEEKLY_PERSONAL_INFO_TEMPLATE_NAME);
            mailInfo.put("title", "Weekly " + reportName + " Report - "  +currentDate);
            emailRequest.setMailTo(EMAIL_WEEKLY_TO.toArray(new String[0]));
            emailRequest.setMailSubject("Weekly " + reportName + " Report - "  +currentDate);
        }
        else if (reportType.equalsIgnoreCase("Monthly")){
            mailInfo.put("emailTemplate", EMAIL_MONTHLY_REFERRAL_TEMPLATE_NAME);
            mailInfo.put("title", "Monthly " + reportName + " Report - "  +currentDate);
            emailRequest.setMailTo(EMAIL_MONTHLY_TO.toArray(new String[0]));
            emailRequest.setMailSubject("Monthly " + reportName + " Report - "  +currentDate);
        }


        try{
            emailRequest.setMailFrom(null);
            emailRequest.setAttachmentBytes(attachment);
            emailRequest.setMailContent(freemarkerUtil.getEmailContentFromTemplate(mailInfo).replace("{reportName}",reportName));
            emailRequest.setAttachmentFileName(filename);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            e.printStackTrace();
        }

        log.info("Mail Request Create Success");
        return emailRequest;
    }

    public SendCommonEmailRequest createFailReportEmailRequest(ReportFailureResponse response){
        log.info("Creating Failed Report Mail Request");
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());

        Map<String, Object> mailInfo = new HashMap<String, Object>();
        mailInfo.put("emailTemplate", EMAIL_FAILURE_HANDLE_ALERT_TEMPLATE_NAME);
        mailInfo.put("title", messageSource.getMessage("spring.mail.subject.failure.handle.alert", new String[]{formattedDate}, Locale.ENGLISH));
        mailInfo.put("reportName",response.getReportName());
        mailInfo.put("generationDate",formattedDate);
        mailInfo.put("frequency", response.getFrequency());
        mailInfo.put("message", response.getMessage());

        try {
            return new SendCommonEmailRequest(
                    messageSource.getMessage("spring.mail.subject.failure.handle.alert", new String[] {formattedDate}, Locale.ENGLISH),
                    null,
                    SystemParam.getInstance().getMailFailureHandleAlertTo().toArray(new String[0]),
                    freemarkerUtil.getEmailContentFromTemplate(mailInfo),
                    null
            );
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Creating Failed Report Mail Request Unsuccessful");
            return  null;
        }


    }

}
