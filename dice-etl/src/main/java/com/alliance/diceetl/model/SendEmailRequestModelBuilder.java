package com.alliance.diceetl.model;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class SendEmailRequestModelBuilder {
    private String eventType;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String exceptionMessage;
    private String failureReasonMessage;
    private DataCountModel dataCountModel;

    private final String ETL_BATCH_JOB_EMAIL_TEMPLATE = System.getenv("email.template");
    private final String ETL_BATCH_JOB_SEND_EMAIL_TO = System.getenv("spring.mail.failure.handle.alert.to");
    private final String ETL_BATCH_JOB_EMAIL_SUBJECT = System.getenv("spring.mail.subject.failure.handle");

    // Setter methods
    public SendEmailRequestModelBuilder setEventType(String eventType) {
        this.eventType = eventType;
        return this;
    }

    public SendEmailRequestModelBuilder setStartDateTime(LocalDateTime startDateTime) {
        if(startDateTime == null){
            LocalDate today = LocalDate.now();
            this.startDateTime = LocalDateTime.of(today, LocalTime.MIDNIGHT);
        } else {
            this.startDateTime = startDateTime;
        }
        return this;
    }

    public SendEmailRequestModelBuilder setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
        return this;
    }

    public SendEmailRequestModelBuilder setDataCountModel(DataCountModel dataCountModel) {
        this.dataCountModel = dataCountModel;
        return this;
    }

    public SendEmailRequestModelBuilder setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
        return this;
    }

    public SendEmailRequestModelBuilder setFailureReasonMessage(String failureReasonMessage) {
        this.failureReasonMessage = failureReasonMessage;
        return this;
    }

    private String loadEmailTemplate(String filename) throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/" + filename);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    private String formatDuration(LocalDateTime start, LocalDateTime end) {
        Duration duration = Duration.between(start, end);

        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        long milliseconds = duration.toMillisPart();

        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append(" hour").append(hours != 1 ? "s" : "").append(", ");
        }
        if (minutes > 0 || hours > 0) {
            sb.append(minutes).append(" minute").append(minutes != 1 ? "s" : "").append(", ");
        }
        if (seconds > 0 || minutes > 0 || hours > 0) {
            sb.append(seconds).append(" second").append(seconds != 1 ? "s" : "").append(", ");
        }
        sb.append(milliseconds).append(" millisecond").append(milliseconds != 1 ? "s" : "");

        return sb.toString();
    }

    public SendEmailRequestModel build() {
        SendEmailRequestModel emailRequest = new SendEmailRequestModel();
        try {
            emailRequest.setMailTo( new String[]{ETL_BATCH_JOB_SEND_EMAIL_TO});
            emailRequest.setMailFrom(null);

            String htmlContent = loadEmailTemplate(ETL_BATCH_JOB_EMAIL_TEMPLATE);
            htmlContent = htmlContent.replace("${title}", ETL_BATCH_JOB_EMAIL_SUBJECT);
            htmlContent = htmlContent.replace("${eventType}",eventType);
            htmlContent = htmlContent.replace("${processStartTime}",startDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            htmlContent = htmlContent.replace("${processEndTime}",endDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:HH:ss")));
            htmlContent = htmlContent.replace("${duration}",formatDuration(startDateTime,endDateTime));
            htmlContent = htmlContent.replace("${exception}",this.exceptionMessage);
            htmlContent = htmlContent.replace("${failureReason}",this.failureReasonMessage);

            String dataProcessed;
            if (dataCountModel != null) {
                dataProcessed = "<span>" + dataCountModel.getReadCount() + "</span> Read, " +
                        "<span>" + dataCountModel.getSkipCount() + "</span> Skipped, " +
                        "<span>" + dataCountModel.getWriteCount() + "</span> Written";
            } else {
                dataProcessed = "N/A";
            }

            htmlContent = htmlContent.replace("${dataProcessed}", dataProcessed);

            emailRequest.setMailContent(htmlContent);

            emailRequest.setMailSubject(ETL_BATCH_JOB_EMAIL_SUBJECT);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return emailRequest;
    }
}

