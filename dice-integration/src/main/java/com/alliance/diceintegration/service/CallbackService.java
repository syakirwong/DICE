package com.alliance.diceintegration.service;

import com.alliance.diceintegration.constant.ApiResponse;
import com.alliance.diceintegration.constant.DataField.Status;
import com.alliance.diceintegration.exception.ServiceException;
import com.alliance.diceintegration.model.Campaign;
import com.alliance.diceintegration.model.CustomerProfile;
import com.alliance.diceintegration.model.EngagementModeTemplate;
import com.alliance.diceintegration.model.ReferralCode;
import com.alliance.diceintegration.repository.EngagementModeRepository;
import com.alliance.diceintegration.request.CampaignRequest;
import com.alliance.diceintegration.request.CustomerActionTrailRequest;
import com.alliance.diceintegration.request.ProcessCampaignRequest;
import com.alliance.diceintegration.request.UpdateCampaignCustomerStatusRequest;
import com.alliance.diceintegration.response.CampaignCheckResponse;
import com.alliance.diceintegration.response.ProcessCampaignResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

@Slf4j
@Service
public class CallbackService {

    @Autowired
    private EngagementModeRepository engagementModeRepository;

    @Value("${get.campaign.recommendation.endpointUrl}")
    private String GET_CAMPAIGN_RECOMMENDATION_ENDPOINT_URL;

    @Value("${createCustomerActionTrail.endpointUrl}")
    private String CREATE_CUST_ACTION_TRAIL_ENDPOINT_URL;

    @Value("${checkCampaign.endpointUrl}")
    private String CHECK_CAMPAIGN_ENDPOINT_URL;

    @Value("${getReferralCodeByValue.endpointUrl}")
    private String GET_REFERRAL_CODE_BY_VALUE_ENDPOINT_URL;

    @Value("${getReferralCodeByUuid.endpointUrl}")
    private String GET_REFERRAL_CODE_BY_UUID_ENDPOINT_URL;

    @Value("${processCampaign.endpointUrl}")
    private String PROCESS_CAMPAIGN_ENDPOINT_URL;

    @Value("${updateCust.CampaignStatus.endpointUrl}")
    private String UPDATE_CUST_CAMPAIGN_STATUS_ENDPOINT_URL;

    @Value("${get.campaign.endpointUrl}")
    private String GET_CAMPAIGN_ENDPOINT_URL;

    @Value("${get.customer.profile.endpointUrl}")
    private String GET_CUSTOMER_PROFILE_URL;

