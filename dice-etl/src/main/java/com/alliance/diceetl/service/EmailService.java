package com.alliance.diceetl.service;

import com.alliance.diceetl.model.DataCountModel;
import com.alliance.diceetl.model.SendEmailRequestModel;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class EmailService {

//    @Value("${notification.service.sendEmail.endpointUrl}")
    private static String NOTIFICATION_SERVICE_SEND_EMAIL_ENDPOINT_URL = System.getenv("notification.service.sendEmail.endpointUrl");

//    @Value("${freemarker.email.template.name.failure.handle.alert}")
    private static String ETL_BATCH_JOB_EMAIL_TEMPLATE = System.getenv("email.template");

//    @Value("#{'${spring.mail.failure.handle.alert.to}'.split(',')}")
//    @Value("${spring.mail.failure.handle.alert.to}")
    private static String ETL_BATCH_JOB_SEND_EMAIL_TO = System.getenv("spring.mail.failure.handle.alert.to");


    private static String ETL_BATCH_JOB_EMAIL_SUBJECT = System.getenv("spring.mail.subject.failure.handle");


    public static void sendEmail(List<SendEmailRequestModel> emailRequests) {
        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(15000);
            factory.setReadTimeout(15000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<String, Object>();
            body.put("sendEmailRequest", emailRequests);

            RestTemplate restTemplate = new RestTemplate(factory);
            ResponseEntity<Map> response = restTemplate.postForEntity(NOTIFICATION_SERVICE_SEND_EMAIL_ENDPOINT_URL,
                    emailRequests, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("sendEmail - success: {}", response.getStatusCode());
            } else {
                log.info("sendEmail - failed: {}, body: {}", response.getStatusCode(), response.getBody());
            }
        } catch (Exception ex) {
            log.error("sendEmail - Exception: {}", ex.toString());
        }
    }

//    private static String loadEmailTemplate(String filename) throws IOException {
//        ClassPathResource resource = new ClassPathResource("templates/" + filename);
//        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
//    }

//    public static SendEmailRequestModel createEmailRequest(String eventType, LocalDateTime startDateTime,
//                                                    LocalDateTime endDateTime, String exception, String failureReason, DataCountModel dataCountModel) {
//        SendEmailRequestModel emailRequest = new SendEmailRequestModel();
//
//        try {
//            emailRequest.setMailTo( new String[]{ETL_BATCH_JOB_SEND_EMAIL_TO});
//            emailRequest.setMailFrom(null);
//
//            String htmlContent = loadEmailTemplate(ETL_BATCH_JOB_EMAIL_TEMPLATE);
//            htmlContent = htmlContent.replace("${title}", ETL_BATCH_JOB_EMAIL_SUBJECT);
//            htmlContent = htmlContent.replace("${eventType}",eventType);
//            htmlContent = htmlContent.replace("${processStartTime}",startDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")));
//            htmlContent = htmlContent.replace("${processEndTime}",endDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")));
//            htmlContent = htmlContent.replace("${duration}",String.valueOf(java.time.Duration.between(startDateTime, endDateTime).toMillis()));
//            htmlContent = htmlContent.replace("${exception}",exception);
//            htmlContent = htmlContent.replace("${failureReason}",failureReason);
//
//            String dataProcessed;
//            if (dataCountModel != null) {
//                dataProcessed = "<span>" + dataCountModel.getReadCount() + "</span> Read, " +
//                        "<span>" + dataCountModel.getSkipCount() + "</span> Skipped, " +
//                        "<span>" + dataCountModel.getWriteCount() + "</span> Written";
//            } else {
//                dataProcessed = "N/A";
//            }
//
//            htmlContent = htmlContent.replace("${dataProcessed}", dataProcessed);
//
//            emailRequest.setMailContent(htmlContent);
//
//            emailRequest.setMailSubject(ETL_BATCH_JOB_EMAIL_SUBJECT);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return emailRequest;
//    }


}
