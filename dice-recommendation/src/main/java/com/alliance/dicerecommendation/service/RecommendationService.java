package com.alliance.dicerecommendation.service;

import java.util.Comparator;
import java.util.Date;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.kafka.common.protocol.types.Field.Bool;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.alliance.dicerecommendation.constant.ApiResponse;
import com.alliance.dicerecommendation.exception.ServiceException;
import com.alliance.dicerecommendation.model.DataField.CampaignStatus;
import com.alliance.dicerecommendation.model.DataField.Status;
import com.alliance.dicerecommendation.model.DataField.TriggerStatus;
import com.alliance.dicerecommendation.model.EngagementModeTemplate;
import com.alliance.dicerecommendation.model.Campaign;
import com.alliance.dicerecommendation.model.Customer;
import com.alliance.dicerecommendation.model.PermaLinkTemplate;
import com.alliance.dicerecommendation.model.PloanAssetTemplate;
import com.alliance.dicerecommendation.model.Recommendation;
import com.alliance.dicerecommendation.model.RecommendationHistoryLog;
import com.alliance.dicerecommendation.repository.EngagementModeTemplateRepository;
import com.alliance.dicerecommendation.repository.MediaAssetRepository;
import com.alliance.dicerecommendation.repository.PermaLinkTemplateRepository;
import com.alliance.dicerecommendation.repository.PersonalInfoTemplateRepository;
import com.alliance.dicerecommendation.repository.PloanAssetTemplateRepository;
import com.alliance.dicerecommendation.repository.RecommendationHistoryLogRepository;
import com.alliance.dicerecommendation.repository.RecommendationRepository;
import com.alliance.dicerecommendation.request.CreateRecommendationRequest;
import com.alliance.dicerecommendation.request.DeleteRecommendationRequest;
import com.alliance.dicerecommendation.request.ProfileRequest;
import com.alliance.dicerecommendation.request.TestRuleProcessRequest;
import com.alliance.dicerecommendation.request.TriggerEngagementPushNotiRequest;
import com.alliance.dicerecommendation.request.UpdateCampaignCustomerStatusRequest;
import com.alliance.dicerecommendation.request.UploadedFileHistoryRequest;
import com.alliance.dicerecommendation.response.CacheCustomerProfileSoleCCResponse;
import com.alliance.dicerecommendation.response.CampaignDetailResponse;
import com.alliance.dicerecommendation.response.CustomerProfileResponse;
import com.alliance.dicerecommendation.response.PermenantLinkResponse;
import com.alliance.dicerecommendation.response.PloanAssetsResponse;
import com.alliance.dicerecommendation.response.RecommendationResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RecommendationService {

    @Autowired
    MessageSource messageSource;

    @Autowired
    TriggerEngagementService triggerEngagementService;

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private RecommendationHistoryLogRepository recommendationHistoryLogRepository;

    @Autowired
    private MediaAssetRepository mediaAssetRepository;

    @Autowired
    private EngagementModeTemplateRepository engagementModeRepository;

    @Autowired
    private PersonalInfoTemplateRepository personalInfoTemplateRepository;

    @Autowired
    private PermaLinkTemplateRepository permaLinkTemplateRepository;

    @Autowired
    private PloanAssetTemplateRepository ploanAssetTemplateRepository;

    @Autowired
    private EngagementModeTemplateService engagementModeTemplateService;

    @Value("${test.process.recommendation.endpointUrl}")
    private String TEST_PROCESS_RECOMMENDATION_ENDPOINT_URL;

    @Value("${test.rule.engine.endpointUrl}")
    private String TEST_RULE_ENGINE_ENDPOINT_URL;

    // @Value("${pushDeviceId.endpointURL}")
    // private String PUSH_DEVICE_ID_END_POINT_URL;

    @Value("${getCampaignDetail.endpointURL}")
    private String GET_CAMPAIGN_DETAIL_END_POINT_URL;

    // @Value("${check.customer.profile.endpointUrl}")
    // private String CHECK_CUSTOMER_PROFILE_URL;

    @Value("${get.customer.plon.pdpa.endpointUrl}")
    private String GET_CUSTOMER_PLOAN_PDPA_ENDPOINT_URL;

    @Value("${addUploadedFileHistoryLog.endpointURL}")
    private String ADD_UPLOADED_FILE_HISTORY_LOG_END_POINT_URL;

    @Value("${permaLinkTemplateId.expired.campaign}")
    private String PERMA_LINK_TEMPLATE_ID_EXPIRED_CAMPAIGN;

    @Value("${permaLinkTemplateId}")
    private String PERMA_LINK_TEMPLATE_ID;

    @Value("${expired.campaign.inappmsgtemplateid.engagementmode.permenant}")
    private String INAPPMSG_TEMPLATE_ID_EXPIRED_CAMPAIGN_ENGAGEMENTMODE_PERMENANT;

    @Value("${ploan.assetid.with.consent.with.nta}")
    private String PLOAN_ASSET_TEMPLATE_ID_WITH_CONSENT_WITH_NTA;

    @Value("${ploan.assetid.with.consent.without.nta}")
    private String PLOAN_ASSET_TEMPLATE_ID_WITH_CONSENT_WITHOUT_NTA;

    @Value("${ploan.assetid.without.consent.with.nta}")
    private String PLOAN_ASSET_TEMPLATE_ID_WITHOUT_CONSENT_WITH_NTA;

    @Value("${ploan.assetid.without.consent.without.nta}")
    private String PLOAN_ASSET_TEMPLATE_ID_WITHOUT_CONSENT_WITHOUT_NTA;

    @Value("${get.campaign.by.name.endpointUrl}")
    private String GET_CAMPAIGN_BY_NAME_ENDPOINT_URL;

    @Value("${saveplus.referral.campaign.name}")
    private String SAVEPLUS_REFERRAL_CAMPAIGN_NAME;

    @Value("${vcc.bundling.cc.campaign.name}")
    private String VCC_BUNDLING_CC_CAMPAIGN_NAME;

    @Value("${vcc.bundling.ploan.campaign.name}")
    private String VCC_BUNDLING_PLOAN_CAMPAIGN_NAME;

    @Value("${dice.cache.get.profile.endpointURL}")
    private String DICE_CACHE_GET_PROFILE_ENDPOINT_URL;

    public RecommendationResponse getCampaignRecommendation(String uuidType, String uuid, String engagementMode,
            String campaignId)
            throws NumberFormatException, Exception {

        // log.info("start - getCampaignRecommendation");

        // VCC bundling checking
        if(engagementMode.equalsIgnoreCase("EKYC_CC_P1") || engagementMode.equalsIgnoreCase("EKYC_CC_P2") || 
        engagementMode.equalsIgnoreCase("EKYC_PL_P1") || engagementMode.equalsIgnoreCase("EKYC_PL_P2")){
            if (uuidType.equalsIgnoreCase("NRIC") && uuid != null){
                Boolean isExistingVccCustomer = false;
                ProfileRequest profileRequest = new ProfileRequest();
                profileRequest.setIdType("id_no");
                profileRequest.setIdValue(uuid);
                profileRequest.setTableName("EVT_VCC_ONBOARDING_FORMS_VIEW");
                isExistingVccCustomer = isExistingVccCustomer(profileRequest);
                
                if(isExistingVccCustomer == false){
                    log.info("getCampaignRecommendation - VCC bundling checking : uuidType = {}, uuid = {} , engagementmode = {}", uuidType, uuid, engagementMode);
                    Recommendation recommendation = new Recommendation();
                    Recommendation firstPromptRecommendation = new Recommendation();
                    Recommendation secondPromptRecommendation = new Recommendation();
                    Boolean showSecondPromptRecommendation = true;
                    if (engagementMode.equalsIgnoreCase("EKYC_CC_P1") || engagementMode.equalsIgnoreCase("EKYC_PL_P1")) {
                        recommendation = recommendationRepository.getByNricAndEngagementModeAndIsUpdated(uuid,engagementMode,false);
                        secondPromptRecommendation =null;
                    
                    }
                    if (engagementMode.equalsIgnoreCase("EKYC_CC_P2") || engagementMode.equalsIgnoreCase("EKYC_PL_P2")) {
                        if(engagementMode.equalsIgnoreCase("EKYC_CC_P2")){
                            
                            firstPromptRecommendation = recommendationRepository.getByNricAndEngagementModeAndIsUpdated(uuid,"EKYC_CC_P1",true);
                        }
                        else if(engagementMode.equalsIgnoreCase("EKYC_PL_P2")){
                            
                            firstPromptRecommendation = recommendationRepository.getByNricAndEngagementModeAndIsUpdated(uuid,"EKYC_PL_P1",true);
                        }
                    
                        secondPromptRecommendation = recommendationRepository.getByNricAndEngagementModeAndIsUpdated(uuid, engagementMode, false);
                    
                        if(firstPromptRecommendation != null || secondPromptRecommendation != null){
                            
                            showSecondPromptRecommendation = false;
                        } else {
                        
                            showSecondPromptRecommendation = true;
                        }
                        recommendation = null;
                    
                    }

                    if (recommendation == null && showSecondPromptRecommendation){
                        try {
                        
                            Campaign campaign= new Campaign();
                            CreateRecommendationRequest createRecommendationRequest = new CreateRecommendationRequest();
                            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                            factory.setConnectTimeout(15000);
                            factory.setReadTimeout(15000);

                            HttpHeaders headers = new HttpHeaders();
                            headers.setContentType(MediaType.APPLICATION_JSON);
                            RestTemplate restTemplate = new RestTemplate(factory);

                            String campaignName=null;
                            if(engagementMode.equalsIgnoreCase("EKYC_CC_P1") || engagementMode.equalsIgnoreCase("EKYC_CC_P2")){
                                campaignName = VCC_BUNDLING_CC_CAMPAIGN_NAME;
                            }

                            if(engagementMode.equalsIgnoreCase("EKYC_PL_P1") || engagementMode.equalsIgnoreCase("EKYC_PL_P2")){
                                campaignName = VCC_BUNDLING_PLOAN_CAMPAIGN_NAME;
                            }

                            ResponseEntity<Campaign> response = restTemplate.getForEntity(
                                    GET_CAMPAIGN_BY_NAME_ENDPOINT_URL
                                            + "?campaignName=" + campaignName,
                                    Campaign.class);

                            campaign = response.getBody();

                            log.info("getCampaignRecommendation - VCC bundling checking - getCampaignByName: " + campaign);

                            Date startDate = null;
                            Date endDate = null;
                            Boolean isCampaignExpired = false;

                            startDate = campaign.getStartDate();
                            endDate = campaign.getEndDate();

                            if (startDate != null && endDate != null) {
                                Date now = new Date();
                                if (now.after(endDate)) {
                                    isCampaignExpired = true;
                                }
                            }
                            log.info("getCampaignRecommendation - VCC bundling checking, CampaignStatus = {} | isCampaignExpired = {}", campaign.getCampaignStatus(), isCampaignExpired);

                            if (campaign.getCampaignStatus() != Status.DISABLE && isCampaignExpired == false) {
                                createRecommendationRequest.setCampaignID(campaign.getCampaignId());
                                createRecommendationRequest.setCampaignPriority(campaign.getCampaignPriority());
                                createRecommendationRequest.setCampaignName(campaign.getCampaignName());
                                createRecommendationRequest.setCampaignDescription(campaign.getDescription());
                                createRecommendationRequest.setCampaignStartDate(campaign.getStartDate());
                                createRecommendationRequest.setCampaignEndDate(campaign.getEndDate());

                                Map<String, Integer> messageTemplateIdMap = new HashMap<>();
                                // if (campaign.getCampaignProperties().containsKey("pushNotificationTemplateId")) {
                                //     messageTemplateIdMap.put("pushNotificationTemplateId",
                                //             Integer.parseInt(campaign.getCampaignProperties().get("pushNotificationTemplateId")));
                                // }
                                // if (campaign.getCampaignProperties().containsKey("inAppMessageTemplateId")) {
                                //     messageTemplateIdMap.put("inAppMessageTemplateId",
                                //             Integer.parseInt(campaign.getCampaignProperties().get("inAppMessageTemplateId")));
                                // }
                                if (campaign.getCampaignProperties().containsKey("multiInAppMessageTemplateId")) {
                                    Integer messageTemplateIdToPrompt;
                                    String[] tempMultiInAppMessageTemplateId = campaign.getCampaignProperties().get("multiInAppMessageTemplateId").split(",");
                                    if (tempMultiInAppMessageTemplateId.length > 0) {
                                        if (engagementMode.equalsIgnoreCase("EKYC_CC_P1") || engagementMode.equalsIgnoreCase("EKYC_PL_P1")){
                                            messageTemplateIdToPrompt = Integer.parseInt(tempMultiInAppMessageTemplateId[0]);
                                        }
                                        else {
                                            messageTemplateIdToPrompt = Integer.parseInt(tempMultiInAppMessageTemplateId[1]);
                                        }
                                    } else {
                                        messageTemplateIdToPrompt = null;
                                        log.warn("getCampaignRecommendation - multiInAppMessageTemplateId not found");
                                    }
                                    messageTemplateIdMap.put("inAppMessageTemplateId", messageTemplateIdToPrompt);
                                }

                                createRecommendationRequest.setMessageTemplateId(messageTemplateIdMap);
                    
                                createRecommendationRequest.setCifNo(uuid);
                                createRecommendationRequest.setDeviceId(null);
                                createRecommendationRequest.setIsIgnore(false);
                                createRecommendationRequest.setIsTriggered(false);
                                createRecommendationRequest.setIsCampaignUpdated(false);
                                createRecommendationRequest.setPushNotificationSendDateTime(null);

                                Set<String> engagementModeSet = new HashSet<>();
                                // String[] engagementModeId = campaign.getCampaignProperties().get("engagementModeTemplateId").split(",");
                                // for (String e : engagementModeId) {
                                //     Optional<EngagementModeTemplate> template = engagementModeTemplateService
                                //             .getTemplate(Integer.parseInt(e));
                                //     engagementModeSet.add(template.get().getEngagementModeName());
                                // }
                                engagementModeSet.add(engagementMode);
                                createRecommendationRequest.setEngagementModeId(engagementModeSet);
                                
                                createRecommendation(createRecommendationRequest);
                            }
                        } catch (Exception ex){
                            throw new ServiceException(ApiResponse.HTTP_RESPONSE_OK,
                                    messageSource.getMessage("get.campaign.name.failure", null, Locale.getDefault()));
                        }
                    }
                }
            }
        }

        RecommendationResponse recommendationResponse = new RecommendationResponse();
        List<Recommendation> recommendation = new ArrayList<>();
        if (campaignId != null && engagementMode.equalsIgnoreCase("BELL") || engagementMode.equalsIgnoreCase("PUSH")) {
            log.info("getCampaignRecommendation - recommendation for engagement : {}", engagementMode);
            Recommendation tempRecommendation = new Recommendation();
            tempRecommendation = recommendationRepository.getByCifNoAndCampaignIdAndEngagementModeForCompleted(uuid,
                    engagementMode, Integer.parseInt(campaignId));
            // log.info("getCampaignRecommendation - recommendation : {}", tempRecommendation);
            if (tempRecommendation == null) {
                tempRecommendation = recommendationRepository
                        .getByCifNoAndCampaignIdAndEngagementModeForNonTrigger(uuid, engagementMode,
                                Integer.parseInt(campaignId));
                recommendation.add(tempRecommendation);
            } else {
                recommendation.add(tempRecommendation);
            }

        } else {

            recommendation = recommendationRepository.getByCifNoAndEngagementMode(uuid, engagementMode);
        }
        // log.debug("getCampaignRecommendation-listOfRecommendations - {}",
        // recommendation);
        Recommendation oldestCampaignWithLowestPriority = new Recommendation();

        if (!recommendation.isEmpty()) {
            List<Recommendation> validRecommendationList = new ArrayList<>();
            for (Recommendation recommendationFilter : recommendation) {
                CampaignStatus campaignStatus = triggerEngagementService
                        .checkCampaign(recommendationFilter.getCampaignID());
                if (campaignStatus == CampaignStatus.VALID) {
                    validRecommendationList.add(recommendationFilter);
                }
            }
            
            if (campaignId != null && engagementMode.equalsIgnoreCase("BELL")
                    || engagementMode.equalsIgnoreCase("PUSH") || engagementMode.equalsIgnoreCase("EKYC_CC_P1") || engagementMode.equalsIgnoreCase("EKYC_CC_P2") || 
        engagementMode.equalsIgnoreCase("EKYC_PL_P1") || engagementMode.equalsIgnoreCase("EKYC_PL_P2")) {
                oldestCampaignWithLowestPriority = recommendation.get(0);
            } else {
                // send the recommendation list to check based on campaignPriority and
                // creationdate
                oldestCampaignWithLowestPriority = getCampaignWithLowestPriorityValue(validRecommendationList);
                // log.info("getCampaignRecommendation-oldestCampaignWithLowestPriority - {}",
                // oldestCampaignWithLowestPriority);
            }

            if (oldestCampaignWithLowestPriority != null) {

                recommendationResponse
                        .setEngagementMode(String.join(", ", oldestCampaignWithLowestPriority.getEngagementMode()));
                recommendationResponse.setUpdateSequence(
                        oldestCampaignWithLowestPriority.getUpdateSequence() != null
                                ? String.join(", ", oldestCampaignWithLowestPriority.getUpdateSequence())
                                : null);
                recommendationResponse
                        .setHighRisk(oldestCampaignWithLowestPriority.getHighRisk() != null
                                ? String.join(", ", oldestCampaignWithLowestPriority.getHighRisk())
                                : null);
                recommendationResponse.setCampaignId(oldestCampaignWithLowestPriority.getCampaignID().toString());
                Map<String, Integer> msgTemplate = oldestCampaignWithLowestPriority.getMessageTemplateId();
                if (campaignId != null && engagementMode.equalsIgnoreCase("BELL")
                        || engagementMode.equalsIgnoreCase("PUSH")) {
                    CampaignStatus validationStatus = triggerEngagementService
                            .checkCampaign(Integer.parseInt(campaignId));
                    switch (validationStatus) {

                        case VALID:
                            log.info("getCampaignRecommendation - campaignId {} is VALID", campaignId);
                            if (oldestCampaignWithLowestPriority.getIsCampaignUpdated() == true) {
                                if (msgTemplate.containsKey("actionCompleteMessageTemplateId")) {
                                    recommendationResponse.setInAppMessageTemplateId(
                                            msgTemplate.get("actionCompleteMessageTemplateId").toString());
                                }
                            } else {
                                if (msgTemplate.containsKey("inAppMessageTemplateId")) {
                                    recommendationResponse.setInAppMessageTemplateId(
                                            msgTemplate.get("inAppMessageTemplateId").toString());
                                }
                            }
                            break;
                        case EXPIRED:
                            log.info("getCampaignRecommendation - campaignId {} is EXPIRED", campaignId);
                            if (msgTemplate.containsKey("expiredMessageTemplateId")) {
                                recommendationResponse.setInAppMessageTemplateId(
                                        msgTemplate.get("expiredMessageTemplateId").toString());
                            }
                            break;
                        case DISABLE:
                            log.info("getCampaignRecommendation - campaignId {} is DISABLE", campaignId);
                            if (msgTemplate.containsKey("disabledMessageTemplateId")) {
                                recommendationResponse.setInAppMessageTemplateId(
                                        msgTemplate.get("disabledMessageTemplateId").toString());
                            }
                            break;
                        case INVALID:
                            log.info("getCampaignRecommendation - campaignId {} is INVALID", campaignId);
                            if (msgTemplate.containsKey("disabledMessageTemplateId")) {
                                recommendationResponse.setInAppMessageTemplateId(
                                        msgTemplate.get("disabledMessageTemplateId").toString());
                            }
                            break;
                        default:
                            log.warn("getCampaignRecommendation - campaignId {} is not able to validate", campaignId);
                            if (msgTemplate.containsKey("disabledMessageTemplateId")) {
                                recommendationResponse.setInAppMessageTemplateId(
                                        msgTemplate.get("disabledMessageTemplateId").toString());
                            }
                            break;
                    }
                } else {
                    if (msgTemplate.containsKey("inAppMessageTemplateId")) {
                        recommendationResponse
                                .setInAppMessageTemplateId(msgTemplate.get("inAppMessageTemplateId").toString());
                    }
                }
                recommendationResponse.setIsCampaignUpdated(oldestCampaignWithLowestPriority.getIsCampaignUpdated());
                recommendationResponse.setIsIgnore(oldestCampaignWithLowestPriority.getIsIgnore());

                if (oldestCampaignWithLowestPriority.getFloatIconImage() != null) {
                    recommendationResponse.setFloatingBtnImg(
                            mediaAssetRepository.getPathUrlByMediaAssetId(
                                    Integer.parseInt(oldestCampaignWithLowestPriority.getFloatIconImage())));
                } else {
                    recommendationResponse.setFloatingBtnImg(null);
                }

                if (uuidType.equals("CIF_NO")) {
                    if (engagementMode.equals("PERMENANT")) {
                        log.info("getCampaignRecommendation - CIF_NO and PERMENANT");
                        PermenantLinkResponse permenantLinkResponse = new PermenantLinkResponse();
                        PermaLinkTemplate permaLinkTemplate = permaLinkTemplateRepository
                                .getPermaLinkTemplateViaId(Integer.parseInt(PERMA_LINK_TEMPLATE_ID));
                        permenantLinkResponse.setType(permaLinkTemplate.getType());
                        permenantLinkResponse.setTitleText(permaLinkTemplate.getTitle());
                        permenantLinkResponse.setIconImg(
                                mediaAssetRepository.getPathUrlByMediaAssetId(permaLinkTemplate.getIconImageId()));

                        recommendationResponse.setPermanentLink(permenantLinkResponse);
                    }
                } else if (uuidType.equals("EFORM_UUID")) {
                    if (engagementMode.equals("PLOAN")) {
                        log.info("getCampaignRecommendation - EFORM_UUID and PLOAN");

                        JSONObject response = getCustomerPloanPdpa(uuid);
                        log.info("getCampaignRecommendation - uuid : {} , customerPdpa: {}", uuid, response.get("IS_PDPA_CONSENT"));

                        Object customerPdpaConsent = response.get("IS_PDPA_CONSENT");
                        Object customerNta = response.get("IS_NTA");
                        PloanAssetsResponse ploanAssetsResponse = new PloanAssetsResponse();

                        if (customerPdpaConsent != null && customerPdpaConsent.equals(1)) {
                            if (customerNta != null && customerNta.equals(1)) {
                                PloanAssetTemplate ploanAssetTemplate = ploanAssetTemplateRepository
                                        .getPloanAssetTemplateViaId(
                                                Integer.parseInt(PLOAN_ASSET_TEMPLATE_ID_WITH_CONSENT_WITH_NTA));

                                ploanAssetsResponse.setIsShowPromo(ploanAssetTemplate.getIsShowPromo());
                                ploanAssetsResponse.setOkBtnType(ploanAssetTemplate.getOkBtnType());

                            } else {
                                PloanAssetTemplate ploanAssetTemplate = ploanAssetTemplateRepository
                                        .getPloanAssetTemplateViaId(
                                                Integer.parseInt(PLOAN_ASSET_TEMPLATE_ID_WITH_CONSENT_WITHOUT_NTA));

                                ploanAssetsResponse.setIsShowPromo(ploanAssetTemplate.getIsShowPromo());
                                ploanAssetsResponse.setOkBtnType(ploanAssetTemplate.getOkBtnType());

                            }

                        } else if (customerPdpaConsent.equals(null) || customerPdpaConsent.equals(0)) {

                            if (customerNta != null && customerNta.equals(1)) {
                                PloanAssetTemplate ploanAssetTemplate = ploanAssetTemplateRepository
                                        .getPloanAssetTemplateViaId(
                                                Integer.parseInt(PLOAN_ASSET_TEMPLATE_ID_WITHOUT_CONSENT_WITH_NTA));

                                ploanAssetsResponse.setIsShowPromo(ploanAssetTemplate.getIsShowPromo());
                                ploanAssetsResponse.setOkBtnType(ploanAssetTemplate.getOkBtnType());
                                ploanAssetsResponse
                                        .setPromoBackgroundStyle(ploanAssetTemplate.getPromoBackgroundStyle());
                                ploanAssetsResponse.setPromoImg(mediaAssetRepository
                                        .getPathUrlByMediaAssetId(ploanAssetTemplate.getPromoImg()));
                                ploanAssetsResponse.setPromoDesc(ploanAssetTemplate.getPromoDesc());
                                ploanAssetsResponse.setPromoBtnType(ploanAssetTemplate.getPromoBtnType());
                                ploanAssetsResponse.setPromoBtnDesc(ploanAssetTemplate.getPromoBtnDesc());
                                ploanAssetsResponse.setSubmitBtnType(ploanAssetTemplate.getSubmitBtnType());

                            } else {

                                PloanAssetTemplate ploanAssetTemplate = ploanAssetTemplateRepository
                                        .getPloanAssetTemplateViaId(
                                                Integer.parseInt(PLOAN_ASSET_TEMPLATE_ID_WITHOUT_CONSENT_WITHOUT_NTA));

                                ploanAssetsResponse.setIsShowPromo(ploanAssetTemplate.getIsShowPromo());
                                ploanAssetsResponse.setOkBtnType(ploanAssetTemplate.getOkBtnType());

                            }

                        }

                        recommendationResponse.setPloanAssest(ploanAssetsResponse);
                    }
                }

            } else {
                throw new ServiceException(ApiResponse.HTTP_RESPONSE_OK,
                        messageSource.getMessage("get.campaign.recommendation.null.value", null, Locale.getDefault()));
            }

        } else {
            log.info("getCampaignRecommendation - no recommendation result found for uuidType: {} | uuid : {}",
                    uuidType, uuid);

            if (uuidType.equals("CIF_NO") && engagementMode.equals("PERMENANT")) {

                // To check for referral campaign status and expiration date

                try {
                    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                    factory.setConnectTimeout(15000);
                    factory.setReadTimeout(15000);

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    RestTemplate restTemplate = new RestTemplate(factory);

                    ResponseEntity<Campaign> response = restTemplate.getForEntity(
                            GET_CAMPAIGN_BY_NAME_ENDPOINT_URL
                                    + "?campaignName=" + SAVEPLUS_REFERRAL_CAMPAIGN_NAME,
                            Campaign.class);

                    Campaign campaign = response.getBody();

                    log.info("getCampaignRecommendation - getCampaignByName: " + campaign);

                    Date startDate = null;
                    Date endDate = null;
                    Boolean isCampaignExpired = false;

                    startDate = campaign.getStartDate();
                    endDate = campaign.getEndDate();

                    if (startDate != null && endDate != null) {
                        Date now = new Date();
                        if (now.after(endDate)) {
                            isCampaignExpired = true;
                        }
                    }
                    log.info("getCampaignRecommendation - general perma menu, CampaignStatus = {} | isCampaignExpired = {}", campaign.getCampaignStatus(), isCampaignExpired);

                    if (campaign.getCampaignStatus() != Status.DISABLE && isCampaignExpired) {
                        Customer customer = new Customer();
                        ProfileRequest profileRequest = new ProfileRequest();
                        profileRequest.setIdType("cif_no");
                        profileRequest.setIdValue(uuid);
                        profileRequest.setTableName("EVT_SOLE_CC_VIEW");
                        CacheCustomerProfileSoleCCResponse cacheCustomerProfileSoleCC = checkCustomerProfile(profileRequest);
                        if (cacheCustomerProfileSoleCC != null) {
                            if (cacheCustomerProfileSoleCC.getPackageId() != null) {
                                if (cacheCustomerProfileSoleCC.getPackageId().equals("3")) {
                                    log.info("getCampaignRecommendation - general perma menu - SOLO_CC, PackageType = {}", cacheCustomerProfileSoleCC.getPackageId());
                                    customer.setCustomerType("SOLO_CC");
                                } else {
                                    log.info("getCampaignRecommendation - general perma menu - GENERIC, PackageType = {}", cacheCustomerProfileSoleCC.getPackageId());
                                    customer.setCustomerType("GENERIC");
                                }
                            }
                        } else {
                            throw new ServiceException(ApiResponse.HTTP_RESPONSE_OK,
                                    messageSource.getMessage("get.campaign.recommendation.null.value", null,
                                            Locale.getDefault()));
                        }

                        if (customer.getCustomerType() == "GENERIC") {
                            log.info("getCampaignRecommendation- general perma menu - CIF_NO and PERMENANT");
                            PermenantLinkResponse permenantLinkResponse = new PermenantLinkResponse();
                            PermaLinkTemplate permaLinkTemplate = permaLinkTemplateRepository
                                    .getPermaLinkTemplateViaId(
                                            Integer.parseInt(PERMA_LINK_TEMPLATE_ID_EXPIRED_CAMPAIGN));
                            permenantLinkResponse.setType(permaLinkTemplate.getType());
                            permenantLinkResponse.setTitleText(permaLinkTemplate.getTitle());
                            permenantLinkResponse.setIconImg(
                                    mediaAssetRepository.getPathUrlByMediaAssetId(permaLinkTemplate.getIconImageId()));

                            recommendationResponse.setEngagementMode("PERMENANT");
                            recommendationResponse.setPermanentLink(permenantLinkResponse);
                            recommendationResponse
                                    .setInAppMessageTemplateId(
                                            INAPPMSG_TEMPLATE_ID_EXPIRED_CAMPAIGN_ENGAGEMENTMODE_PERMENANT);
                        }

                    }
                } catch (Exception e) {
                    throw new ServiceException(ApiResponse.HTTP_RESPONSE_OK,
                            messageSource.getMessage("get.campaign.name.failure", null, Locale.getDefault()));
                }

            } else if (uuidType.equals("EFORM_UUID") && engagementMode.equals("PLOAN")) {
                PloanAssetsResponse ploanAssetsResponse = new PloanAssetsResponse();

                ploanAssetsResponse.setIsShowPromo(false);
                ploanAssetsResponse.setOkBtnType("HOME");
                recommendationResponse.setPloanAssest(ploanAssetsResponse);

            } else {
                log.info("getCampaignRecommendation - Unable to find record with the uuidType: {} | uuid : {}", uuidType, uuid);
                throw new ServiceException(ApiResponse.HTTP_RESPONSE_OK,
                        messageSource.getMessage("get.campaign.recommendation.null.value", null, Locale.getDefault()));
            }
        }

        // log.info("getCampaignRecommendation - recommendationResponse {}", recommendationResponse);
        
        //setMessageDisplayMethod
        if(oldestCampaignWithLowestPriority.getCampaignID()!=null) {
            CampaignDetailResponse campaignDetailResponse = new CampaignDetailResponse();
                campaignDetailResponse = getCampaignDetail(oldestCampaignWithLowestPriority.getCampaignID());
                Map<String, String> campaignProperties = campaignDetailResponse.getCampaignProperties();
            if (campaignProperties != null && campaignProperties.containsKey("messageDisplayMethod")) {
                String messageDisplayMethod = campaignProperties.get("messageDisplayMethod");
                recommendationResponse.setMessageDisplayMethod(messageDisplayMethod);
            } else {
                // default messageDisplayMethod
                recommendationResponse.setMessageDisplayMethod("INAPPMSG");
            }
        } else {
            // default messageDisplayMethod
            recommendationResponse.setMessageDisplayMethod("INAPPMSG");
        }
       
        recommendationResponse.setIsBase64Content(false);
        return recommendationResponse;

    }

    public void createRecommendation(CreateRecommendationRequest createRecommendationRequest) {
        log.info(" createRecommendation - from Service : request {}", createRecommendationRequest);
        Boolean isCreateNewRecommendation = true;
        String engagementMode = String.join(", ", createRecommendationRequest.getEngagementModeId());
        if(engagementMode.equalsIgnoreCase("EKYC_CC_P1") || engagementMode.equalsIgnoreCase("EKYC_CC_P2") || engagementMode.equalsIgnoreCase("EKYC_PL_P1") || engagementMode.equalsIgnoreCase("EKYC_PL_P2")){
            log.info("test5");
            if(recommendationRepository.getByNricAndEngagementMode(createRecommendationRequest.getCifNo(),engagementMode)!=null){
                log.warn("Cannot create recommendation - a recommendation already exists for NRIC = {} , Campaign ID = {} and engagement mode = {}",
                        createRecommendationRequest.getCifNo(), createRecommendationRequest.getCampaignID(), engagementMode);
                        log.info("test6");
                        isCreateNewRecommendation = false;
            }
        } else {
            log.info("test7");
            if (recommendationRepository.countByCifNoAndCampaignId(createRecommendationRequest.getCifNo(),
                    createRecommendationRequest.getCampaignID()) > 0) {
                log.warn("Cannot create recommendation - a recommendation already exists for CIF No {} And Campaign ID {}",
                        createRecommendationRequest.getCifNo(), createRecommendationRequest.getCampaignID());

                List<Recommendation> existingRecommendation = recommendationRepository.findByCifNoAndCampaignId(
                        createRecommendationRequest.getCifNo(),
                        createRecommendationRequest.getCampaignID());

                log.info("existingRecommendation: {}", existingRecommendation);
                isCreateNewRecommendation = false;

                for (Recommendation r : existingRecommendation) {
                    if (!r.getIsIgnore()) {
                        isCreateNewRecommendation = true;
                        createRecommendationHistory(r);
                        recommendationRepository.delete(r);
                    }
                }
            }
        }
        if (isCreateNewRecommendation) {
            log.info("Creating recommendation for CIF No {}", createRecommendationRequest.getCifNo());

            Recommendation recommendation = new Recommendation();
            recommendation.setCampaignID(createRecommendationRequest.getCampaignID());
            recommendation.setCampaignPriority(createRecommendationRequest.getCampaignPriority());
            recommendation.setCampaignName(createRecommendationRequest.getCampaignName());
            recommendation.setCampaignDescription(createRecommendationRequest.getCampaignDescription());
            recommendation.setCampaignStartDate(createRecommendationRequest.getCampaignEndDate());
            recommendation.setCampaignEndDate(createRecommendationRequest.getCampaignEndDate());
            recommendation.setReferralCode(createRecommendationRequest.getReferralCode());
            recommendation.setPermanentLinkType(createRecommendationRequest.getPermanentLinkType());
            recommendation.setPermanentLinkText(createRecommendationRequest.getPermanentTitleText());
            recommendation.setEngagementMode(createRecommendationRequest.getEngagementModeId());
            recommendation.setRewardAmount(createRecommendationRequest.getRewardAmount());
            recommendation.setRewardType(createRecommendationRequest.getRewardType());
            recommendation.setTriggerTypeId(createRecommendationRequest.getTriggerTypeId());
            recommendation.setMessageTemplateId(createRecommendationRequest.getMessageTemplateId());
            recommendation.setCustomerSegmentationId(createRecommendationRequest.getCustomerSegmentationId());
            recommendation.setCifNo(createRecommendationRequest.getCifNo());
            recommendation.setDeviceId(createRecommendationRequest.getDeviceId());
            recommendation.setIsIgnore(createRecommendationRequest.getIsIgnore());
            recommendation.setIsTriggered(createRecommendationRequest.getIsTriggered());
            recommendation.setIsCampaignUpdated(createRecommendationRequest.getIsCampaignUpdated());
            recommendation
                    .setPushNotificationSendDateTime(
                            createRecommendationRequest.getPushNotificationSendDateTime());
            recommendation.setDiceActionTemplateId(createRecommendationRequest.getDiceActionTemplateId());
            recommendation.setUpdateSequence(createRecommendationRequest.getUpdateSequence());
            recommendation.setHighRisk(createRecommendationRequest.getHighRisk());
            recommendation.setTriggerStatus(createRecommendationRequest.getTriggerStatus());
            recommendation.setFloatIconImage(createRecommendationRequest.getFloatIconImage());

            recommendationRepository.save(recommendation);
        } else {
            if(!(engagementMode.equalsIgnoreCase("EKYC_CC_P1") || engagementMode.equalsIgnoreCase("EKYC_CC_P2") || engagementMode.equalsIgnoreCase("EKYC_PL_P1") || engagementMode.equalsIgnoreCase("EKYC_PL_P2"))){
                log.info("Creating recommendation for CIF No {}", createRecommendationRequest.getCifNo());

                Recommendation recommendation = new Recommendation();
                recommendation.setCampaignID(createRecommendationRequest.getCampaignID());
                recommendation.setCampaignPriority(createRecommendationRequest.getCampaignPriority());
                recommendation.setCampaignName(createRecommendationRequest.getCampaignName());
                recommendation.setCampaignDescription(createRecommendationRequest.getCampaignDescription());
                recommendation.setCampaignStartDate(createRecommendationRequest.getCampaignEndDate());
                recommendation.setCampaignEndDate(createRecommendationRequest.getCampaignEndDate());
                recommendation.setReferralCode(createRecommendationRequest.getReferralCode());
                recommendation.setPermanentLinkType(createRecommendationRequest.getPermanentLinkType());
                recommendation.setPermanentLinkText(createRecommendationRequest.getPermanentTitleText());
                recommendation.setEngagementMode(createRecommendationRequest.getEngagementModeId());
                recommendation.setRewardAmount(createRecommendationRequest.getRewardAmount());
                recommendation.setRewardType(createRecommendationRequest.getRewardType());
                recommendation.setTriggerTypeId(createRecommendationRequest.getTriggerTypeId());
                recommendation.setMessageTemplateId(createRecommendationRequest.getMessageTemplateId());
                recommendation.setCustomerSegmentationId(createRecommendationRequest.getCustomerSegmentationId());
                recommendation.setCifNo(createRecommendationRequest.getCifNo());
                recommendation.setDeviceId(createRecommendationRequest.getDeviceId());
                recommendation.setIsIgnore(createRecommendationRequest.getIsIgnore());
                recommendation.setIsTriggered(createRecommendationRequest.getIsTriggered());
                recommendation.setIsCampaignUpdated(createRecommendationRequest.getIsCampaignUpdated());
                recommendation
                        .setPushNotificationSendDateTime(createRecommendationRequest.getPushNotificationSendDateTime());
                recommendation.setDiceActionTemplateId(createRecommendationRequest.getDiceActionTemplateId());
                recommendation.setUpdateSequence(createRecommendationRequest.getUpdateSequence());
                recommendation.setHighRisk(createRecommendationRequest.getHighRisk());
                recommendation.setTriggerStatus(createRecommendationRequest.getTriggerStatus());
                recommendation.setFloatIconImage(createRecommendationRequest.getFloatIconImage());

                recommendationRepository.save(recommendation);
            }
        }

    }

    public void createRecommendationHistory(Recommendation recommendation) {
        // log.info("createRecommendationHistory-- {} ", recommendation);
        RecommendationHistoryLog recommendationHistoryLog = new RecommendationHistoryLog();
        recommendationHistoryLog.setCampaignID(recommendation.getCampaignID());
        recommendationHistoryLog.setCampaignPriority(recommendation.getCampaignPriority());
        recommendationHistoryLog.setCampaignName(recommendation.getCampaignName());
        recommendationHistoryLog.setCampaignDescription(recommendation.getCampaignDescription());
        recommendationHistoryLog.setCampaignStartDate(recommendation.getCampaignEndDate());
        recommendationHistoryLog.setCampaignEndDate(recommendation.getCampaignEndDate());
        recommendationHistoryLog.setReferralCode(recommendation.getReferralCode());
        recommendationHistoryLog.setPermanentLinkType(recommendation.getPermanentLinkType());
        recommendationHistoryLog.setPermanentLinkText(recommendation.getPermanentLinkText());
        recommendationHistoryLog.setEngagementMode(recommendation.getEngagementMode());
        recommendationHistoryLog.setRewardAmount(recommendation.getRewardAmount());
        recommendationHistoryLog.setRewardType(recommendation.getRewardType());
        recommendationHistoryLog.setTriggerTypeId(recommendation.getTriggerTypeId());
        recommendationHistoryLog.setMessageTemplateId(recommendation.getMessageTemplateId());
        recommendationHistoryLog.setCustomerSegmentationId(recommendation.getCustomerSegmentationId());
        recommendationHistoryLog.setCifNo(recommendation.getCifNo());
        recommendationHistoryLog.setDeviceId(recommendation.getDeviceId());
        recommendationHistoryLog.setIsIgnore(recommendation.getIsIgnore());
        recommendationHistoryLog.setIsTriggered(recommendation.getIsTriggered());
        recommendationHistoryLog.setIsCampaignUpdated(recommendation.getIsCampaignUpdated());
        recommendationHistoryLog.setPushNotificationSendDateTime(recommendation.getPushNotificationSendDateTime());
        recommendationHistoryLog.setDiceActionTemplateId(recommendation.getDiceActionTemplateId());
        recommendationHistoryLog.setUpdateSequence(recommendation.getUpdateSequence());
        recommendationHistoryLog.setHighRisk(recommendation.getHighRisk());
        recommendationHistoryLog.setTriggerStatus(recommendation.getTriggerStatus());
        recommendationHistoryLog.setFloatIconImage(recommendation.getFloatIconImage());
        recommendationHistoryLogRepository.save(recommendationHistoryLog);
    }

    public JSONObject getCustomerPloanPdpa(String uuid) throws ServiceException {
        try {
            log.info("gettingCustomerPloanPdpa...");
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(15000);
            factory.setReadTimeout(15000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            RestTemplate restTemplate = new RestTemplate(factory);
            ResponseEntity<String> response = restTemplate.getForEntity(
                    GET_CUSTOMER_PLOAN_PDPA_ENDPOINT_URL + uuid,
                    String.class);
            log.info("getCustomerPloanPdpa : {}", response.getBody());

            JSONObject responseJson = new JSONObject(response.getBody());
            log.info("responseJson : {}", responseJson.toString());
            JSONObject result = responseJson.getJSONObject("result");

            return result;
        } catch (Exception ex) {
            log.error("getCustomerPloanPdpa - Exception: {}", ex.getMessage());
            throw new ServiceException(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, ex.toString());
        }
    }

    public void processRecommendation(String uuidType, String uuid) {
        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(15000);
            factory.setReadTimeout(15000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            RestTemplate restTemplate = new RestTemplate(factory);
            TestRuleProcessRequest testRuleRequest = new TestRuleProcessRequest();
            testRuleRequest.setUuid(uuid);
            testRuleRequest.setUuidType(uuidType);

            Object response;
            response = restTemplate.postForObject(
                    TEST_RULE_ENGINE_ENDPOINT_URL, testRuleRequest, Object.class);
            log.info("processRecommendation : {}", response);

        } catch (Exception ex) {

            ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
                    ex.getLocalizedMessage(), null);

            log.info("processRecommendation : exception {} | response : {}", ex, apiResponse);
        }
    }

    // get campaign with lowest value (highest priority)
    public Recommendation getCampaignWithLowestPriorityValue(List<Recommendation> recommendationList)
            throws JSONException {

        log.info(recommendationList.size() + " recommendations");
        List<Map<Object, Recommendation>> recommendations = new ArrayList<>();

        for (Recommendation recommendation : recommendationList) {
            Map<Object, Recommendation> recommendationMap = new HashMap<>();
            JSONObject parentCampaign = getParentCampaign(recommendation.getCampaignID());

            recommendationMap.put(parentCampaign.get("campaignPriority"),
                    recommendation);
            recommendations.add(recommendationMap);
        }

        log.info("getCampaignWithLowestPriorityValue - recommendations : {}",
                recommendations);

        // return recommendationList.stream()
        // .min(Comparator.comparingInt(Recommendation::getCampaignPriority)
        // .thenComparing(Recommendation::getCreatedOn))
        // .orElse(null);

        Optional<Map<Object, Recommendation>> lowestPriorityRecommendationMap = recommendations.stream()
                .min(Comparator.comparing(map -> (Integer) map.keySet().iterator().next()));

        return lowestPriorityRecommendationMap.map(map -> map.values().iterator().next()).orElse(null);

    }

    public JSONObject getParentCampaign(Integer campaignId) {
        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(15000);
            factory.setReadTimeout(15000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            RestTemplate restTemplate = new RestTemplate(factory);

            ResponseEntity<String> response = restTemplate.exchange(
                    GET_CAMPAIGN_DETAIL_END_POINT_URL + campaignId,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class);

            String jsonResponse = response.getBody();
            JSONObject parentCampaignJson = new JSONObject(jsonResponse);

            log.info("getParentCampaign : {}", parentCampaignJson);
            return parentCampaignJson;
        } catch (Exception ex) {
            log.error("getParentCampaign - campaign id : {} | exception : {}", campaignId, ex);
            return null;
        }
    }

    public void deleteRecommendation(DeleteRecommendationRequest deleteRecommendationRequest) {
        List<Recommendation> existingRecommendation = recommendationRepository.findByCifNoAndCampaignId(
                deleteRecommendationRequest.getCifNo(),
                deleteRecommendationRequest.getCampaignId());

        for (Recommendation r : existingRecommendation) {
            createRecommendationHistory(r);
            recommendationRepository.delete(r);
        }
    }

    public void addTargetedCustomer(String targetedCampaignList, byte[] targetedCustomerFileByte, String fileName,
            String createBy, Boolean isReadHeader)
            throws Exception, ServiceException {
        log.info("start - addTargetedCustomer : {}", targetedCampaignList);

        // TargetedCustomer targetedCustomer = new TargetedCustomer();
        UploadedFileHistoryRequest uploadedFileHistoryRequest = new UploadedFileHistoryRequest();

        String tempTargetedCustomerList = "";

        // TODO - get the value return from joget, now is put as default .xlsx, need
        // support .csv as well
        // String uploadedFileFormat =
        // "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        String fileFormat = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (fileFormat.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            log.info("addTargetedCustomer - file format is .xlsx");

            XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(targetedCustomerFileByte));
            XSSFSheet worksheet = workbook.getSheetAt(0);
            DataFormatter dataFormatter = new DataFormatter();
            XSSFRow tempRow = worksheet.getRow(1);
            Integer totalColumn = tempRow.getPhysicalNumberOfCells();

            int startRow = isReadHeader ? 1 : 1; // Choose starting row based on isReadHeader value
            int numberOfRow = isReadHeader ? worksheet.getPhysicalNumberOfRows()
                    : worksheet.getPhysicalNumberOfRows() + 1;

            for (int i = startRow; i < numberOfRow; i++) {

                XSSFRow row = worksheet.getRow(i);

                if (row.getCell(0) != null) {
                    totalColumn = row.getPhysicalNumberOfCells();
                    String cellValue = dataFormatter.formatCellValue(row.getCell(0)); // Format cell value as a string

                    // Check if the string starts with a single quote
                    if (cellValue.startsWith("'")) {
                        // Remove the single quote by creating a substring starting from index 1
                        cellValue = cellValue.substring(1);
                    }

                    if (tempTargetedCustomerList == "") {
                        tempTargetedCustomerList = cellValue.toString();
                    } else {
                        tempTargetedCustomerList = tempTargetedCustomerList + "," + cellValue.toString();
                    }

                }
            }

            uploadedFileHistoryRequest.setFileName(fileName);
            uploadedFileHistoryRequest.setCreateBy(createBy);
            uploadedFileHistoryRequest.setFileFormat("xlsx");
            uploadedFileHistoryRequest
                    .setDescription("This file history is from upload targeted customer interface in joget");
            uploadedFileHistoryRequest.setIsReadHeader(isReadHeader);
            uploadedFileHistoryRequest.setTotalSheet(workbook.getNumberOfSheets());
            uploadedFileHistoryRequest.setTotalColumn(totalColumn);
            workbook.close();
        } else {
            InputStream inputStream = new ByteArrayInputStream(targetedCustomerFileByte);
            InputStreamReader isr = new InputStreamReader(inputStream);
            String s = isr.getEncoding();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            String line;
            Boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                String[] cols = line.split(",");
                if (isFirstLine) {
                    isFirstLine = false;

                } else if (tempTargetedCustomerList == "") {
                    if (!cols[0].isEmpty()) {
                        tempTargetedCustomerList = cols[0].toString();
                    }
                } else {
                    if (!cols[0].isEmpty()) {
                        tempTargetedCustomerList = tempTargetedCustomerList + ","
                                + cols[0].toString();
                    }
                }
            }
        }

        try {
            if (tempTargetedCustomerList != "") {
                // targetedCustomer.setTargetedCustomerList(tempTargetedCustomerList);
                String formatTargetedCampaignList = targetedCampaignList.replace(";", ",");
                log.info("addTargetedCustomer - Total Targeted Campaign : {}",
                        StringUtils.countOccurrencesOf(formatTargetedCampaignList, ",") + 1);
                log.info("addTargetedCustomer - Targeted Campaign List : {}", formatTargetedCampaignList);
                log.info("addTargetedCustomer - Total Targeted Customer : {}",
                        StringUtils.countOccurrencesOf(tempTargetedCustomerList, ",") + 1);
                log.info("addTargetedCustomer - Targeted Customer List : {}", tempTargetedCustomerList);
                // targetedCustomer.setTargetedCampaignList(formatTargetedCampaignList);
                uploadedFileHistoryRequest.setTargetedCampaignList(formatTargetedCampaignList);
                // targetedCustomer.setTotalTargetedCustomer(
                // StringUtils.countOccurrencesOf(tempTargetedCustomerList, ",") + 1);
                uploadedFileHistoryRequest
                        .setTotalRow(StringUtils.countOccurrencesOf(tempTargetedCustomerList, ",") + 1);
                // targetedCustomer.setTotalTargetedCampaign(
                // StringUtils.countOccurrencesOf(formatTargetedCampaignList, ",") + 1);
                uploadedFileHistoryRequest
                        .setTotalTargetedCampaign(StringUtils.countOccurrencesOf(formatTargetedCampaignList, ",") + 1);
                // log.info("start - targetedCustomerRepository : {}", targetedCustomer);

                log.info("addTargetedCustomer - schedule trigger engagement for recommandation");

                String[] targetedCampaignElements = formatTargetedCampaignList.split(",");
                String[] targetedCustomerElements = tempTargetedCustomerList.split(",");

                List<String> targetedCampaignFixedLenghtList = Arrays.asList(targetedCampaignElements);
                List<String> targetedCustomerFixedLenghtList = Arrays.asList(targetedCustomerElements);

                ArrayList<String> targetedCampaignListOfString = new ArrayList<String>(targetedCampaignFixedLenghtList);
                ArrayList<String> targetedCustomerListOfString = new ArrayList<String>(targetedCustomerFixedLenghtList);

                log.info("addTargetedCustomer - targetedCampaignListOfString : {}", targetedCampaignListOfString);
                log.info("addTargetedCustomer - Size targetedCampaignListOfString : {}",
                        targetedCampaignListOfString.size());

                log.info("addTargetedCustomer - targetedCustomerListOfString : {}", targetedCustomerListOfString);
                log.info("addTargetedCustomer - Size targetedCustomerListOfString : {}",
                        targetedCustomerListOfString.size());

                Integer triggerScheduledCount = 0;

                for (int targetCustomerCount = 0; targetCustomerCount < targetedCustomerListOfString
                        .size(); targetCustomerCount++) {
                    log.info("addTargetedCustomer - target customer No. {} in file list, ID value : {}",
                            targetCustomerCount, targetedCustomerListOfString.get(targetCustomerCount));

                    for (int campaignCount = 0; campaignCount < targetedCampaignListOfString.size(); campaignCount++) {
                        CampaignStatus campaignStatus = triggerEngagementService
                                .checkCampaign(Integer.parseInt(targetedCampaignListOfString.get(campaignCount)));
                        Boolean campaignValid = true;
                        log.info("addTargetedCustomer : campaignStatus : {}", campaignStatus);
                        if (campaignStatus == CampaignStatus.VALID) {
                            campaignValid = true;
                        }

                        if (campaignValid == true) {
                            log.debug("addTargetedCustomer - index : {} | campaign id : {}", campaignCount,
                                    targetedCampaignListOfString.get(campaignCount));

                            Recommendation triggerScheduledRecommendation = new Recommendation();

                            CampaignDetailResponse campaignDetailResponse = getCampaignDetail(
                                    Integer.parseInt(targetedCampaignListOfString.get(campaignCount)));

                            Map<String, String> campaignProperties = campaignDetailResponse.getCampaignProperties();

                            // if (campaignDetailResponse != null) {
                            // Set Cif No
                            triggerScheduledRecommendation
                                    .setCifNo(targetedCustomerListOfString.get(targetCustomerCount));

                            // Set Campaign ID
                            triggerScheduledRecommendation
                                    .setCampaignID(Integer.parseInt(targetedCampaignListOfString.get(campaignCount)));

                            // Set Campaign Priority
                            triggerScheduledRecommendation
                                    .setCampaignPriority(campaignDetailResponse.getCampaignPriority());

                            // Set Engagement Mode
                            if (campaignProperties != null
                                    && campaignProperties.containsKey("engagementModeTemplateId")) {
                                String tempEngagementModeIdList = campaignProperties.get("engagementModeTemplateId");
                                String[] tempEngagementModeIdListElements = tempEngagementModeIdList.split(",");
                                List<String> tempEngagementModeIdListFixedLenghtList = Arrays
                                        .asList(tempEngagementModeIdListElements);

                                ArrayList<String> tempEngagementModeIdListOfString = new ArrayList<String>(
                                        tempEngagementModeIdListFixedLenghtList);

                                String engagementModeNameList = "";

                                for (int engagementModeCount = 0; engagementModeCount < tempEngagementModeIdListOfString
                                        .size(); engagementModeCount++) {
                                    if (engagementModeNameList == "") {
                                        engagementModeNameList = engagementModeRepository
                                                .getEngagementModeNameById(
                                                        Integer.parseInt(
                                                                tempEngagementModeIdListOfString
                                                                        .get(engagementModeCount)));
                                    } else {
                                        engagementModeNameList = engagementModeNameList
                                                + ","
                                                + engagementModeRepository
                                                        .getEngagementModeNameById(
                                                                Integer.parseInt(
                                                                        tempEngagementModeIdListOfString
                                                                                .get(engagementModeCount)));
                                    }
                                }
                                Set<String> engagementModeSet = new HashSet<>(
                                        Arrays.asList(engagementModeNameList.split(",")));
                                log.debug("addTargetedCustomer - setEngagementMode : {}", engagementModeSet);
                                triggerScheduledRecommendation.setEngagementMode(engagementModeSet);

                                // Set Trigger Status
                                if (engagementModeNameList.contains("PUSH")
                                        || engagementModeNameList.contains("BELL")
                                        || engagementModeNameList.contains("WHATSAPP")) {
                                    triggerScheduledRecommendation
                                            .setTriggerStatus(TriggerStatus.NEW);
                                } else {
                                    triggerScheduledRecommendation.setTriggerStatus(
                                            TriggerStatus.NON_TRIGGER);
                                }

                            } else {
                                triggerScheduledRecommendation.setEngagementMode(null);
                            }

                            // Set Personal Info Update Sequence
                            if (campaignProperties != null
                                    && campaignProperties.containsKey("personalInformationToUpdateId")) {
                                String tempPersonalInformationToUpdateTemplateIdList = campaignProperties
                                        .get("personalInformationToUpdateId");
                                String[] PersonalInformationToUpdateTemplateIdListElements = tempPersonalInformationToUpdateTemplateIdList
                                        .split(",");
                                List<String> tempPersonalInformationToUpdateTemplateIdListFixedLenghtList = Arrays
                                        .asList(PersonalInformationToUpdateTemplateIdListElements);

                                ArrayList<String> tempPersonalInformationToUpdateTemplateIdListOfString = new ArrayList<String>(
                                        tempPersonalInformationToUpdateTemplateIdListFixedLenghtList);

                                String personalInformationToUpdateTemplateNameList = "";
                                for (int personalInformationToUpdateTemplateCount = 0; personalInformationToUpdateTemplateCount < tempPersonalInformationToUpdateTemplateIdListOfString
                                        .size(); personalInformationToUpdateTemplateCount++) {

                                    if (personalInformationToUpdateTemplateNameList == "") {
                                        personalInformationToUpdateTemplateNameList = personalInfoTemplateRepository
                                                .getPersonalInfoTemplateNameById(
                                                        Integer.parseInt(
                                                                tempPersonalInformationToUpdateTemplateIdListOfString
                                                                        .get(personalInformationToUpdateTemplateCount)));
                                    } else {
                                        personalInformationToUpdateTemplateNameList = personalInformationToUpdateTemplateNameList
                                                + ","
                                                + personalInfoTemplateRepository.getPersonalInfoTemplateNameById(
                                                        Integer.parseInt(
                                                                tempPersonalInformationToUpdateTemplateIdListOfString
                                                                        .get(personalInformationToUpdateTemplateCount)));
                                    }
                                }
                                String[] personalInfoArray = personalInformationToUpdateTemplateNameList.split(",");
                                List<String> personalInfoList = Arrays.asList(personalInfoArray);
                                log.debug("addTargetedCustomer - setUpdateSequence : {}", personalInfoList);
                                triggerScheduledRecommendation.setUpdateSequence(personalInfoList);
                            }

                            // set High Risk
                            if (campaignProperties != null
                                    && campaignProperties.containsKey("highRiskPersonalInformationToUpdateId")) {
                                String tempHighRiskPersonalInformationToUpdateTemplateIdList = campaignProperties
                                        .get("highRiskPersonalInformationToUpdateId");

                                if (tempHighRiskPersonalInformationToUpdateTemplateIdList != null) {
                                    String[] HighRiskPersonalInformationToUpdateTemplateIdListElements = tempHighRiskPersonalInformationToUpdateTemplateIdList
                                            .split(",");

                                    List<String> tempHighRiskPersonalInformationToUpdateTemplateIdListFixedLenghtList = Arrays
                                            .asList(HighRiskPersonalInformationToUpdateTemplateIdListElements);

                                    ArrayList<String> tempHighRiskPersonalInformationToUpdateTemplateIdListOfString = new ArrayList<String>(
                                            tempHighRiskPersonalInformationToUpdateTemplateIdListFixedLenghtList);

                                    String highRiskPersonalInformationToUpdateTemplateNameList = "";
                                    for (int highRiskPersonalInformationToUpdateTemplateCount = 0; highRiskPersonalInformationToUpdateTemplateCount < tempHighRiskPersonalInformationToUpdateTemplateIdListOfString
                                            .size(); highRiskPersonalInformationToUpdateTemplateCount++) {

                                        if (highRiskPersonalInformationToUpdateTemplateNameList == "") {
                                            highRiskPersonalInformationToUpdateTemplateNameList = personalInfoTemplateRepository
                                                    .getPersonalInfoTemplateNameById(
                                                            Integer.parseInt(
                                                                    tempHighRiskPersonalInformationToUpdateTemplateIdListOfString
                                                                            .get(highRiskPersonalInformationToUpdateTemplateCount)));
                                        } else {
                                            highRiskPersonalInformationToUpdateTemplateNameList = highRiskPersonalInformationToUpdateTemplateNameList
                                                    + ","
                                                    + personalInfoTemplateRepository
                                                            .getPersonalInfoTemplateNameById(
                                                                    Integer.parseInt(
                                                                            tempHighRiskPersonalInformationToUpdateTemplateIdListOfString
                                                                                    .get(highRiskPersonalInformationToUpdateTemplateCount)));
                                        }
                                    }
                                    if (highRiskPersonalInformationToUpdateTemplateNameList != null) {
                                        String[] highRiskPersonalInfoArray = highRiskPersonalInformationToUpdateTemplateNameList
                                                .split(",");

                                        List<String> highRiskPersonalInfoList = Arrays
                                                .asList(highRiskPersonalInfoArray);

                                        triggerScheduledRecommendation.setHighRisk(highRiskPersonalInfoList);
                                        log.debug("addTargetedCustomer - setUpdateSequence : {}",
                                                highRiskPersonalInfoList);
                                    } else {
                                        triggerScheduledRecommendation.setHighRisk(null);
                                        log.debug("addTargetedCustomer - setUpdateSequence is null");
                                    }

                                    // Set<String> highRiskPersonalInfoSet = new HashSet<>(
                                    // Arrays.asList(highRiskPersonalInformationToUpdateTemplateNameList.split(",")));
                                    // log.info("Pass 2 : setUpdateSequence : {}", highRiskPersonalInfoSet);
                                    // triggerScheduledRecommendation.setHighRisk(highRiskPersonalInfoSet);
                                } else {
                                    log.info(
                                            "addTargetedCustomer - unable to find field highRiskPersonalInformationToUpdateId for campaign id : {}",
                                            targetedCampaignListOfString
                                                    .get(campaignCount));
                                }
                            }

                            // Set Message Template
                            Map<String, Integer> messageTemplateIdMap = new HashMap<>();
                            if (campaignProperties != null) {
                                if (campaignProperties.containsKey("pushNotificationTemplateId")) {
                                    messageTemplateIdMap.put("pushNotificationTemplateId",
                                            Integer.parseInt(campaignProperties
                                                    .get("pushNotificationTemplateId")));
                                }

                                if (campaignProperties.containsKey("inAppMessageTemplateId")) {
                                    messageTemplateIdMap.put("inAppMessageTemplateId",
                                            Integer.parseInt(campaignProperties
                                                    .get("inAppMessageTemplateId")));
                                }

                                if (campaignProperties.containsKey("actionCompleteMessageTemplateId")) {
                                    messageTemplateIdMap.put("actionCompleteMessageTemplateId",
                                            Integer.parseInt(campaignProperties
                                                    .get("actionCompleteMessageTemplateId")));
                                }

                                if (campaignProperties.containsKey("expiredMessageTemplateId")) {
                                    messageTemplateIdMap.put("expiredMessageTemplateId",
                                            Integer.parseInt(campaignProperties
                                                    .get("expiredMessageTemplateId")));
                                }
                                if (campaignProperties.containsKey("disabledMessageTemplateId")) {
                                    messageTemplateIdMap.put("disabledMessageTemplateId",
                                            Integer.parseInt(campaignProperties
                                                    .get("disabledMessageTemplateId")));
                                }
                                triggerScheduledRecommendation.setMessageTemplateId(messageTemplateIdMap);
                            }
                            log.debug("addTargetedCustomer : setMessageTemplateId {}", messageTemplateIdMap);

                            // Set pushNotificationSendDateTime
                            if (campaignProperties != null
                                    && campaignProperties.containsKey("pushNotificationSendDateTime")) {
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                                        "yyyy-MM-dd-HH.mm.ss.SSSSSS");

                                triggerScheduledRecommendation
                                        .setPushNotificationSendDateTime(simpleDateFormat.parse(campaignProperties
                                                .get("pushNotificationSendDateTime").toString()));
                            }

                            // Set Device ID and Platform
                            try {
                                CacheCustomerProfileSoleCCResponse cacheCustomerProfileSoleCCResponse = new CacheCustomerProfileSoleCCResponse();
                                ProfileRequest profileRequest = new ProfileRequest();
                                profileRequest.setIdType("cif_no");
                                profileRequest.setIdValue(targetedCustomerListOfString.get(targetCustomerCount));
                                profileRequest.setTableName("EVT_SOLE_CC_VIEW");
                                cacheCustomerProfileSoleCCResponse = checkCustomerProfile(profileRequest);

                                
                                // TriggerEngagementPushNotiRequest getDeviceIdAndPlatform = getDeviceIdAndPlatform(
                                //         targetedCustomerListOfString.get(targetCustomerCount));

                                TriggerEngagementPushNotiRequest deviceIdAndPlatform = new TriggerEngagementPushNotiRequest();
                                deviceIdAndPlatform.setDeviceId(cacheCustomerProfileSoleCCResponse.getMobileFirstPlatformId());
                                deviceIdAndPlatform.setDevicePlatform(cacheCustomerProfileSoleCCResponse.getDevicePlatform());
                                if (deviceIdAndPlatform.getDeviceId() != ""
                                        && deviceIdAndPlatform.getDeviceId() != null) {
                                    triggerScheduledRecommendation
                                            .setDeviceId(deviceIdAndPlatform.getDeviceId());
                                } else {
                                    triggerScheduledRecommendation.setDeviceId(
                                            targetedCustomerListOfString.get(targetCustomerCount));
                                    log.info(
                                            "getDeviceIdAndPlatform - DeviceUuid is null and set to cifNo for testing");
                                }

                                if (deviceIdAndPlatform.getDevicePlatform() != ""
                                        && deviceIdAndPlatform.getDevicePlatform() != null) {
                                    deviceIdAndPlatform.setDevicePlatform(
                                            deviceIdAndPlatform.getDevicePlatform());
                                } else {
                                    triggerScheduledRecommendation.setDevicePlatform("ANDROID");
                                    log.info(
                                            "getDeviceIdAndPlatform - DevicePlatform is null and set to ANDROID for testing");
                                }
                            } catch (Exception ex) {
                                triggerScheduledRecommendation
                                        .setDeviceId(targetedCustomerListOfString.get(targetCustomerCount));
                                log.info(
                                        "addTargetedCustomer - getDeviceIdAndPlatform exception, setDeviceUuid to default");

                                triggerScheduledRecommendation.setDevicePlatform("ANDROID");
                                log.info(
                                        "getDeviceIdAndPlatform - getDeviceIdAndPlatform exception, setDevicePlatform to default");

                                log.error("addTargetedCustomer - getDeviceIdAndPlatform exception : {}", ex);
                            }
                            // Set Campaign Name
                            triggerScheduledRecommendation
                                    .setCampaignName(campaignDetailResponse.getCampaignName());

                            // Set Description
                            triggerScheduledRecommendation
                                    .setCampaignDescription(campaignDetailResponse.getDescription());

                            // Set Start Date
                            triggerScheduledRecommendation
                                    .setCampaignStartDate(campaignDetailResponse.getStartDate());

                            // Set End Date
                            triggerScheduledRecommendation.setCampaignEndDate(campaignDetailResponse.getEndDate());

                            // Set Float Image Icon
                            if (campaignProperties.containsKey("floatIconImage")) {
                                triggerScheduledRecommendation.setFloatIconImage(
                                        campaignProperties.get("floatIconImage").toString());
                            }

                            // Set general campaign detail
                            triggerScheduledRecommendation.setIsCampaignUpdated(false);
                            triggerScheduledRecommendation.setIsIgnore(false);
                            triggerScheduledRecommendation.setIsTriggered(false);
                            Date currentDate = new Date();
                            long currentTimeInMillis = currentDate.getTime();
                            long eightHoursInMillis = 8 * 60 * 60 * 1000; // 8 hours in milliseconds
                            long malaysiaTimeInMillis = currentTimeInMillis + eightHoursInMillis;
                            Date malaysiaTime = new Date(malaysiaTimeInMillis); // to store as malaysia time in
                                                                                // cassandra since it will make the date
                                                                                // be UTC

                            triggerScheduledRecommendation.setCreatedOn(malaysiaTime);
                            triggerScheduledRecommendation.setUpdatedOn(malaysiaTime);
                            recommendationRepository.save(triggerScheduledRecommendation);

                            triggerScheduledCount++;
                            // } else {
                            // log.info("addTargetedCustomer - campaignDetailResponse for campaign id : {}
                            // is null", campaignCount);
                            // throw new
                            // ServiceException(ApiResponse.HTTP_RESPONSE_OK,messageSource.getMessage("create.target.customer.fail.add",
                            // null, Locale.getDefault()));
                            // }

                        } else {
                            log.info("addTargetedCustomer - validation result for campaign id {} is not valid",
                                    targetedCampaignListOfString.get(campaignCount));
                        }
                    }
                }
                uploadedFileHistoryRequest.setTotalTriggerRequest(triggerScheduledCount);

                log.info("start - addTargetedCustomer :  addUploadedFileHistoryLog : {}", uploadedFileHistoryRequest);
                addUploadedFileHistoryLog(uploadedFileHistoryRequest);

                log.info("end - addTargetedCustomer :  total of {} trigger scheduled", triggerScheduledCount);

            } else {
                log.info("end - addTargetedCustomer : there is no data in the file");
                throw new ServiceException(ApiResponse.HTTP_RESPONSE_OK,
                        messageSource.getMessage("create.target.customer.fail.add", null, Locale.getDefault()));
            }
        } catch (Exception ex) {
            log.info("addTargetedCustomer - exception : {}", ex);
            throw new ServiceException(ApiResponse.HTTP_RESPONSE_BAD_REQUEST,
                    messageSource.getMessage("create.target.customer.bad.request", null, Locale.getDefault()));
        }

    }

    public static RestTemplate createRestTemplate() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s)
                            throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s)
                            throws CertificateException {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier((s, sslSession) -> true)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        requestFactory.setConnectTimeout(60000);
        requestFactory.setReadTimeout(60000);

        return new RestTemplate(requestFactory);
    }

    // @Async
    // public TriggerEngagementPushNotiRequest getDeviceIdAndPlatform(String cifNo) throws Exception, ServiceException {
    //     log.info("Start - getDeviceIdAndPlatform for cifNo : {}", cifNo);
    //     HttpHeaders headers = new HttpHeaders();
    //     headers.setContentType(MediaType.APPLICATION_JSON);

    //     Map<String, Object> body = new HashMap<String, Object>();
    //     HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
    //     RestTemplate restTemplate = createRestTemplate();

    //     ResponseEntity<Map> response = restTemplate.exchange(
    //             PUSH_DEVICE_ID_END_POINT_URL + cifNo, HttpMethod.GET, entity, Map.class);

    //     if (response.getStatusCode().value() != 200) {
    //         log.error("getDeviceIdAndPlatform failed - statuscode {}, failed : {}", response.getStatusCode(), response);
    //         throw new ServiceException(ApiResponse.HTTP_RESPONSE_BAD_REQUEST, response.toString());
    //     } else {
    //         log.info("getDeviceIdAndPlatform - statuscode {}, body {}", response.getStatusCode(), response.getBody());

    //         ObjectMapper objectMapper = new ObjectMapper();
    //         log.info("response.getBody() : {}", response.getBody());
    //         String jsonStr = objectMapper.writeValueAsString(response.getBody());
    //         log.info("jsonStr : {}", jsonStr);
    //         JsonNode jsonNode = objectMapper.readTree(jsonStr);
    //         log.info("jsonNode : {}", jsonNode);

    //         String deviceId = "";
    //         if (jsonNode.has("deviceUuid")) {
    //             deviceId = jsonNode.get("deviceUuid").asText();
    //         }

    //         String devicePlatform = "";
    //         if (jsonNode.has("devicePlatform")) {
    //             devicePlatform = jsonNode.get("devicePlatform").asText();
    //         }

    //         TriggerEngagementPushNotiRequest pushNotificationRequest = new TriggerEngagementPushNotiRequest();
    //         pushNotificationRequest.setDeviceId(deviceId);
    //         pushNotificationRequest.setDevicePlatform(devicePlatform);

    //         return pushNotificationRequest;
    //     }
    // }

    @Async
    public CampaignDetailResponse getCampaignDetail(Integer id) throws Exception {
        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(15000);
            factory.setReadTimeout(15000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            RestTemplate restTemplate = new RestTemplate(factory);

            Object response;

            response = restTemplate.getForObject(GET_CAMPAIGN_DETAIL_END_POINT_URL + id, Object.class);

            log.info("getCampaignDetail : {}", response);
            CampaignDetailResponse campaignDetailResponse = new CampaignDetailResponse();

            if (response instanceof Map) {
                Map<String, Object> responseMap = (Map<String, Object>) response;
                if (responseMap.containsKey("campaignId")) {
                    campaignDetailResponse.setCampaignId(Integer.parseInt(responseMap.get("campaignId").toString()));
                } else {
                    log.info("getCampaignDetail - campaignId field does not exist.");
                }

                if (responseMap.containsKey("campaignPriority")) {
                    if (responseMap.get("campaignPriority") != null) {
                        campaignDetailResponse
                                .setCampaignPriority(Integer.parseInt(responseMap.get("campaignPriority").toString()));
                    } else {
                        campaignDetailResponse.setCampaignPriority(null);
                    }
                } else {
                    log.info("getCampaignDetail - campaignPriority field does not exist.");
                }

                if (responseMap.containsKey("campaignName")) {
                    if (responseMap.get("campaignName") != null) {
                        campaignDetailResponse
                                .setCampaignName(responseMap.get("campaignName").toString());
                    } else {
                        campaignDetailResponse.setCampaignName(null);
                    }
                } else {
                    log.info("getCampaignDetail - campaignName field does not exist.");
                }

                if (responseMap.containsKey("description")) {
                    if (responseMap.get("description") != null) {
                        campaignDetailResponse
                                .setDescription(responseMap.get("description").toString());
                    } else {
                        campaignDetailResponse.setDescription(null);
                    }
                } else {
                    log.info("getCampaignDetail - description field does not exist.");
                }

                if (responseMap.containsKey("startDate")) {
                    if (responseMap.get("startDate") != null) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                        campaignDetailResponse
                                .setStartDate(dateFormat.parse(responseMap.get("startDate").toString()));
                    } else {
                        campaignDetailResponse.setStartDate(null);
                    }
                } else {
                    log.info("getCampaignDetail - startDate field does not exist.");
                }

                if (responseMap.containsKey("endDate")) {
                    if (responseMap.get("endDate") != null) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                        campaignDetailResponse
                                .setEndDate(dateFormat.parse(responseMap.get("endDate").toString()));
                    } else {
                        campaignDetailResponse.setEndDate(null);
                    }
                } else {
                    log.info("getCampaignDetail - endDate field does not exist.");
                }

                if (responseMap.containsKey("campaignProperties")) {
                    campaignDetailResponse.setCampaignProperties(
                            (Map<String, String>) responseMap.getOrDefault("campaignProperties", new HashMap<>()));
                } else {
                    log.info("getCampaignDetail - campaignProperties field does not exist.");
                }
                return campaignDetailResponse;
            } else {
                log.info("getCampaignDetail - Invalid response format.");
                return null;
            }

        } catch (Exception ex) {
            log.error("getCampaignDetail - campaign id : {} | exception : {}", id, ex);
            return null;
        }
    }

    public Recommendation updateCustCampaignStatus(UpdateCampaignCustomerStatusRequest data)
            throws ServiceException {
        try {
            Recommendation recommendationList = new Recommendation();
            if ("NRIC".equalsIgnoreCase(data.getIdType()) && data.getEngagementMode() != null && data.getCifNo() != null && data.getCampaignId() != null) {
                recommendationList = recommendationRepository.getByNricAndCampaignIdAndEngagementMode(data.getCifNo(), Integer.parseInt(data.getCampaignId()), data.getEngagementMode());
            }else{
                recommendationList = recommendationRepository.getByCifNoAndCampaignId(data.getCifNo(), Integer.parseInt(data.getCampaignId()));
            }

            if (recommendationList != null) {
                if (data.getIsCampaignUpdated()) {
                    recommendationList.setIsCampaignUpdated(data.getIsCampaignUpdated());
                }
                if (data.getIsIgnore()) {
                    recommendationList.setIsIgnore(data.getIsIgnore());
                }
                recommendationRepository.save(recommendationList);
            } else {
                log.info("updateCustCampaignStatus - no result found for cifNo : {}", data.getCifNo());
                throw new ServiceException(ApiResponse.HTTP_RESPONSE_OK, messageSource
                        .getMessage("get.cust.campaign.status.null.value", null, Locale.getDefault()));
            }

            return recommendationList;
        } catch (Exception ex) {
            log.error("updateCustCampaignStatus - detail : {} | exception : {}", data, ex);
            return null;
        }
    }

    // public CustomerProfileResponse checkCustomerProfile(String cifNo) {
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

    //         ResponseEntity<CustomerProfileResponse> response = restTemplate.getForEntity(
    //                 CHECK_CUSTOMER_PROFILE_URL + cifNo,
    //                 CustomerProfileResponse.class);

    //         log.info("checkCustomerProfile : {}", response.getBody());

    //         return response.getBody();

    //     } catch (NullPointerException ex) {
    //         // Handle the NullPointerException gracefully
    //         ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
    //                 "NullPointerException occurred: " + ex.getMessage(), null);

    //         log.info("checkCustomerProfile : exception {} | response : {}", ex, apiResponse);
    //         // Return an appropriate response or take necessary action
    //         return new CustomerProfileResponse(); // Return an empty CustomerProfileResponse object, or handle it based
    //                                               // on your use case
    //     } catch (Exception ex) {
    //         // Handle other exceptions
    //         ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
    //                 ex.getLocalizedMessage(), null);

    //         log.info("checkCustomerProfile : exception {} | response : {}", ex, apiResponse);
    //         // Return an appropriate response or take necessary action
    //         return null; // Return null or handle it based on your use case
    //     }
    // }

    public CacheCustomerProfileSoleCCResponse checkCustomerProfile(ProfileRequest profileRequest) {
        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(15000);
            factory.setReadTimeout(15000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<ProfileRequest> entity = new HttpEntity<>(profileRequest);
            RestTemplate restTemplate = new RestTemplate(factory);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(DICE_CACHE_GET_PROFILE_ENDPOINT_URL,
                    HttpMethod.POST, entity, new ParameterizedTypeReference<Map<String, Object>>() {});
            CacheCustomerProfileSoleCCResponse cacheCustomerProfileSoleCC = new CacheCustomerProfileSoleCCResponse();
            if (response.getBody() != null) {
				log.info("show data : {}", response.getBody().get("data"));

				Map<String, String> dataMap = (Map<String, String>) response.getBody().get("data");

				if (dataMap != null) {
                    
					// customerProfileResponse.setCifNo(dataMap.containsKey("cif_no") ? dataMap.get("cif_no") : null);
                    cacheCustomerProfileSoleCC = mapDataToCustomerProfileResponse(dataMap);
				}
                else{
                    return new CacheCustomerProfileSoleCCResponse();
                }
            } else{
                return new CacheCustomerProfileSoleCCResponse();
            }

            log.info("profileRequest - request: {} | response : {}", entity, response);

            return cacheCustomerProfileSoleCC;

        } catch (NullPointerException ex) {
            // Handle the NullPointerException gracefully
            ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
                    "NullPointerException occurred: " + ex.getMessage(), null);

            log.info("checkCustomerProfile : exception {} | response : {}", ex, apiResponse);
            // Return an appropriate response or take necessary action
            return new CacheCustomerProfileSoleCCResponse(); // Return an empty CustomerProfileResponse object, or handle it based
                                                  // on your use case
        } catch (Exception ex) {
            // Handle other exceptions
            ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
                    ex.getLocalizedMessage(), null);

            log.info("checkCustomerProfile : exception {} | response : {}", ex, apiResponse);
            // Return an appropriate response or take necessary action
            return null; // Return null or handle it based on your use case
        }
    }

    public CacheCustomerProfileSoleCCResponse mapDataToCustomerProfileResponse(Map<String, String> dataMap) {
        CacheCustomerProfileSoleCCResponse cacheCustomerProfileSoleCC = new CacheCustomerProfileSoleCCResponse();

        for (Field field : CacheCustomerProfileSoleCCResponse.class.getDeclaredFields()) {
            String jsonPropertyName = getJsonPropertyName(field);
            if (jsonPropertyName != null && dataMap.containsKey(jsonPropertyName)) {
                try {
                    field.setAccessible(true);
                    field.set(cacheCustomerProfileSoleCC, dataMap.get(jsonPropertyName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace(); // Handle exception accordingly
                }
            } else {
                // Set the field to null if the key doesn't exist in the dataMap or there's no mapping
                try {
                    field.setAccessible(true);
                    field.set(cacheCustomerProfileSoleCC, null);
                } catch (IllegalAccessException e) {
                    e.printStackTrace(); // Handle exception accordingly
                }
            }
        }

        return cacheCustomerProfileSoleCC;
    }

    private String getJsonPropertyName(Field field) {
        JsonProperty jsonPropertyAnnotation = field.getAnnotation(JsonProperty.class);
        if (jsonPropertyAnnotation != null) {
            return jsonPropertyAnnotation.value();
        }
        // If @JsonProperty is not present, return null
        return null;
    }

        public Boolean isExistingVccCustomer(ProfileRequest profileRequest) {
        try {
            Boolean isExistingVccCustomer = false;
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(15000);
            factory.setReadTimeout(15000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<ProfileRequest> entity = new HttpEntity<>(profileRequest);
            RestTemplate restTemplate = new RestTemplate(factory);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(DICE_CACHE_GET_PROFILE_ENDPOINT_URL,
                    HttpMethod.POST, entity, new ParameterizedTypeReference<Map<String, Object>>() {});
            CacheCustomerProfileSoleCCResponse cacheCustomerProfileSoleCC = new CacheCustomerProfileSoleCCResponse();
            if (response.getBody() != null) {
				log.info("isExistingVccCustomer - result : {}", response.getBody().get("data"));

				Map<String, String> dataMap = (Map<String, String>) response.getBody().get("data");

				if (dataMap != null) {
                    isExistingVccCustomer = true;
                    log.info("isExistingVccCustomer - {} for request : {}", isExistingVccCustomer, profileRequest);
				}
                else{
                    return isExistingVccCustomer;
                }
            } else{
                return isExistingVccCustomer;
            }

            log.info("isExistingVccCustomer - request: {} | response : {}", entity, response);

            return isExistingVccCustomer;

        } catch (NullPointerException ex) {
            // Handle the NullPointerException gracefully
            ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
                    "NullPointerException occurred: " + ex.getMessage(), null);

            log.info("checkCustomerProfile : exception {} | response : {}", ex, apiResponse);
            // Return an appropriate response or take necessary action
            return false; // Return an empty CustomerProfileResponse object, or handle it based
                                                  // on your use case
        } catch (Exception ex) {
            // Handle other exceptions
            ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
                    ex.getLocalizedMessage(), null);

            log.info("checkCustomerProfile : exception {} | response : {}", ex, apiResponse);
            // Return an appropriate response or take necessary action
            return null; // Return null or handle it based on your use case
        }
    }

    public void addUploadedFileHistoryLog(UploadedFileHistoryRequest uploadedFileHistoryRequest) {
        log.info("Start - addUploadedFileHistoryLog");
        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(15000);
            factory.setReadTimeout(15000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<UploadedFileHistoryRequest> entity = new HttpEntity<>(uploadedFileHistoryRequest);
            RestTemplate restTemplate = new RestTemplate(factory);
            ResponseEntity<Map> response = restTemplate.exchange(ADD_UPLOADED_FILE_HISTORY_LOG_END_POINT_URL,
                    HttpMethod.POST, entity, Map.class);

            log.info("addUploadedFileHistoryLog - request: {} | response : {}", entity, response);

        } catch (Exception ex) {
            log.error("addUploadedFileHistoryLog - Exception: {}", ex);
        }
    }

    public ResponseEntity<Map<String, Object>> getProfileById(ProfileRequest profileRequest) {
        log.info("Start - addUploadedFileHistoryLog");
        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(15000);
            factory.setReadTimeout(15000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<ProfileRequest> entity = new HttpEntity<>(profileRequest);
            RestTemplate restTemplate = new RestTemplate(factory);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(DICE_CACHE_GET_PROFILE_ENDPOINT_URL,
                    HttpMethod.POST, entity, new ParameterizedTypeReference<Map<String, Object>>() {});

            log.info("profileRequest - request: {} | response : {}", entity, response);

            return response;

        } catch (Exception ex) {
            log.error("profileRequest - Exception: {}", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }



}