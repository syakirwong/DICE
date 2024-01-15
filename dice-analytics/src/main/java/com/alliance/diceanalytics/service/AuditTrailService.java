package com.alliance.diceanalytics.service;


import com.alliance.diceanalytics.model.AuditTrail;
import com.alliance.diceanalytics.model.DataField;
import com.alliance.diceanalytics.repository.AuditTrailRepository;
import com.alliance.diceanalytics.request.SendCommonEmailRequest;
import com.alliance.diceanalytics.utility.FreemarkerUtil;
import com.alliance.diceanalytics.utility.SystemParam;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;

@Service
@Slf4j
public class AuditTrailService {

    @Autowired
    private AuditTrailRepository auditTrailRepository;

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private FreemarkerUtil freemarkerUtil;

    @Value("${freemarker.email.template.name.failure.handle.alert}")
    private String EMAIL_FAILURE_HANDLE_ALERT_TEMPLATE_NAME;

    @Value("#{'${spring.mail.failure.handle.alert.to}'.split(',')}")
    private List<String> EMAIL_FAILURE_HANDLE_ALERT_TO;


    public void saveAuditTrail(AuditTrail auditTrail) throws TemplateException, IOException {

        ZoneId zoneId = ZoneId.of("Asia/Kuala_Lumpur");
        ZonedDateTime createDate = ZonedDateTime.now(zoneId);

        OffsetDateTime date = OffsetDateTime.now(ZoneOffset.of("+08:00"));

        auditTrail.setCreatedOn(Date.from(createDate.plusHours(8).toInstant()));

        if (auditTrail.getRequestStatus().equals(DataField.RequestStatus.SERVICE_EXCEPTION) || auditTrail.getRequestStatus().equals(DataField.RequestStatus.EXCEPTION))
            log.info("Audit Trail for {} : {} ",  auditTrail.getRequestStatus(), new ObjectMapper().writeValueAsString(auditTrail));

        auditTrailRepository.save(auditTrail);

        if ( auditTrail.getRequestStatus().equals(DataField.RequestStatus.SERVICE_EXCEPTION) || auditTrail.getRequestStatus().equals(DataField.RequestStatus.SUCCESS) )
            return;


        LocalDate dateNow = LocalDate.now();
        Date endDateTime = Date.from(LocalDateTime.now().plusHours(8).atZone(zoneId).toInstant());
        Date startDateTime = Date.from(LocalDateTime.now().plusHours(8).minusHours(2).atZone(zoneId).toInstant());

        Date startDateTime1Hour = Date.from(LocalDateTime.now().plusHours(8).minusHours(1).atZone(zoneId).toInstant());
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(endDateTime);


        long errorCountAtDuration = auditTrailRepository.countErrorByEventAndDuration(
                auditTrail.getEvent(),
                startDateTime,
                endDateTime
        );

        long mailIsSentCurrentHour = auditTrailRepository.countEmailSentByEventAndDuration(
                auditTrail.getEvent(),
                startDateTime1Hour,
                endDateTime
        );

        long todayMailSent = auditTrailRepository.countEmailSentByEventAndDuration(
                auditTrail.getEvent(),
                Date.from(dateNow.atStartOfDay(zoneId).toInstant()),
                Date.from(dateNow.atTime(LocalTime.MAX).atZone(zoneId).toInstant())
        );

        if (todayMailSent > 10){
            log.info("Maximum 10 email alerts have been sent for event {} today.", auditTrail.getEvent());
            return;
        }

        if(errorCountAtDuration >=5 ) {
            if (mailIsSentCurrentHour == 0) {
                log.info("SENDING ALERT MAIL");
                Map<String, Object> mailInfo = new HashMap<String, Object>();
                mailInfo.put("emailTemplate", EMAIL_FAILURE_HANDLE_ALERT_TEMPLATE_NAME);
                mailInfo.put("title", messageSource.getMessage("spring.mail.subject.failure.handle.alert", new String[]{formattedDate}, Locale.ENGLISH));
                mailInfo.put("statusCode", auditTrail.getStatusCode());
                mailInfo.put("endPointURL", auditTrail.getEndPointUrl());
                mailInfo.put("requestStatus", auditTrail.getRequestStatus());
                mailInfo.put("message", auditTrail.getMessage());

                messagingService.sendEmail(Arrays.asList(createAlertEmailRequest(formattedDate, mailInfo)));

                auditTrailRepository.updateEmailStatusByUUID(auditTrail.getAuditTrailLogId());
            }
            else {
                log.info("sendAlertEmailFailureHandle - reach 5 send alert mail within 2 hours for event : {}", auditTrail.getEvent());
            }
        }
        else{
            log.info("Email is Not Sent for Error at {}", auditTrail.getEvent());
        }


    }

    private SendCommonEmailRequest createAlertEmailRequest(String currentDate,Map<String, Object> mailInfo) throws TemplateException, IOException {
        SendCommonEmailRequest request = null;
        String body = freemarkerUtil.getEmailContentFromTemplate(mailInfo);

            request = new SendCommonEmailRequest(
                    messageSource.getMessage("spring.mail.subject.failure.handle.alert",
                    new String[] {currentDate}, Locale.ENGLISH),
                    null,
                    SystemParam.getInstance().getMailFailureHandleAlertTo().toArray(new String[0]),
                    body,
                    null
            );



        log.info("sendAlertEmailFailureHandle - send alert mail to {}", SystemParam.getInstance().getMailFailureHandleAlertTo());
        return request;
    }

}
