package com.alliance.diceetl.controller;

import com.alliance.diceetl.constants.BatchConstants;
import com.alliance.diceetl.model.BatchTrackingModel;
import com.alliance.diceetl.model.DataCountModel;
import com.alliance.diceetl.model.SendEmailRequestModel;
import com.alliance.diceetl.model.SendEmailRequestModelBuilder;
import com.alliance.diceetl.service.ETLBatchJobService;
import com.alliance.diceetl.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;

@RestController
@Slf4j
@RequestMapping("/etl")
public class ETLController {

    @Autowired
    private ETLBatchJobService etlBatchJobService;


    @GetMapping("/check")
    public ResponseEntity<String> check(){
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/execute")
    public ResponseEntity<String> executeEtl(@RequestParam(name = "source") String source){
        switch (source){
            case "onboarding_forms" -> {
                etlBatchJobService.startOnBoardingEtlJob();
            }
            case "ddm_sole_cc" -> {
                etlBatchJobService.startDdmSoleCcEtlJob();
            }
            case "internet_banking_activation" -> {
                etlBatchJobService.startInternetBankingActivationEtlJob();
            }
            case "ploan_activation" -> {
                etlBatchJobService.startPloanActivationEtlJob();
            }
        }
        return ResponseEntity.ok("Done");
    }


    @PostMapping("/email")
    public ResponseEntity<String> email(@RequestParam(name = "dataCount",defaultValue = "present") String dataCount){
        log.info("Triggered Email");
        SendEmailRequestModel request;

        if(dataCount.equalsIgnoreCase("present")) {

            request = new SendEmailRequestModelBuilder()
                    .setStartDateTime(null)
                    .setEndDateTime(LocalDateTime.now())
                    .setEventType(BatchConstants.JobNames.INTERNET_BANKING_ACTIVATION)
                    .setExceptionMessage("N/A")
                    .setFailureReasonMessage("Error Connecting to Database")
                    .setDataCountModel(new DataCountModel(1000L,0L,500L))
                    .build();

//            request = EmailService.createEmailRequest(
//                    BatchConstants.JobNames.ONBOARDING_FORMS, LocalDateTime.now(), LocalDateTime.now(),
//                    "N/A", "Error connecting to database", new DataCountModel(1000L, 0L, 500L));
            EmailService.sendEmail(Arrays.asList(request));
        } else {
            request = new SendEmailRequestModelBuilder()
                    .setStartDateTime(null)
                    .setEndDateTime(LocalDateTime.now())
                    .setEventType(BatchConstants.JobNames.ONBOARDING_FORMS)
                    .setExceptionMessage("N/A")
                    .setFailureReasonMessage("Error connecting to database")
                    .setDataCountModel(null)
                    .build();
//            request = EmailService.createEmailRequest(
//                    BatchConstants.JobNames.ONBOARDING_FORMS, LocalDateTime.now(), LocalDateTime.now(),
//                    "N/A", "Error connecting to database", null);
            EmailService.sendEmail(Arrays.asList(request));
        }
        log.info("Finish Email");
        return ResponseEntity.ok("Done");
    }


}
