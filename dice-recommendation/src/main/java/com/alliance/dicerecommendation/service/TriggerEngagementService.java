package com.alliance.dicerecommendation.service;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.poi.ss.usermodel.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.alliance.dicerecommendation.constant.ApiResponse;
import com.alliance.dicerecommendation.constant.DataField.Status;
import com.alliance.dicerecommendation.exception.ServiceException;
import com.alliance.dicerecommendation.model.Recommendation;
import com.alliance.dicerecommendation.model.ReferralCode;
import com.alliance.dicerecommendation.model.DataField.CampaignStatus;
import com.alliance.dicerecommendation.model.DataField.TriggerStatus;
import com.alliance.dicerecommendation.model.Campaign;
import com.alliance.dicerecommendation.model.CustomerProfile;
import com.alliance.dicerecommendation.model.EngagementHistory;
import com.alliance.dicerecommendation.repository.EngagementHistoryRepository;
import com.alliance.dicerecommendation.repository.RecommendationRepository;
import com.alliance.dicerecommendation.repository.ReferralCodeRepository;
import com.alliance.dicerecommendation.request.ProfileRequest;
import com.alliance.dicerecommendation.request.TriggerEngagementPushNotiRequest;
import com.alliance.dicerecommendation.response.CacheCustomerProfileSoleCCResponse;
import com.alliance.dicerecommendation.response.CampaignDetailResponse;
import com.alliance.dicerecommendation.response.CheckCampaignResponse;
import com.alliance.dicerecommendation.response.MessageTemplateDetailResponse;
import com.alliance.dicerecommendation.response.PandaiBroadcastResponse;
import com.alliance.dicerecommendation.utility.StringUtil;
import com.datastax.oss.driver.api.core.uuid.Uuids;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TriggerEngagementService {
    @Autowired
    MessageSource messageSource;

    @Autowired
    RecommendationRepository recommendationRepository;

    @Autowired
    EngagementHistoryRepository engagementHistoryRepository;

    @Autowired
    MessagingService messagingService;

    @Autowired
    RecommendationService recommendationService;

    @Autowired
    private ReferralCodeRepository referralCodeRepository;

    @Value("${getMsgTmpDetail.endpointURL}")
    private String GET_MSG_TMP_DETAIL_END_POINT_URL;

    @Value("${checkCampaign.endpointURL}")
    private String CHECK_CAMPAIGN_END_POINT_URL;

    @Value("${number.of.thread.executeInParallel}")
    private Integer NUMBER_OF_THREAD_EXECUTE_IN_PARALLEL;

    @Value("${pandaiBroadcast.messageType}")
    private String PANDAI_BROADCAST_MESSAGE_TYPE;

    @Value("${pandaiBroadcast.flowName}")
    private String PANDAI_BROADCAST_FLOW_NAME;

    @Value("${pandaiBroadcast.language}")
    private String PANDAI_BROADCAST_LANGUAGE;

    // @Value("${get.customer.profile.endpointUrl}")
    // private String GET_CUSTOMER_PROFILE_URL;

    public void triggerEngagement() throws ServiceException {
        try {
            log.debug("start - triggerEngagement with debug mode on");

            List<Recommendation> recommendationListForTrigger = recommendationRepository.getEngagementTriggerListFilterByCurrentDateTime();

            if (!recommendationListForTrigger.isEmpty()) {
                AtomicInteger totalSuccessTriggerRequest = new AtomicInteger(0);

                log.info("start - triggerEngagement with total of {} trigger request", recommendationListForTrigger.size());

                executeInParallel(recommendationListForTrigger, totalSuccessTriggerRequest);

                log.info("end - triggerEngagement with {} total success request", totalSuccessTriggerRequest.get());
            } else {
                log.debug("end - triggerEngagement no engagement request");
            }

        } catch (Exception ex) {
            log.error("triggerEngagement - Exception: {}", ex);
        }
    }

    public void executeInParallel(List<Recommendation> recommendationList, AtomicInteger totalSuccessTriggerRequest) {
        int numThreads = NUMBER_OF_THREAD_EXECUTE_IN_PARALLEL; // Set the number of threads to 5
        int batchSize = (int) Math.ceil((double) recommendationList.size() / numThreads);

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        // CampaignDetailResponse campaignDetailResponse = recommendationService.getCampaignDetail(trigger.getCampaignID());
        // Map<String, String> campaignProperties = campaignDetailResponse.getCampaignProperties();
        AtomicReference<CampaignDetailResponse> campaignDetailResponse = new AtomicReference<>(new CampaignDetailResponse());
        AtomicReference<Map<String, String>> campaignProperties = new AtomicReference<>(new HashMap<>());
        AtomicInteger tempCampaignId = new AtomicInteger(0);
        try {
            List<Callable<Void>> callables = new ArrayList<>();
            for (int i = 0; i < recommendationList.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, recommendationList.size());
                List<Recommendation> sublist = recommendationList.subList(i, endIndex);
                callables.add(() -> {
                    try {
                        for (Recommendation trigger : sublist) {
                            if(trigger.getCampaignID() != tempCampaignId.get()){
                                log.info("triggerEngagement - get campaign detail for campaign id : {}", trigger.getCampaignID());
                                tempCampaignId.set(trigger.getCampaignID());
                                campaignDetailResponse.set(recommendationService.getCampaignDetail(trigger.getCampaignID()));
                                campaignProperties.set(campaignDetailResponse.get().getCampaignProperties());
                                log.info("triggerEngagement - campaign detail and properties : {}", campaignDetailResponse);
                            }
                            processTrigger(trigger, totalSuccessTriggerRequest,campaignProperties);
                        }
                    } catch (ServiceException e) {
                        log.info("ServiceException in processTrigger(): {}", e);
                    } catch (Exception e) {
                        log.error("Exception in processTrigger(): {}", e);
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    return null;
                });
            }

            // Invoke all tasks concurrently
            List<Future<Void>> futures = executor.invokeAll(callables);

            // Wait for all tasks to finish
            for (Future<Void> future : futures) {
                try {
                    future.get(); // This call ensures any exceptions that occurred within the tasks are propagated
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Exception occurred in one of the tasks: {}", e);
                }
            }
        } catch (InterruptedException e) {
            log.error("InterruptedException in executeInParallel(): {}", e);
        } finally {
            executor.shutdown();
        }
    }


    public void processTrigger(Recommendation trigger, AtomicInteger totalSuccessTriggerRequest, AtomicReference<Map<String, String>> campaignProperties) throws Exception {
        if (trigger.getIsTriggered() != true) {
            trigger.setTriggerStatus(TriggerStatus.PENDING);
            trigger.setUpdatedOn(getCurrentDate());
            recommendationRepository.save(trigger);

            // General campaign validation
            CampaignStatus validationStatus = checkCampaign(trigger.getCampaignID());
            log.info("triggerEngagement - validationStatus for campaign id : {} is {}", trigger.getCampaignID(), validationStatus);

            if(validationStatus == CampaignStatus.VALID){
                            TriggerEngagementPushNotiRequest triggerEngagementPushNotiRequest = new TriggerEngagementPushNotiRequest();
                            Map<String, Integer> msgTemplate = trigger.getMessageTemplateId();

                            log.debug("triggerEngagement - msgTemplate : {}", msgTemplate);
                            if(msgTemplate!=null){
                                if (msgTemplate.containsKey("pushNotificationTemplateId")) {
                                    MessageTemplateDetailResponse messageTemplateDetailResponse = getMessageTemplateDetail(msgTemplate.get("pushNotificationTemplateId"));
                                    
                                    triggerEngagementPushNotiRequest.setTitle(messageTemplateDetailResponse.getTitle());
                                    triggerEngagementPushNotiRequest.setContent(messageTemplateDetailResponse.getContent());
                                }
                                triggerEngagementPushNotiRequest.setDeviceId(trigger.getDeviceId());
                                triggerEngagementPushNotiRequest.setDevicePlatform(trigger.getDevicePlatform());
                                triggerEngagementPushNotiRequest.setNotificationType("ekyc-inappmsg-piu"); 
                                triggerEngagementPushNotiRequest.setContentId(trigger.getCampaignID());

                                // set Message Display Method if any else default
                                if (campaignProperties != null && campaignProperties.get() != null) {
                                    Map<String, String> propertiesMap = campaignProperties.get();
                                    
                                    if (propertiesMap.containsKey("messageDisplayMethod")) {
                                        // Key exists in the map
                                        String messageDisplayMethod = propertiesMap.get("messageDisplayMethod");
                                        triggerEngagementPushNotiRequest.setMessageDisplayMethod(messageDisplayMethod);
                                    } else {
                                        // Key doesn't exist in the map
                                         triggerEngagementPushNotiRequest.setMessageDisplayMethod(null);
                                    }
                                } else {
                                    // Handle the case where campaignProperties or its value is null
                                }

                                triggerEngagementPushNotiRequest.setType("PushNotification");
                            }

                            if ((trigger.getEngagementMode()).contains("PUSH")) {
                                try{
                                    Boolean isPushNotiSuccess = messagingService.pushNotification(triggerEngagementPushNotiRequest);
                                    log.info("triggerEngagement - isPushNotiSuccess : {}",isPushNotiSuccess);
                                    if (isPushNotiSuccess == true){
                                       
                                        createEngagementSentHistory(msgTemplate.get("pushNotificationTemplateId"),trigger.getDeviceId(),"PUSH","EN",triggerEngagementPushNotiRequest.getTitle(),triggerEngagementPushNotiRequest.getContent(),trigger.getCampaignID());

                                        trigger.setIsTriggered(true);
                                        trigger.setTriggerStatus(TriggerStatus.COMPLETED);
                                        trigger.setUpdatedOn(getCurrentDate());
                                        recommendationRepository.save(trigger);
                                        totalSuccessTriggerRequest.incrementAndGet();
                                    }
                                    else{
                                        trigger.setIsTriggered(false);
                                        trigger.setTriggerStatus(TriggerStatus.FAILED);
                                        trigger.setUpdatedOn(getCurrentDate());
                                        recommendationRepository.save(trigger);
                                    }
                                }catch (Exception ex){
                                                trigger.setTriggerStatus(TriggerStatus.FAILED);
                                                trigger.setUpdatedOn(getCurrentDate());
                                                recommendationRepository.save(trigger);
                                                log.error("triggerEngagement - Exception: {}", ex);
                                }
                            }

                            if ((trigger.getEngagementMode()).contains("BELL")) {
                                try{
                                    Boolean isBellNotiSuccess = messagingService.pushBellNotification(triggerEngagementPushNotiRequest,trigger.getCifNo());
                                    log.info("triggerEngagement - isBellNotiSuccess : {}", isBellNotiSuccess);
                                    if (isBellNotiSuccess == true){

                                        createEngagementSentHistory(msgTemplate.get("pushNotificationTemplateId"),trigger.getDeviceId(),"BELL","EN",triggerEngagementPushNotiRequest.getTitle(),triggerEngagementPushNotiRequest.getContent(),trigger.getCampaignID());

                                        trigger.setIsTriggered(true);
                                        trigger.setTriggerStatus(TriggerStatus.COMPLETED);
                                        trigger.setUpdatedOn(getCurrentDate());
                                        recommendationRepository.save(trigger);
                                        totalSuccessTriggerRequest.incrementAndGet();
                                    }
                                    else{
                                        trigger.setIsTriggered(false);
                                        trigger.setTriggerStatus(TriggerStatus.FAILED);
                                        trigger.setUpdatedOn(getCurrentDate());
                                        recommendationRepository.save(trigger);
                                    }
                                }catch (Exception ex){
                                                trigger.setTriggerStatus(TriggerStatus.FAILED);
                                                trigger.setUpdatedOn(getCurrentDate());
                                                recommendationRepository.save(trigger);
                                                log.error("triggerEngagement - Exception: {}", ex);
                                }
                            }

                            if ((trigger.getEngagementMode()).contains("WHATSAPP")) {
                                try{
                                    Boolean isPandaiBroadcastSuccess = false;
                                    CacheCustomerProfileSoleCCResponse userProfile = new CacheCustomerProfileSoleCCResponse();
                                    ProfileRequest profileRequest = new ProfileRequest();
                                    profileRequest.setIdType("cif_no");
                                    profileRequest.setIdValue(trigger.getCifNo());
                                    profileRequest.setTableName("EVT_SOLE_CC_VIEW");
                                    userProfile = recommendationService.checkCustomerProfile(profileRequest);
                                    // userProfile = getCustomerProfileViaCif(trigger.getCifNo());
                                    if(userProfile!=null){
                                        log.info("triggerEngagement - userProfile for cifno : {} : {}",trigger.getCifNo(), userProfile);
                                    }
                                    else {
                                        log.warn("triggerEngagement - userProfile is null for cifno : {}", trigger.getCifNo());
                                    }
                                    String normalizedNumber;
                                    if(!userProfile.getMobile().trim().isEmpty()){
                                        // Remove all non-digit characters, including the "+"
                                        normalizedNumber = (userProfile.getMobile()).replaceAll("[^0-9]", "");
                                    }
                                    else {
                                        log.info("triggerEngagement - mobile number is null for cif no : {}", trigger.getCifNo());
                                        normalizedNumber = null;
                                    }
                                    log.info("triggerEngagement - userProfile : {}", userProfile);

                                    String pandaiBroadcastFlowName = null;
                                    String platformChannel = "save_plus";
                                    String diceActionName = null;
                                    if (campaignProperties != null){
                                        Map<String, String> campaignPropertiesMap = campaignProperties.get();
                                        if (campaignPropertiesMap.containsKey("pandaiBroadcastFlowName")){
                                            pandaiBroadcastFlowName = campaignPropertiesMap.get("pandaiBroadcastFlowName");
                                            log.info("triggerEngagement - pandaiBroadcastFlowName : {}", pandaiBroadcastFlowName);
                                        } else{
                                            log.warn("triggerEngagement - does not contain pandaiBroadcastFlowName for campaign id : {}",trigger.getCampaignID());
                                        }

                                        if (campaignPropertiesMap.containsKey("platformChannel")){
                                            platformChannel = campaignPropertiesMap.get("platformChannel");
                                            log.info("triggerEngagement - platformChannel : {}", platformChannel);
                                        } else{
                                            log.warn("triggerEngagement - does not contain platformChannel for campaign id : {}",trigger.getCampaignID());
                                        }

                                        if (campaignPropertiesMap.containsKey("diceActionName")){
                                            diceActionName = campaignPropertiesMap.get("diceActionName");
                                            log.info("triggerEngagement - diceActionName : {}", platformChannel);
                                        } else{
                                            log.warn("triggerEngagement - does not contain diceActionName for campaign id : {}",trigger.getCampaignID());
                                        }
                                    }

                                    String referralCode = null;

                                    if("REFERRAL".equals(diceActionName)){
                                        Integer generateCodeAttempt = 4;
                                        log.info("triggerEngagement - generate REFERRAL code for campaign id : {}", trigger.getCampaignID());
                                        
                                        for (int i = 0; i < generateCodeAttempt; i++) {
                                            Integer digit = 6;
                                            // add VC prefix to generated code
                                            if (platformChannel != null && platformChannel.equalsIgnoreCase("vcc")){
                                                digit = (i > 3) ? 5 : 4; // Set digit to 5 if generateCodeAttempt > 3, otherwise 4
                                                referralCode = "VC" + StringUtil.generateRandomString(digit, true, true);
                                            }
                                            // normal generated code
                                            else {
                                                 digit = (i > 3) ? 7 : 6; // Set digit to 7 if generateCodeAttempt > 3, otherwise 6
                                                 referralCode = StringUtil.generateRandomString(digit, true, true);
                                            }
                                            
                                            Integer countByCodeValueAndStatus = referralCodeRepository.countByCodeValueAndStatus(referralCode, Status.ACTIVE) + referralCodeRepository.countByCodeValueAndStatus(referralCode, Status.DISABLE);
                                            if (countByCodeValueAndStatus == 0) {
                                                log.info("triggerEngagement - generateCodeAttempt = {} , referralCode = {}", i, referralCode);
                                                break;
                                            } else {
                                                log.info("triggerEngagement - code exist, retry generateCodeAttempt = {}", generateCodeAttempt);
                                                referralCode = null;
                                            }
                                        }

                                        if(referralCode != null){
                                            ReferralCode referralCodeToStore = new ReferralCode();
                                            log.info("triggerEngagement - referralCode is {}", referralCode);
                                            referralCodeToStore.setCampaignId(trigger.getCampaignID());
                                            referralCodeToStore.setCodeValue(referralCode);
                                            referralCodeToStore.setCifNo(trigger.getCifNo());
                                            log.info("triggerEngagement - referralCodeToStore : {}", referralCodeToStore);
                                            referralCodeRepository.save(referralCodeToStore);
                                        }
                                    }

                                    if (userProfile.getMobile() != null && pandaiBroadcastFlowName !=null){
                                        PandaiBroadcastResponse broadcastResponse = new PandaiBroadcastResponse();
                                        broadcastResponse = messagingService.pandaiBroadcast(
                                            userProfile.getCustomerName(),
                                            normalizedNumber, referralCode, null,
                                            PANDAI_BROADCAST_MESSAGE_TYPE, pandaiBroadcastFlowName,
                                            PANDAI_BROADCAST_LANGUAGE, platformChannel);


                                        log.info("triggerEngagement - broadcastResponse -> {}", broadcastResponse);
                                        if (broadcastResponse != null) {
                                            for (int i = 0; i < broadcastResponse.getSuccessful_users().size(); i++) {
                                                if (broadcastResponse.getSuccessful_users().get(i).getUser_id()
                                                        .equals(normalizedNumber)) {
                                                isPandaiBroadcastSuccess = true;
                                                }
                                            }
                                        }
                                        log.info("triggerEngagement - isPandaiBroadcastSuccess : {}", isPandaiBroadcastSuccess);
                                    } else {
                                        log.info("triggerEngagement - unable to get userProfile for cifno {}", trigger.getCifNo());
                                    }
                                   
                                    if (isPandaiBroadcastSuccess == true){

                                        //createEngagementSentHistory(msgTemplate.get("pushNotificationTemplateId"),trigger.getDeviceId(),"BELL","EN",triggerEngagementPushNotiRequest.getTitle(),triggerEngagementPushNotiRequest.getContent(),trigger.getCampaignID());

                                        trigger.setIsTriggered(true);
                                        trigger.setTriggerStatus(TriggerStatus.COMPLETED);
                                        trigger.setUpdatedOn(getCurrentDate());
                                        recommendationRepository.save(trigger);
                                        totalSuccessTriggerRequest.incrementAndGet();
                                    }
                                    else{
                                        trigger.setIsTriggered(false);
                                        trigger.setTriggerStatus(TriggerStatus.FAILED);
                                        trigger.setUpdatedOn(getCurrentDate());
                                        recommendationRepository.save(trigger);
                                    }
                                }catch (Exception ex){
                                                trigger.setTriggerStatus(TriggerStatus.FAILED);
                                                trigger.setUpdatedOn(getCurrentDate());
                                                recommendationRepository.save(trigger);
                                                log.error("triggerEngagement - Exception: {}", ex);
                                }
                            }

                            if(!(trigger.getEngagementMode()).contains("PUSH") && !(trigger.getEngagementMode()).contains("BELL") && !(trigger.getEngagementMode()).contains("WHATSAPP")){
                                log.warn("triggerEngagement - request recommendation id {} with {} engagement mode is unable to trigger", trigger.getRecommendationId(),trigger.getEngagementMode());
                            }
                        } else {
                switch (validationStatus) {
                    case EXPIRED:
                        log.info("triggerEngagement - campaignId {} is EXPIRED", trigger.getCampaignID());
                        trigger.setTriggerStatus(TriggerStatus.EXPIRED);
                        trigger.setUpdatedOn(getCurrentDate());
                        recommendationRepository.save(trigger);
                        break;
                    case DISABLE:
                        log.info("triggerEngagement - campaignId {} is DISABLE", trigger.getCampaignID());
                        trigger.setTriggerStatus(TriggerStatus.DISABLE);
                        trigger.setUpdatedOn(getCurrentDate());
                        recommendationRepository.save(trigger);
                        break;
                    default:
                        log.warn("triggerEngagement - campaignId {} is INVALID", trigger.getCampaignID());
                        trigger.setTriggerStatus(TriggerStatus.FAILED);
                        trigger.setUpdatedOn(getCurrentDate());
                        recommendationRepository.save(trigger);
                        break;
                }
            }
        }
    }


    // public void triggerEngagement() throws ServiceException{
    //     try{
    //         log.debug("start - triggerEngagement with debug mode on");

    //         List<Recommendation> recommendationListForTrigger = recommendationRepository.getEngagementTriggerListFilterByCurrentDateTime();

    //         if(!recommendationListForTrigger.isEmpty()){
    //             Integer totalTriggerRequest = recommendationListForTrigger.size();
    //             log.info("start - triggerEngagement with total of {} trigger request",totalTriggerRequest);
    //             Integer totalSuccessTriggerRequest = 0;
    //             for (Recommendation trigger : recommendationListForTrigger){
    //                 if(trigger.getIsTriggered()!=true){
    //                     trigger.setTriggerStatus(TriggerStatus.PENDING);
    //                     trigger.setUpdatedOn(getCurrentDate());
    //                     recommendationRepository.save(trigger);

    //                     // General campaign validation
    //                      CampaignStatus validationStatus =  checkCampaign(trigger.getCampaignID());
    //                     log.info("triggerEngagement - validationStatus for campaign id : {} is {}",trigger.getCampaignID(),validationStatus);

    //                     if(validationStatus == CampaignStatus.VALID){
    //                         TriggerEngagementPushNotiRequest triggerEngagementPushNotiRequest = new TriggerEngagementPushNotiRequest();
    //                         Map<String, Integer> msgTemplate = trigger.getMessageTemplateId();
                            
    //                         if (msgTemplate.containsKey("pushNotificationTemplateId")) {
    //                             MessageTemplateDetailResponse messageTemplateDetailResponse = getMessageTemplateDetail(msgTemplate.get("pushNotificationTemplateId"));
                                
    //                             triggerEngagementPushNotiRequest.setTitle(messageTemplateDetailResponse.getTitle());
    //                             triggerEngagementPushNotiRequest.setContent(messageTemplateDetailResponse.getContent());
    //                         }
    //                         triggerEngagementPushNotiRequest.setDeviceId(trigger.getDeviceId());
    //                         triggerEngagementPushNotiRequest.setDevicePlatform(trigger.getDevicePlatform());
    //                         triggerEngagementPushNotiRequest.setNotificationType("ekyc-inappmsg-piu"); 
    //                         triggerEngagementPushNotiRequest.setContentId(trigger.getCampaignID());

    //                         triggerEngagementPushNotiRequest.setType("PushNotification");

    //                         if ((trigger.getEngagementMode()).contains("PUSH")) {
    //                             try{
    //                                 Boolean isPushNotiSuccess = messagingService.pushNotification(triggerEngagementPushNotiRequest);
    //                                 log.info("triggerEngagement - isPushNotiSuccess : {}",isPushNotiSuccess);
    //                                 if (isPushNotiSuccess == true){
                                       
    //                                     createEngagementSentHistory(msgTemplate.get("pushNotificationTemplateId"),trigger.getDeviceId(),"PUSH","EN",triggerEngagementPushNotiRequest.getTitle(),triggerEngagementPushNotiRequest.getContent(),trigger.getCampaignID());

    //                                     trigger.setIsTriggered(true);
    //                                     trigger.setTriggerStatus(TriggerStatus.COMPLETED);
    //                                     trigger.setUpdatedOn(getCurrentDate());
    //                                     recommendationRepository.save(trigger);
    //                                     totalSuccessTriggerRequest++;
    //                                 }
    //                                 else{
    //                                     trigger.setIsTriggered(false);
    //                                     trigger.setTriggerStatus(TriggerStatus.FAILED);
    //                                     trigger.setUpdatedOn(getCurrentDate());
    //                                     recommendationRepository.save(trigger);
    //                                 }
    //                             }catch (Exception ex){
    //                                             trigger.setTriggerStatus(TriggerStatus.FAILED);
    //                                             trigger.setUpdatedOn(getCurrentDate());
    //                                             recommendationRepository.save(trigger);
    //                                             log.error("triggerEngagement - Exception: {}", ex);
    //                             }
    //                         }

    //                         if ((trigger.getEngagementMode()).contains("BELL")) {
    //                             try{
    //                                 Boolean isBellNotiSuccess = messagingService.pushBellNotification(triggerEngagementPushNotiRequest,trigger.getCifNo());
    //                                 log.info("triggerEngagement - isBellNotiSuccess : {}", isBellNotiSuccess);
    //                                 if (isBellNotiSuccess == true){

    //                                     createEngagementSentHistory(msgTemplate.get("pushNotificationTemplateId"),trigger.getDeviceId(),"BELL","EN",triggerEngagementPushNotiRequest.getTitle(),triggerEngagementPushNotiRequest.getContent(),trigger.getCampaignID());

    //                                     trigger.setIsTriggered(true);
    //                                     trigger.setTriggerStatus(TriggerStatus.COMPLETED);
    //                                     trigger.setUpdatedOn(getCurrentDate());
    //                                     recommendationRepository.save(trigger);
    //                                     totalSuccessTriggerRequest++;
    //                                 }
    //                                 else{
    //                                     trigger.setIsTriggered(false);
    //                                     trigger.setTriggerStatus(TriggerStatus.FAILED);
    //                                     trigger.setUpdatedOn(getCurrentDate());
    //                                     recommendationRepository.save(trigger);
    //                                 }
    //                             }catch (Exception ex){
    //                                             trigger.setTriggerStatus(TriggerStatus.FAILED);
    //                                             trigger.setUpdatedOn(getCurrentDate());
    //                                             recommendationRepository.save(trigger);
    //                                             log.error("triggerEngagement - Exception: {}", ex);
    //                             }
    //                         }

    //                         if(!(trigger.getEngagementMode()).contains("PUSH") && !(trigger.getEngagementMode()).contains("BELL")){
    //                             log.warn("triggerEngagement - request recommendation id {} with {} engagement mode is unable to trigger", trigger.getRecommendationId(),trigger.getEngagementMode());
    //                         }
    //                     } else {
    //                         switch (validationStatus) {
    //                             case EXPIRED:
    //                                 log.info("triggerEngagement - campaignId {} is EXPIRED", trigger.getCampaignID());
    //                                 trigger.setTriggerStatus(TriggerStatus.EXPIRED);
    //                                 trigger.setUpdatedOn(getCurrentDate());
    //                                 recommendationRepository.save(trigger);
    //                                 break;
    //                             case DISABLE:
    //                                 log.info("triggerEngagement - campaignId {} is DISABLE", trigger.getCampaignID());
    //                                 trigger.setTriggerStatus(TriggerStatus.DISABLE);
    //                                 trigger.setUpdatedOn(getCurrentDate());
    //                                 recommendationRepository.save(trigger);
    //                                 break;
    //                             default:
    //                                 log.warn("triggerEngagement - campaignId {} is INVALID", trigger.getCampaignID());
    //                                 trigger.setTriggerStatus(TriggerStatus.FAILED);
    //                                 trigger.setUpdatedOn(getCurrentDate());
    //                                 recommendationRepository.save(trigger);
    //                                 break;
    //                         }
    //                     }
    //                 }
    //             }
    //             log.info("end - triggerEngagement with {} total success request", totalSuccessTriggerRequest);
    //         } else {
    //             log.debug("end - triggerEngagement no engagement request");

    //         }

    //     }catch (Exception ex){
    //             log.error("triggerEngagement - Exception: {}", ex);
    //     }
    // }

    @Async
    public MessageTemplateDetailResponse getMessageTemplateDetail(Integer id) throws Exception {
        try {
            log.info("start - getMessageTemplateDetail for message template id : {}", id);
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(15000);
            factory.setReadTimeout(15000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            RestTemplate restTemplate = new RestTemplate(factory);

            Object response;

            response = restTemplate.getForObject(GET_MSG_TMP_DETAIL_END_POINT_URL + id, Object.class);

            log.info("getMessageTemplateDetail : {}", response);
            MessageTemplateDetailResponse messageTemplateDetailResponse = new MessageTemplateDetailResponse();

            if (response instanceof Map) {
                Map<String, Object> responseMap = (Map<String, Object>) response;
                if (responseMap.containsKey("title")) {
                    messageTemplateDetailResponse.setTitle(responseMap.get("title").toString());
                } else {
                    log.info("getMessageTemplateDetail - title field does not exist.");
                }

                if (responseMap.containsKey("content")) {
                    if (responseMap.get("content") != null) {
                        messageTemplateDetailResponse.setContent(responseMap.get("content").toString());
                    } else {
                        messageTemplateDetailResponse.setContent(null);
                    }
                } else {
                    log.info("getMessageTemplateDetail - content field does not exist.");
                }

                return messageTemplateDetailResponse;
            } else {
                log.info("getMessageTemplateDetail - Invalid response format.");
                return null;
            }

        } catch (Exception ex) {
            log.error("getMessageTemplateDetail - campaign id : {} | exception : {}", id, ex);
            return null;
        }
    }

    @Async
    public CampaignStatus checkCampaign(Integer id) throws Exception {
        try {
            log.info("start - checkCampaign for campaign id : {}", id);
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(15000);
            factory.setReadTimeout(15000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            RestTemplate restTemplate = new RestTemplate(factory);

            Object response;

            response = restTemplate.getForObject(CHECK_CAMPAIGN_END_POINT_URL + id, Object.class);

            if (response!=null){
                log.info("checkCampaign : {}", response);
                CampaignStatus campaignStatus = CampaignStatus.INVALID;
                CheckCampaignResponse checkCampaignResponse = new CheckCampaignResponse();

                if (response instanceof Map) {
                    Map<String, Object> responseMap = (Map<String, Object>) response;

                    if (responseMap.containsKey("result") && responseMap.get("result") instanceof Map) {
                        Map<String, Object> resultMap = (Map<String, Object>) responseMap.get("result");
                    
                        if (resultMap.containsKey("isValid") && resultMap.containsKey("isExpired") && resultMap.containsKey("isDisable")) {
                            checkCampaignResponse.setIsValid(Boolean.parseBoolean(resultMap.get("isValid").toString()));
                            checkCampaignResponse.setIsExpired(Boolean.parseBoolean(resultMap.get("isExpired").toString()));
                            checkCampaignResponse.setIsDisable(Boolean.parseBoolean(resultMap.get("isDisable").toString()));
                            log.info("checkCampaign - checkCampaignResponse : {}", checkCampaignResponse);
                            if(checkCampaignResponse.getIsValid()==true){
                                campaignStatus = CampaignStatus.VALID;
                            } else if (checkCampaignResponse.getIsExpired()==true){
                                campaignStatus = CampaignStatus.EXPIRED;
                            } else if (checkCampaignResponse.getIsDisable()==true){
                                campaignStatus = CampaignStatus.DISABLE;
                            } else {
                                campaignStatus = CampaignStatus.INVALID;
                            }
                            
                            return campaignStatus;
                        } else {
                            log.info("checkCampaign - field does not exist.");
                            return CampaignStatus.INVALID;
                        }
                    } else {
                        log.info("checkCampaign - field does not exist.");
                        return CampaignStatus.INVALID;
                    }
                    
                } else {
                    log.info("checkCampaign - Invalid response format.");
                    return CampaignStatus.INVALID;
                }
            } else{
                log.info("checkCampaign - campaign id {} not found", id);
                return CampaignStatus.INVALID;
            }
        } catch (Exception ex) {
            log.error("checkCampaign - exception : {} | campaign id : {}", ex, id);
            return CampaignStatus.INVALID;
        }
    }

    @Async
    public Date getCurrentDate() throws Exception {
        Date currentDate = new Date();
        long currentTimeInMillis = currentDate.getTime();
        long eightHoursInMillis = 8 * 60 * 60 * 1000; // 8 hours in milliseconds
        long malaysiaTimeInMillis = currentTimeInMillis + eightHoursInMillis;
        Date malaysiaTime = new Date(malaysiaTimeInMillis); // to store as malaysia time in cassandra since it will make the date be UTC
        return malaysiaTime;
    }

    @Async
    public void createEngagementSentHistory(Integer messageTemplateId, String messageRecipient,
            String communicationChannel, String language, String title, String messageContent, Integer campaignId) {
        try {
            log.info("createEngagementSentHistory - create engagement sent history for messageRecipient : {}",
                    messageRecipient);
            EngagementHistory engagementSentHistory = new EngagementHistory();

            engagementSentHistory.setMessageTemplateId(messageTemplateId);
            engagementSentHistory.setMessageRecipient(messageRecipient);
            engagementSentHistory.setCommunicationChannel(communicationChannel);
            engagementSentHistory.setLanguage(language);
            engagementSentHistory.setTitle(title);
            engagementSentHistory.setMessageContent(messageContent);
            engagementSentHistory.setSentDateTime(getCurrentDate());
            engagementSentHistory.setLatestStage(null);
            engagementSentHistory.setTriggerEventGeneralRuleName(null);
            engagementSentHistory.setApplicationSessionID(null);
            engagementSentHistory.setTriggerEventGeneralRuleId(null);
            engagementSentHistory.setCampaignId(campaignId);

            engagementHistoryRepository.save(engagementSentHistory);
        } catch (Exception ex) {
            log.error("createEngagementSentHistory - messageRecipient : {} | exception : {}", messageRecipient, ex);
        }
    }

    // public CustomerProfile getCustomerProfileViaCif(String cifNo) {

    //     try {

    //         TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
    //             @Override
    //             public void checkClientTrusted(X509Certificate[] x509Certificates, String s)
    //                     throws CertificateException {
    //             }

    //             @Override
    //             public void checkServerTrusted(X509Certificate[] x509Certificates, String s)
    //                     throws CertificateException {
    //             }

    //             @Override
    //             public X509Certificate[] getAcceptedIssuers() {
    //                 return null;
    //             }
    //         }
    //         };

    //         SSLContext sslContext = SSLContext.getInstance("TLS");
    //         sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

    //         // Create a custom HttpClient that uses the custom SSL context
    //         CloseableHttpClient httpClient = HttpClients.custom().setSSLContext(sslContext)
    //                 .setSSLHostnameVerifier((s, sslSession) -> true)
    //                 .build();

    //         // Create a custom request factory that uses the custom HttpClient
    //         HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
    //         requestFactory.setHttpClient(httpClient);
    //         requestFactory.setConnectTimeout(15000);
    //         requestFactory.setReadTimeout(15000);
    //         RestTemplate restTemplate = new RestTemplate(requestFactory);

    //         ResponseEntity<CustomerProfile> response = restTemplate.getForEntity(
    //                 GET_CUSTOMER_PROFILE_URL + cifNo,
    //                 CustomerProfile.class);

    //         log.info("getCustomerProfile : {}", response.getBody());

    //         return response.getBody();

    //     } catch (NullPointerException ex) {
    //         // Handle the NullPointerException gracefully
    //         ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
    //                 "NullPointerException occurred: " + ex.getMessage(), null);

    //         log.info("checkCustomerProfile : exception {} | response : {}", ex, apiResponse);
    //         // Return an appropriate response or take necessary action
    //         return null; // Return an empty CustomerProfileResponse object, or handle it based
    //                      // on your use case
    //     } catch (Exception ex) {
    //         // Handle other exceptions
    //         ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
    //                 ex.getLocalizedMessage(), null);

    //         log.info("checkCustomerProfile : exception {} | response : {}", ex, apiResponse);
    //         // Return an appropriate response or take necessary action
    //         return null; // Return null or handle it based on your use case
    //     }

    // }
}
