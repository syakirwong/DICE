package com.alliance.diceruleengine.service;

import java.math.BigDecimal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.kafka.common.security.auth.Login;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.alliance.diceruleengine.configurer.DroolsConfig.CustomKieContainer;
import com.alliance.diceruleengine.constant.ApiResponse;
import com.alliance.diceruleengine.constant.DataField.Status;
import com.alliance.diceruleengine.exception.ServiceException;
import com.alliance.diceruleengine.model.Campaign;
import com.alliance.diceruleengine.model.Customer;
import com.alliance.diceruleengine.model.CustomerProfile;
import com.alliance.diceruleengine.model.CustomerSegmentationTemplate;
import com.alliance.diceruleengine.model.DiceActionTemplate;
import com.alliance.diceruleengine.model.EngagementModeTemplate;
import com.alliance.diceruleengine.model.ReferralCode;
import com.alliance.diceruleengine.model.Rule;
import com.alliance.diceruleengine.repository.RuleRepository;
import com.alliance.diceruleengine.request.CreateRecommendationRequest;
import com.alliance.diceruleengine.request.DeleteExistingRecommendationRequest;
import com.alliance.diceruleengine.request.ProcessCampaignRequest;
import com.alliance.diceruleengine.request.ProfileRequest;
import com.alliance.diceruleengine.request.TestRuleProcessRequest;
import com.alliance.diceruleengine.utility.DateUtil;
import com.alliance.diceruleengine.response.CheckCampaignResponse;
import com.alliance.diceruleengine.response.CustomerProfileResponse;
import com.alliance.diceruleengine.response.ProcessCampaignResponse;
import com.alliance.diceruleengine.utility.RuleUtil;
import com.alliance.diceruleengine.utility.StringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.db2.cmx.internal.json4j.JSONObject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RuleService {

    @Autowired
    RuleRepository ruleRepository;

    @Autowired
    private CustomKieContainer customKieContainer;

    @Autowired
    private CustomerSegmentationTemplateService customerSegmentationTemplateService;

    @Autowired
    private ReferralCodeService referralCodeService;

    @Autowired
    private EngagementModeTemplateService engagementModeTemplateService;

    @Autowired
    private DiceActionTemplateService diceActionTemplateService;

    @Value("${test.process.recommendation.endpointUrl}")
    private String TEST_PROCESS_RECOMMENDATION_ENDPOINT_URL;

    @Value("${create.recommendation.endpointUrl}")
    private String CREATE_RECOMMENDATION_ENDPOINT_URL;

    @Value("${get.campaign.endpointUrl}")
    private String GET_CAMPAIGN_ENDPOINT_URL;

    @Value("${get.campaign.by.name.endpointUrl}")
    private String GET_CAMPAIGN_BY_NAME_ENDPOINT_URL;

    @Value("${get.campaign.by.properties.and.status.endpointUrl}")
    private String GET_CAMPAIGN_BY_PROPERTIES_AND_STATUS_ENDPOINT_URL;

    @Value("${check.customer.profile.endpointUrl}")
    private String CHECK_CUSTOMER_PROFILE_URL;

    @Value("${get.customer.profile.via.cache.endpointUrl}")
    private String GET_CUSTOMER_PROFILE_VIA_CACHE_URL;

    @Value("${saveplus.referral.campaign.name}")
    private String SAVEPLUS_REFERRAL_CAMPAIGN_NAME;

    @Value("${solocc.campaign.name}")
    private String SOLOCC_CAMPAIGN_NAME;

    @Value("${ploan.campaign.name}")
    private String PLOAN_CAMPAIGN_NAME;

    @Value("${delete.existing.recommendation.endpointUrl}")
    private String DELETE_EXISTING_RECOMMENDATION_ENDPOINT_URL;

    @Autowired
    private StringUtil stringUtil;

    public List<Rule> getAllRule() {
        return ruleRepository.findAll();
    }

    public Rule createRule(String ruleName, Set<Integer> ruleTemplateId) {
        Integer ruleId = (ruleRepository
                .getMaxRuleId() != null)
                        ? ruleRepository.getMaxRuleId() + 1
                        : 1;
        Rule newRule = new Rule();
        newRule.setRuleId(ruleId);
        newRule.setRuleName(ruleName);
        newRule.setRuleTemplateId(ruleTemplateId);
        return ruleRepository.save(newRule);
    }

    public CreateRecommendationRequest testProcessRule(String uuidType, String uuid) throws ServiceException {
        Customer customer = new Customer();
        Object response = new Object();
        CreateRecommendationRequest request = new CreateRecommendationRequest();

        if (uuidType.equals("CIF_NO")) {
            // CustomerProfileResponse customerProfile = checkCustomerProfile(uuid);
            CustomerProfileResponse customerProfile = checkCustomerProfileViaCache(uuid);

            if (customerProfile != null) {
                if (customerProfile.getCifNo() != null) {
                    customer.setCustomerCifNo(customerProfile.getCifNo());
                }
                if (customerProfile.getCustomerName() != null) {
                    customer.setCustomerName(customerProfile.getCustomerName());
                }
                if (customerProfile.getGender() != null) {
                    customer.setCustomerGender(customerProfile.getGender());
                }
                // if (customerProfile.getDob() != null) {
                // customer.setCustomerAge(RuleUtil.calculateAge(customerProfile.getDob()));
                // }

                if (customerProfile.getPackageId() != null) {
                    if (customerProfile.getPackageId().equals("3")) {
                        customer.setCustomerType("SOLO_CC");
                    } else {
                        customer.setCustomerType("GENERIC");
                    }
                }
            }
        }

        if (uuidType.equals("EFORM_UUID")) {
            customer.setCustomerCifNo(uuid);
            customer.setCustomerType("GENERIC");
        }

        // Run customer segment check, return customerSegmentIds
        KieContainer kieContainer1 = customKieContainer.getKieContainer1();
        KieSession kieSession = kieContainer1.newKieSession();
        loadAllCustomerSegmentTemplateIntoDroolsMemory(kieSession);
        Set<Integer> customerSegmentId = new HashSet<>();
        kieSession.setGlobal("customerSegmentId", customerSegmentId);

        kieSession.insert(customer);
        kieSession.fireAllRules();
        kieSession.dispose();

        // log.info("--- {} ---", customer);
        String customerSegmentTemplateId = customer.getCustomerSegmentId() != null
                ? customer.getCustomerSegmentId().stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(","))
                : null;

        // log.info("--from rule service-- customerSegmentTemplateId: {}",
        // customerSegmentTemplateId);

        // Run cross check campaign with customersegment, return campaignIds
        try {
            KieContainer kieContainer2 = customKieContainer.getKieContainer2();
            KieSession campaignKieSession = kieContainer2.newKieSession();
            loadCampaignListIntoDroolsMemory(campaignKieSession);
            campaignKieSession.insert(customerSegmentTemplateId);
            campaignKieSession.insert(customer);
            campaignKieSession.setGlobal("savePlusReferralCampaignName", SAVEPLUS_REFERRAL_CAMPAIGN_NAME);
            campaignKieSession.setGlobal("soloCcCampaignName", SOLOCC_CAMPAIGN_NAME);
            campaignKieSession.setGlobal("ploanCampaignName", PLOAN_CAMPAIGN_NAME);
            campaignKieSession.fireAllRules();
            campaignKieSession.dispose();
        } catch (Exception e) {
            e.printStackTrace();
            // Handle the exception appropriately
        }

        if (customer != null && !customer.isEmpty()) {
            String customerCampaignName = customer.getCampaignName() != null
                    ? customer.getCampaignName().stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(","))
                    : null;
            // log.info("--from rule service-- customerCampaignName: {}",
            // customerCampaignName);

            // Run campaign check return CreateRecommendationRequest
            // Query the campaign based on the campaignId
            // Check if campaign is active or not
            // Check the diceActionId
            // if referral generate the referral code
            if (customerCampaignName != null) {
                // log.info("=========================================================");
                String[] values = customerCampaignName.split(",");

                for (String value : values) {

                    // log.info("---start recommendation process---");
                    // log.info("---calling create recommendation api---");
                    log.info("processRule - createRecommendationRequest - value: {}, uuidType : {}, uuid : {}", value,
                            uuidType, uuid);

                    if (!uuid.equals(null) && !uuid.trim().isEmpty()
                            && customer.getCustomerCifNo() != null) {
                        request = createRecommendationRequest(value, uuidType, uuid);
                    }
                    log.info("processRule - RECOMMENDATION REQUEST created, request : {}", request);

                    if (request != null && request.getCifNo() != null) {
                        try {
                            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                            factory.setConnectTimeout(15000);
                            factory.setReadTimeout(15000);

                            HttpHeaders headers = new HttpHeaders();
                            headers.setContentType(MediaType.APPLICATION_JSON);

                            RestTemplate restTemplate = new RestTemplate(factory);

                            response = restTemplate.postForObject(CREATE_RECOMMENDATION_ENDPOINT_URL,
                                    request,
                                    Object.class);

                            log.info("processRule - createRecommendation uuidType : {}, uuid : {} , response : {}",
                                    uuidType, uuid, response);

                        } catch (Exception ex) {

                            ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR,
                                    false,
                                    ex.getLocalizedMessage(), null);

                            log.error("processRule : exception {} | response : {}", ex,
                                    apiResponse);
                        }
                    } else {
                        try {
                            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                            factory.setConnectTimeout(15000);
                            factory.setReadTimeout(15000);

                            HttpHeaders headers = new HttpHeaders();
                            headers.setContentType(MediaType.APPLICATION_JSON);
                            RestTemplate restTemplate = new RestTemplate(factory);

                            ResponseEntity<Campaign> r = restTemplate.getForEntity(
                                    GET_CAMPAIGN_BY_NAME_ENDPOINT_URL
                                            + "?campaignName=" + value,
                                    Campaign.class);

                            Campaign campaign = r.getBody();

                            Integer campaignId = null;

                            campaignId = campaign.getCampaignId();

                            DeleteExistingRecommendationRequest deleteExistingRecommendationRequest = new DeleteExistingRecommendationRequest();
                            deleteExistingRecommendationRequest.setCampaignId(campaignId);
                            deleteExistingRecommendationRequest.setCifNo(uuid);

                            HttpEntity<DeleteExistingRecommendationRequest> entity = new HttpEntity<>(
                                    deleteExistingRecommendationRequest, headers);

                            ResponseEntity<Object> deleteResponse = restTemplate.exchange(
                                    DELETE_EXISTING_RECOMMENDATION_ENDPOINT_URL,
                                    HttpMethod.DELETE,
                                    entity,
                                    Object.class);

                            // response =
                            // restTemplate.postForObject(DELETE_EXISTING_RECOMMENDATION_ENDPOINT_URL,
                            // deleteExistingRecommendationRequest,
                            // Object.class);

                            log.info(
                                    "processRule - deleteExistingRecommendation, uuidType : {}, uuid : {}, Response : {}",
                                    uuidType, uuid, deleteResponse);

                        } catch (Exception ex) {

                            ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR,
                                    false,
                                    ex.getLocalizedMessage(), null);

                            log.error("processRule - deleteExistingRecommendation : exception {} | response : {}", ex,
                                    apiResponse);
                        }
                    }
                }

            }
        }
        return request;

    }

    public void loadAllCustomerSegmentTemplateIntoDroolsMemory(KieSession kieSession) {
        List<CustomerSegmentationTemplate> customerSegmentationTemplateList = customerSegmentationTemplateService
                .getAllCustomerSegmentationTemplate();
        // log.info("--- customerSegmentationList: {} ---",
        // customerSegmentationTemplateList);
        kieSession.insert(customerSegmentationTemplateList);
    }

    public void loadCampaignListIntoDroolsMemory(KieSession kieSession) {

        List<Object> campaignList = new ArrayList<Object>();
        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(15000);
            factory.setReadTimeout(15000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            RestTemplate restTemplate = new RestTemplate(factory);

            ResponseEntity<Object[]> response = restTemplate.getForEntity(GET_CAMPAIGN_ENDPOINT_URL,
                    Object[].class);
            Object[] campaignArray = response.getBody();

            campaignList = Arrays.asList(campaignArray);

            // log.info("getCampaignList : {}", campaignList);

        } catch (Exception ex) {

            ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
                    ex.getLocalizedMessage(), null);

            log.info("getCampaignList : exception {} | response : {}", ex, apiResponse);
        }

        // log.info("--- campaignList: {} ---", campaignList);
        // kieSession.insert(campaignList);
        kieSession.setGlobal("campaignList", campaignList);

    }

    public CreateRecommendationRequest createRecommendationRequest(String campaignName, String uuidType, String uuid)
            throws ServiceException {
        // Query campaign via campaignName
        CreateRecommendationRequest createRecommendationRequest = new CreateRecommendationRequest();
        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(15000);
            factory.setReadTimeout(15000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            RestTemplate restTemplate = new RestTemplate(factory);

            ResponseEntity<Campaign> response = restTemplate.getForEntity(
                    GET_CAMPAIGN_BY_NAME_ENDPOINT_URL
                            + "?campaignName=" + campaignName,
                    Campaign.class);

            Campaign campaign = response.getBody();

            if (campaign.getCampaignStatus() != Status.ACTIVE) {
                return null;
            }

            Integer campaignId = null;

            campaignId = campaign.getCampaignId();

            Date startDate = null;
            Date endDate = null;

            startDate = campaign.getStartDate();
            endDate = campaign.getEndDate();

            if (startDate != null && endDate != null) {
                Date now = new Date();
                // log.info("now: {}", now);
                // log.info("startdate: {}, enddate: {}", startDate, endDate);
                if (now.after(endDate)) {
                    return null;
                }
            }

            if (uuid.trim().isEmpty()) {
                return null;
            }

            campaign.getCampaignProperties();
            createRecommendationRequest.setCampaignID(campaign.getCampaignId());
            createRecommendationRequest.setCampaignPriority(campaign.getCampaignPriority());
            createRecommendationRequest.setCampaignName(campaign.getCampaignName());
            createRecommendationRequest.setCampaignDescription(campaign.getDescription());
            createRecommendationRequest.setCampaignStartDate(campaign.getStartDate());
            createRecommendationRequest.setCampaignEndDate(campaign.getEndDate());
            createRecommendationRequest.setDiceActionTemplateId(campaign.getDiceActionTemplateId());
            createRecommendationRequest.setFloatIconImage(campaign.getCampaignProperties().get("floatIconImage"));
            createRecommendationRequest.setTriggerTypeId(1);

            Set<Integer> customerSegmentId = new HashSet<Integer>();
            String[] customerSegmentTemplateId = campaign.getCampaignProperties().get("customerSegmentId").split(",");
            for (String id : customerSegmentTemplateId) {
                customerSegmentId.add(Integer.parseInt(id));
            }
            createRecommendationRequest.setCustomerSegmentationId(customerSegmentId);

            Set<String> engagementMode = new HashSet<>();
            String[] engagementModeId = campaign.getCampaignProperties().get("engagementModeTemplateId").split(",");
            for (String e : engagementModeId) {
                Optional<EngagementModeTemplate> template = engagementModeTemplateService
                        .getTemplate(Integer.parseInt(e));
                engagementMode.add(template.get().getEngagementModeName());
            }
            createRecommendationRequest.setEngagementModeId(engagementMode);

            if (campaign.getDiceActionTemplateId() != null) {
                for (Integer id : campaign.getDiceActionTemplateId()) {
                    Optional<DiceActionTemplate> template = diceActionTemplateService
                            .getTemplate(id);
                    if (template.get().getDiceActionName().equals("REFERRAL")) {
                        // BigDecimal reward = new BigDecimal(30);
                        ReferralCode existingReferralCode = referralCodeService
                                .getExistingCodeBasedOnCampaignIdAndCifNo(Integer.toString(campaignId), uuid);
                        if (existingReferralCode != null) {
                            createRecommendationRequest.setReferralCode(existingReferralCode.getCodeValue());
                        } else {
                            String referralCode = stringUtil.generateRandomString(6, true, true);
                            if (referralCode != null) {
                                referralCodeService.createReferralCode(referralCode, campaignId, uuid);
                                createRecommendationRequest.setReferralCode(referralCode);
                            } else {
                                log.error(
                                        "createRecommendationRequest - the referralCode is null when process these request : campaignName = {}, uuidType = {}, uuid = {}",
                                        campaignName, uuidType, uuid);
                                throw new ServiceException(ApiResponse.HTTP_RESPONSE_BAD_REQUEST,
                                        "Referral Code is null");
                            }
                        }

                        // createRecommendationRequest.setPermanentLinkType("INAPPMSG");
                        // createRecommendationRequest.setPermanentIconImg("2");
                        // createRecommendationRequest.setPermanentTitleText("Invite a friend");
                        // createRecommendationRequest.setRewardAmount(reward);
                        // createRecommendationRequest.setRewardType("cash");
                    }
                }

            }

            Map<String, Integer> messageTemplateIdMap = new HashMap<>();
            if (campaign.getCampaignProperties().containsKey("pushNotificationTemplateId")) {
                messageTemplateIdMap.put("pushNotificationTemplateId",
                        Integer.parseInt(campaign.getCampaignProperties().get("pushNotificationTemplateId")));
            }
            if (campaign.getCampaignProperties().containsKey("inAppMessageTemplateId")) {
                messageTemplateIdMap.put("inAppMessageTemplateId",
                        Integer.parseInt(campaign.getCampaignProperties().get("inAppMessageTemplateId")));
            }

            createRecommendationRequest.setMessageTemplateId(messageTemplateIdMap);

            createRecommendationRequest.setCifNo(uuid);
            createRecommendationRequest.setDeviceId(null);
            createRecommendationRequest.setIsIgnore(false);
            createRecommendationRequest.setIsTriggered(false);
            createRecommendationRequest.setIsCampaignUpdated(false);
            createRecommendationRequest.setPushNotificationSendDateTime(null);

            log.info("createRecommendationRequest - uuid : {}, getCampaign : {}", uuid, response.getBody());

        } catch (Exception ex) {

            ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
                    ex.getLocalizedMessage(), null);

            log.error("createRecommendationRequest - getCampaign: exception {} | response : {}", ex, apiResponse);
        }

        return createRecommendationRequest;
    }

    public CustomerProfileResponse checkCustomerProfileViaCache(String cifNo) {
        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(15000);
            factory.setReadTimeout(15000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            RestTemplate restTemplate = new RestTemplate(factory);

            ProfileRequest profileRequest = new ProfileRequest();
            profileRequest.setIdType("cif_no");
            profileRequest.setIdValue(cifNo);
            profileRequest.setTableName("EVT_SOLE_CC_VIEW");

            HttpEntity<ProfileRequest> entity = new HttpEntity<>(profileRequest);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(GET_CUSTOMER_PROFILE_VIA_CACHE_URL,
                    HttpMethod.POST, entity, new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            CustomerProfileResponse customerProfile = new CustomerProfileResponse();
            Map<String, String> dataMap = (Map<String, String>) response.getBody().get("data");

            customerProfile.setCifNo(dataMap.get("cif_no"));
            customerProfile.setCustomerName(dataMap.get("customer_name"));
            customerProfile.setDevicePlatform(dataMap.get("device_platform"));
            customerProfile.setDob(dataMap.get("dob"));
            customerProfile.setEmail(dataMap.get("email"));
            customerProfile.setGender(dataMap.get("gender"));
            customerProfile.setMobile(dataMap.get("mobile"));
            customerProfile.setMobileFirstPlatformId(dataMap.get("mobile_first_platform_id"));
            customerProfile.setNationality(dataMap.get("nationality"));
            customerProfile.setNewIcNo(dataMap.get("new_ic_no"));
            customerProfile.setPackageId(dataMap.get("package_id"));
            customerProfile.setUserId(dataMap.get("user_id"));

            log.info("checkCustomerProfileViaCache : {}", customerProfile);

            return customerProfile;

        } catch (Exception ex) {
            ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
                    ex.getLocalizedMessage(), null);

            log.error("checkCustomerProfileViaCache : exception {} | response : {}", ex, apiResponse);
            return null;
        }
    }

    public CustomerProfileResponse checkCustomerProfile(String cifNo) {
        try {

            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
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

            // Create a custom HttpClient that uses the custom SSL context
            CloseableHttpClient httpClient = HttpClients.custom().setSSLContext(sslContext)
                    .setSSLHostnameVerifier((s, sslSession) -> true)
                    .build();

            // Create a custom request factory that uses the custom HttpClient
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);
            requestFactory.setConnectTimeout(15000);
            requestFactory.setReadTimeout(15000);
            RestTemplate restTemplate = new RestTemplate(requestFactory);

            ResponseEntity<CustomerProfileResponse> response = restTemplate.getForEntity(
                    CHECK_CUSTOMER_PROFILE_URL + cifNo,
                    CustomerProfileResponse.class);

            log.info("checkCustomerProfile - cif : {} , response : {}", cifNo, response.getBody());

            return response.getBody();

        } catch (NullPointerException ex) {
            // Handle the NullPointerException gracefully
            ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
                    "NullPointerException occurred: " + ex.getMessage(), null);

            log.error("checkCustomerProfile : exception {} | response : {}", ex, apiResponse);
            // Return an appropriate response or take necessary action
            return new CustomerProfileResponse(); // Return an empty CustomerProfileResponse object, or handle it based
                                                  // on your use case
        } catch (Exception ex) {
            // Handle other exceptions
            ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
                    ex.getLocalizedMessage(), null);

            log.error("checkCustomerProfile : exception {} | response : {}", ex, apiResponse);
            // Return an appropriate response or take necessary action
            return null; // Return null or handle it based on your use case
        }
    }

    public CheckCampaignResponse checkCampaign(Integer campaignId) throws Exception {
        // log.info("checkCampaign - campaignId: {}", campaignId.toString());
        CheckCampaignResponse checkCampaignResponse = new CheckCampaignResponse();
        try {
            checkCampaignResponse.setCampaignId(campaignId);
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(15000);
            factory.setReadTimeout(15000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            RestTemplate restTemplate = new RestTemplate(factory);

            ResponseEntity<Campaign> response = restTemplate.getForEntity(GET_CAMPAIGN_ENDPOINT_URL + "/" + campaignId,
                    Campaign.class);

            Campaign campaign = response.getBody();

            // log.info("Response status code: {}", response.getStatusCode());

            if (campaign.getCampaignStatus() != Status.DISABLE) {
                Date startDate = null;
                Date endDate = null;

                startDate = campaign.getStartDate();
                endDate = campaign.getEndDate();

                if (startDate != null && endDate != null) {
                    Date now = new Date();
                    if (now.after(endDate)) {
                        checkCampaignResponse.setIsValid(false);
                        checkCampaignResponse.setIsExpired(true);
                    } else {
                        checkCampaignResponse.setIsValid(true);
                        checkCampaignResponse.setIsExpired(false);

                    }
                }
            } else {
                checkCampaignResponse.setIsDisable(true);
                checkCampaignResponse.setIsValid(false);
                checkCampaignResponse.setIsExpired(null);

            }

            return checkCampaignResponse;

        } catch (Exception ex) {

            // ApiResponse apiResponse = new
            // ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
            // ex.getLocalizedMessage(), null);

            log.error("checkCampaign service - exception : {} | campaign id : {}", ex, campaignId);
            throw ex;
        }
    }

    public List<Campaign> getCampaignByPropertiesAndCampaignStatus(String campaignPropertyKey,
            String campaignPropertyValue,
            Status campaignStatus) {

        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(15000);
            factory.setReadTimeout(15000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            RestTemplate restTemplate = new RestTemplate(factory);

            ResponseEntity<List<Campaign>> response = restTemplate.exchange(
                    GET_CAMPAIGN_BY_PROPERTIES_AND_STATUS_ENDPOINT_URL +
                            "?campaignPropertyKey=" + campaignPropertyKey + "&campaignPropertyValue="
                            + campaignPropertyValue +
                            "&campaignStatus=" + campaignStatus,
                    HttpMethod.GET, new HttpEntity<>(headers), new ParameterizedTypeReference<List<Campaign>>() {
                    });

            List<Campaign> campaigns = response.getBody();
            return campaigns;

        } catch (Exception ex) {

            ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
                    ex.getLocalizedMessage(), null);
            log.info("getCampaignByPropertiesAndCampaignStatus: exception {} | response : {}", ex, apiResponse);
            return null;
        }
    }

    public ProcessCampaignResponse processCampaign(ProcessCampaignRequest processCampaignRequest) {
        log.info("processCampaign - processCampaignRequest: {}", processCampaignRequest);
        ProcessCampaignResponse processCampaignResponse = new ProcessCampaignResponse();

        if (processCampaignRequest.getIsProcess() != null && !processCampaignRequest.getIsProcess()) {

            Date startDate = null;
            Date endDate = null;
            Boolean active = false;
            List<Campaign> campaigns = getCampaignByPropertiesAndCampaignStatus("transactionType",
                    processCampaignRequest.getTransactionType(), Status.ACTIVE);

            if (campaigns.size() > 1 || campaigns == null) {
                log.info(
                        "processCampaign - getCampaignByPropertiesAndCampaignStatus - ReferenceId : {}, campaigns : {}",
                        processCampaignRequest.getReferenceId(), campaigns);
                return null;
            }

            // log.info("processCampaign - getCampaignByPropertiesAndCampaignStatus: {}",
            // campaigns);
            for (Campaign campaign : campaigns) {
                active = campaign.getCampaignStatus() == Status.ACTIVE ? true : false;
                startDate = campaign.getStartDate();
                endDate = campaign.getEndDate();
                // log.info("Start date: {}", startDate);
                // log.info("End date: {}", endDate);
                // log.info("Active: {}", active);
                if (startDate != null && endDate != null && active) {

                    Date now = new Date();
                    if (now.after(startDate) && now.before(endDate)) {
                        processCampaignResponse.setIsValid(true);
                        processCampaignResponse.setCampaignId(campaign.getCampaignId());
                        processCampaignResponse.setCampaignStatus(campaign.getCampaignStatus());
                        processCampaignResponse.setStartDate(startDate);
                        processCampaignResponse.setEndDate(endDate);
                        processCampaignResponse
                                .setTransactionType(campaign.getCampaignProperties().get("transactionType"));

                        // Check if pandaiBroadcastFlowName exists, otherwise set to null
                        if (campaign.getCampaignProperties().containsKey("pandaiBroadcastFlowName")) {
                            processCampaignResponse.setPandaiBroadcastFlowName(
                                    campaign.getCampaignProperties().get("pandaiBroadcastFlowName"));
                        } else {
                            processCampaignResponse.setPandaiBroadcastFlowName(null);
                        }

                        // Check if pandaiBroadcastFlowNameReferral exists, otherwise set to null
                        if (campaign.getCampaignProperties().containsKey("pandaiBroadcastFlowNameReferral")) {
                            processCampaignResponse.setPandaiBroadcastFlowNameReferral(
                                    campaign.getCampaignProperties().get("pandaiBroadcastFlowNameReferral"));
                        } else {
                            processCampaignResponse.setPandaiBroadcastFlowNameReferral(null);
                        }
                    }
                }
            }

        }
        // }
        log.info("processCampaign - ReferenceId : {} , processCampaignResponse : {}",
                processCampaignRequest.getReferenceId(), processCampaignResponse);
        return processCampaignResponse;
    }

}