    public Object getCampaignRecommendation(String uuidType, String uuid, String engagementMode, String campaignId) {

        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(15000);
            factory.setReadTimeout(15000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            RestTemplate restTemplate = new RestTemplate(factory);

            Object response;
            if (campaignId != null) {
                response = restTemplate
                        .getForObject(GET_CAMPAIGN_RECOMMENDATION_ENDPOINT_URL + uuidType + "&uuid=" + uuid
                                + "&engagementMode=" + engagementMode + "&campaignId=" + campaignId, Object.class);
            } else {
                response = restTemplate.getForObject(
                        GET_CAMPAIGN_RECOMMENDATION_ENDPOINT_URL + uuidType + "&uuid=" + uuid
                                + "&engagementMode=" + engagementMode,
                        Object.class);
            }

            // Check if the message is not "Operation Successful"
            if (response instanceof Map) {
                Map<String, Object> responseMap = (Map<String, Object>) response;

                if (responseMap.containsKey("message")) {
                    String message = (String) responseMap.get("message");

                    if (message != null && !message.equals("Operation Successful.")) {
                        // log the full response
                        log.info(
                                "getCampaignRecommendation - integration , uuidType = {} | uuid = {} | engagementMode = {} | campaignId = {} Response : {}",
                                uuidType, uuid, engagementMode, campaignId, response);
                    } else {
                        if (responseMap.containsKey("result")) {
                            Map<String, Object> resultMap = (Map<String, Object>) responseMap.get("result");
                            String tempResponse = null;
                            if (resultMap.containsKey("engagementMode") && resultMap.containsKey("campaignId")
                                    && resultMap.containsKey("inAppMessageTemplateId")) {
                                tempResponse = "engagementMode : " + (String) resultMap.get("engagementMode")
                                        + ", campaignId : " + (String) resultMap.get("campaignId")
                                        + ", inAppMessageTemplateId : "
                                        + (String) resultMap.get("inAppMessageTemplateId");
                            }

                            log.info(
                                    "getCampaignRecommendation - integration , uuidType = {} | uuid = {} | engagementMode = {} | campaignId = {} Response : {}",
                                    uuidType, uuid, engagementMode, campaignId, tempResponse);
                        } else{
                            log.warn("getCampaignRecommendation - integration , uuidType = {} | uuid = {} | engagementMode = {} | campaignId = {} Response no contain result key", uuidType, uuid, engagementMode, campaignId);
                        }
                    }
                } else {
                    log.warn("getCampaignRecommendation - integration , uuidType = {} | uuid = {} | engagementMode = {} | campaignId = {} Response no contain message key", uuidType, uuid, engagementMode, campaignId);
                }

            }

            // log.info("getCampaignRecommendation - integration , uuidType = {} | uuid = {} | engagementMode = {} | campaignId = {} Response : {}", uuidType, uuid, engagementMode, campaignId, response);

            return response;

        } catch (Exception ex) {

            log.error("getCampaignRecommendation service - Exception : {}", ex);

            ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
                    ex.getLocalizedMessage(), null);

            return apiResponse;
        }
    }

    public Object createCustomerActionTrail(CustomerActionTrailRequest request) throws ServiceException {
        try {
            if (request.getCampaignId() != null) {
                // log.info("createCustomerActionTrail - get Engagement Mode for campaignId : {}",
                //         request.getCampaignId());

                request.setEngagementMode(lookUpEngagementMode(request.getCampaignId()));

            }

            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(15000);
            factory.setReadTimeout(15000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            RestTemplate restTemplate = new RestTemplate(factory);
            ResponseEntity<Object> response = restTemplate
                    .postForEntity(CREATE_CUST_ACTION_TRAIL_ENDPOINT_URL, request,
                            Object.class);
            // log.info("createCustomerActionTrail : {}", response);
            return response.getBody();
        } catch (Exception ex) {
            log.error("createCustomerJourney - Exception: {}", ex);
            throw new ServiceException(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, ex.toString());
        }
    }

    // public CampaignCheckResponse checkCampaign(Integer campaignId) throws ServiceException {
    //     try {
    //         log.info("checkCampaign - start with campaign id : {}", campaignId);
    //         SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    //         factory.setConnectTimeout(15000);
    //         factory.setReadTimeout(15000);

    //         HttpHeaders headers = new HttpHeaders();
    //         headers.setContentType(MediaType.APPLICATION_JSON);

    //         RestTemplate restTemplate = new RestTemplate(factory);
    //         ResponseEntity<CampaignCheckResponse> response = restTemplate.getForEntity(
    //                 CHECK_CAMPAIGN_ENDPOINT_URL + campaignId,
    //                 CampaignCheckResponse.class);
    //         log.info("checkCampaign : {}", response.getBody());
    //         return response.getBody();
    //     } catch (Exception ex) {
    //         log.error("checkCampaign - Exception: {}", ex);
    //         throw new ServiceException(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, ex.toString());
    //     }
    // }

    @Async
    public CampaignCheckResponse checkCampaign(Integer campaignId) throws Exception {
        try {
            log.info("start - checkCampaign for campaign id : {}", campaignId);
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(15000);
            factory.setReadTimeout(15000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            RestTemplate restTemplate = new RestTemplate(factory);

            Object response;

            response = restTemplate.getForObject(CHECK_CAMPAIGN_ENDPOINT_URL + campaignId, Object.class);

            if (response!=null){
                log.info("checkCampaign : {}", response);
                CampaignCheckResponse campaignCheckResponse = new CampaignCheckResponse();

                if (response instanceof Map) {
                    Map<String, Object> responseMap = (Map<String, Object>) response;

                    if (responseMap.containsKey("result") && responseMap.get("result") instanceof Map) {
                        Map<String, Object> resultMap = (Map<String, Object>) responseMap.get("result");
                    
                        if (resultMap.containsKey("isValid") && resultMap.containsKey("isExpired") && resultMap.containsKey("isDisable")) {
                            campaignCheckResponse.setIsValid(Boolean.parseBoolean(resultMap.get("isValid").toString()));
                            campaignCheckResponse.setIsExpired(Boolean.parseBoolean(resultMap.get("isExpired").toString()));
                            campaignCheckResponse.setIsDisable(Boolean.parseBoolean(resultMap.get("isDisable").toString()));
                            campaignCheckResponse.setCampaignId(campaignId);
                            log.info("checkCampaign - checkCampaignResponse : {}", campaignCheckResponse);
                           
                            return campaignCheckResponse;
                        } else {
                            log.info("checkCampaign - field does not exist.");
                            return null;
                        }
                    } else {
                        log.info("checkCampaign - field does not exist.");
                        return null;
                    }
                    
                } else {
                    log.info("checkCampaign - Invalid response format.");
                    return null;
                }
            } else{
                log.info("checkCampaign - campaign id {} not found", campaignId);
                return null;
            }
        } catch (Exception ex) {
            log.error("checkCampaign - campaign id : {} | exception : {}", campaignId, ex);
            return null;
        }
    }    

    public ProcessCampaignResponse processCampaign(ProcessCampaignRequest request) {

        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(15000);
            factory.setReadTimeout(15000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            RestTemplate restTemplate = new RestTemplate(factory);
            ResponseEntity<ProcessCampaignResponse> response = restTemplate.postForEntity(PROCESS_CAMPAIGN_ENDPOINT_URL,
                    request,
                    ProcessCampaignResponse.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("processCampaign - success: {}", response.getStatusCode());

            } else {
                log.error("processCampaign - failed: {}, body: {}", response.getStatusCode(), response.getBody());
            }

            return response.getBody();

        } catch (Exception ex) {
            log.error("processCampaign - Exception: {}", ex);
            return null;
        }
    }

    public ReferralCode getReferralCodeByCodeValue(String referralCode, Status status) throws ServiceException {
        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(15000);
            factory.setReadTimeout(15000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            RestTemplate restTemplate = new RestTemplate(factory);
            ResponseEntity<ReferralCode> response = restTemplate.getForEntity(
                    GET_REFERRAL_CODE_BY_VALUE_ENDPOINT_URL + referralCode + "&status=" + status,
                    ReferralCode.class);
            log.info("getReferralCodeByCodeValue : {}", response.getBody());
            return response.getBody();
        } catch (Exception ex) {
            log.error("getReferralCodeByCodeValue - Exception: {}", ex);
            throw new ServiceException(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, ex.toString());
        }
    }

    public ReferralCode getReferralCodeByUuid(String uuidType, String uuid)
            throws ServiceException {
        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(15000);
            factory.setReadTimeout(15000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            RestTemplate restTemplate = new RestTemplate(factory);
            ResponseEntity<ReferralCode> response = restTemplate.getForEntity(
                    GET_REFERRAL_CODE_BY_UUID_ENDPOINT_URL + "?uuidType="
                            + uuidType + "&uuid=" + uuid,
                    ReferralCode.class);
            log.info("getReferralCodeByUuid: {}", response.getBody());
            return response.getBody();

        } catch (Exception ex) {
            log.error("getReferralCodeByUuid - Exception: {}", ex);
            throw new ServiceException(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, ex.toString());
        }
    }

    public Object updateCustCampaignStatus(UpdateCampaignCustomerStatusRequest data) {
        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(15000);
            factory.setReadTimeout(15000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<String, Object>();
            body.put("cifNo", data.getCifNo());
            body.put("campaignId", data.getCampaignId());
            body.put("isCampaignUpdated", data.getIsCampaignUpdated());
            body.put("isIgnore", data.getIsIgnore());
            if(data.getIdType()!= null){
                body.put("idType", data.getIdType());
            }
            if(data.getEngagementMode()!= null){
                body.put("engagementMode",data.getEngagementMode());
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            RestTemplate restTemplate = new RestTemplate(factory);

            ResponseEntity<Object> response = restTemplate.exchange(UPDATE_CUST_CAMPAIGN_STATUS_ENDPOINT_URL,
                    HttpMethod.PUT, entity, Object.class);

            log.info("updateCustCampaignStatus : {}", response);

            return response.getBody();

        } catch (Exception ex) {
            log.error("updateCustCampaignStatus - Exception: {}", ex);
            ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
                    ex.getLocalizedMessage(), null);
            Object response = null;
            return response;
        }
    }

    public Campaign getCampaignById(Integer campaignId) {
        Campaign campaign = new Campaign();
        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(15000);
            factory.setReadTimeout(15000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            RestTemplate restTemplate = new RestTemplate(factory);

            ResponseEntity<Campaign> response = restTemplate.getForEntity(
                    GET_CAMPAIGN_ENDPOINT_URL + "/" + campaignId.toString(),
                    Campaign.class);

            campaign = response.getBody();

        } catch (Exception ex) {
            ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, false,
                    ex.getLocalizedMessage(), null);

            log.info("getCampaign: exception {} | response : {}", ex, apiResponse);
        }
        return campaign;

    }

    public Set<String> lookUpEngagementMode(Integer campaignId) throws JSONException {
        RestTemplate restTemplate = getDefaultHttpTemplate();

        ResponseEntity<CampaignRequest> result = restTemplate
                .getForEntity(GET_CAMPAIGN_ENDPOINT_URL + campaignId,
                        CampaignRequest.class);

        Set<String> engagementModeName = new HashSet<>();
        if (!result.getBody().toJsonString().isEmpty()) {

            JSONObject jsonResponse = new JSONObject(result.getBody().toJsonString());
            if (jsonResponse.has("campaignProperties")) {
                if (jsonResponse.getJSONObject("campaignProperties").has("engagementModeTemplateId")) {
                    String[] engagementIds = jsonResponse
                            .getJSONObject("campaignProperties")
                            .getString("engagementModeTemplateId").split(",");

                    for (String id : engagementIds) {
                        // log.info("Engagement IDs" + id);
                        Optional<EngagementModeTemplate> engagementMode = engagementModeRepository
                                .findById(Integer.parseInt(id));
                        engagementModeName.add(engagementMode.get().getEngagementModeName());
                    }
                }
            }
        }
        if (engagementModeName.isEmpty())
            return null;
        return engagementModeName;
    }

    private RestTemplate getDefaultHttpTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(15000);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        RestTemplate restTemplate = new RestTemplate(factory);
        return restTemplate;

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

}
