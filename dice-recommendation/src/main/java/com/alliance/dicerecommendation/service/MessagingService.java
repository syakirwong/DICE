package com.alliance.dicerecommendation.service;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.alliance.dicerecommendation.request.BroadcastData;
import com.alliance.dicerecommendation.request.Messages;
import com.alliance.dicerecommendation.request.PandaiBroadcastRequest;
import com.alliance.dicerecommendation.request.SendCommonEmailRequest;
import com.alliance.dicerecommendation.request.TriggerEngagementPushNotiRequest;
import com.alliance.dicerecommendation.request.UserDetails;
import com.alliance.dicerecommendation.response.PandaiBroadcastResponse;
import com.alliance.dicerecommendation.utility.FreemarkerUtil;
import com.alliance.dicerecommendation.utility.ListUtil;
import com.alliance.dicerecommendation.utility.SystemParam;
import com.fasterxml.jackson.databind.ObjectMapper;

import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MessagingService {
        @Value("${pushNotification.dice.endpointURL}")
        private String PUSH_NOTIFICATION_END_POINT_URL;

        @Value("${pushBellNotification.dice.endpointURL}")
        private String PUSH_BELL_NOTIFICATION_END_POINT_URL;

        @Value("${notification.service.pandaiBroadcast.endpointUrl}")
        private String NOTIFICATION_SERVICE_PANDAI_BROADCAST_ENDPOINT_URL;
        
        @Value("${pandaiBroadcast.abbr}")
        private String PANDAI_BROADCAST_ABBR;
        
        @Value("${pandaiBroadcast.platform}")
        private String PANDAI_BROADCAST_PLATFORM;

        @Value("${notification.service.sendEmail.endpointUrl}")
        private String NOTIFICATION_SERVICE_SEND_EMAIL_ENDPOINT_URL;

        @Autowired
        private FreemarkerUtil freemarkerUtil;

        @Autowired
        private MessageSource messageSource;

        public Boolean pushNotification(TriggerEngagementPushNotiRequest request) {
            log.info("Start - pushNotification");
                try {
                    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                    factory.setConnectTimeout(15000);
                    factory.setReadTimeout(15000);
        
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);

                    HttpEntity<TriggerEngagementPushNotiRequest> entity = new HttpEntity<>(request);
                    
                    RestTemplate restTemplate = new RestTemplate(factory);
                  
                    // log.debug("pushNotification endpointUrl : {}",PUSH_NOTIFICATION_END_POINT_URL);
                    ResponseEntity<Map> response = restTemplate.exchange(PUSH_NOTIFICATION_END_POINT_URL, HttpMethod.POST, entity, Map.class);

                    log.info("pushNotification - request: {}", request);
        
                    if(response.getStatusCode() == HttpStatus.OK) {
                        log.info("pushNotification - success: {}", response.getStatusCode());
                        return true;
        
                    }else {
                        log.error("pushNotification - failed: {}, body: {}", response.getStatusCode(), response.getBody());
                        return false;
                    }
                }catch (Exception ex) {
                    log.error("pushNotification - Exception: {}", ex);
                    return false;
                }
        }

        public Boolean pushBellNotification(TriggerEngagementPushNotiRequest pushNotificationRequest, String cifNo) {
            log.info("Start - pushBellNotification");
                try {
                    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                    factory.setConnectTimeout(15000);
                    factory.setReadTimeout(15000);
        
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    
                    // Set push bell notification notification type
                    if(pushNotificationRequest.getMessageDisplayMethod()!= null && pushNotificationRequest.getMessageDisplayMethod().equalsIgnoreCase("INAPPBRW")){
                        pushNotificationRequest.setNotificationType(pushNotificationRequest.getMessageDisplayMethod());
                    } else {
                        // Default
                        pushNotificationRequest.setNotificationType("INAPPMSG");
                    }


                    HttpEntity<TriggerEngagementPushNotiRequest> entity = new HttpEntity<>(pushNotificationRequest);
                    RestTemplate restTemplate = new RestTemplate(factory);
                    ResponseEntity<Map> response = restTemplate.exchange(PUSH_BELL_NOTIFICATION_END_POINT_URL+cifNo, HttpMethod.PUT, entity, Map.class);
        
                    log.debug("pushBellNotification - request: {}", entity);
        
                    if(response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.NO_CONTENT) {
                        log.info("pushBellNotification - success: {}", response.getStatusCode());
                        return true;
        
                    }else {
                        log.error("pushBellNotification - failed: {}, body: {}", response.getStatusCode(), response.getBody());
                        return false;
                    }
                }catch (Exception ex) {
                    log.error("pushBellNotification - Exception: {}", ex);
                    return false;
                }
        }

        public PandaiBroadcastResponse pandaiBroadcast(String name, String mobileNumber, String referralCode, String text,
            String type, String flowName, String language, String platformChannel) {
            PandaiBroadcastRequest request = new PandaiBroadcastRequest();
            BroadcastData broadcastData = new BroadcastData();
            Messages messages = new Messages();
            UserDetails userDetails = new UserDetails();
            Map<String, Object> user_data = new HashMap<String, Object>();
            // if(name != null && !name.isEmpty()){
            //     user_data.put("name", name);
            // }

            Map<String, Object> object_data = new HashMap<String, Object>();

            if (referralCode != null && !referralCode.isEmpty()) {
                object_data.put("referral_code", referralCode);
            }

            if(!object_data.isEmpty()){
                user_data.put(platformChannel, object_data);
            } else {
                log.info("pandaiBroadcast - object_data is empty for user_id : {}", mobileNumber);
            }

            userDetails.setUser_id(mobileNumber);
            if(name != null && !name.isEmpty()){
                userDetails.setUser_name(name);
            }
            userDetails.setUser_data(user_data);
            userDetails.setOverwrite_user_data(false);
            messages.setType(type);

            Map<String, Object> data = new HashMap<String, Object>();
            switch (type) {
                case "message":
                    data.put("text", text);
                    break;
                case "flow":
                    data.put("flow_name", flowName);
                    data.put("language", language);
                    break;
                default:
                    break;
            }

            messages.setData(data);

            broadcastData.setPlatform(PANDAI_BROADCAST_PLATFORM);
            broadcastData.setMessages(Arrays.asList(messages));
            broadcastData.setUser_details(Arrays.asList(userDetails));

            request.setAbbr(PANDAI_BROADCAST_ABBR);
            request.setBroadcast_data(Arrays.asList(broadcastData));

            log.debug("pandaiBroadcast - request: {}", request);

            try {
                SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                factory.setConnectTimeout(15000);
                factory.setReadTimeout(15000);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                RestTemplate restTemplate = new RestTemplate(factory);
                ResponseEntity<Map> response = restTemplate
                        .postForEntity(NOTIFICATION_SERVICE_PANDAI_BROADCAST_ENDPOINT_URL, request, Map.class);

                if (response.getStatusCode() == HttpStatus.OK) {
                    log.info("pandaiBroadcast - success: {}", response.getStatusCode());
                    if (response.getBody().get("result") instanceof ArrayList) {
                        ObjectMapper mapper = new ObjectMapper();
                        List<?> list = ListUtil.convertObjectToList(response.getBody().get("result"));
                        PandaiBroadcastResponse responseClass = mapper.convertValue(list.get(0),
                                PandaiBroadcastResponse.class);
                        return responseClass;
                    }

                } else {
                    log.error("pandaiBroadcast - failed: {}, body: {}", response.getStatusCode(), response.getBody());
                }
            } catch (Exception ex) {
                log.error("pandaiBroadcast - Exception: {}", ex);
            }
            return null;
    }

    public void sendEmail(List<SendCommonEmailRequest> emailRequests) {
        try {
                TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return null;
                        }

                        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                        }
                } };

                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

                // Create a custom HttpClient that uses the custom SSL context
                CloseableHttpClient httpClient = HttpClients.custom().setSSLContext(sslContext).build();

                HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
                factory.setConnectTimeout(15000);
                factory.setReadTimeout(15000);
                factory.setHttpClient(httpClient);

                Map<String, Object> body = new HashMap<String, Object>();
                body.put("sendEmailRequest", emailRequests);

                RequestEntity<List<SendCommonEmailRequest>> requestEntity = RequestEntity
                        .post(new URL(NOTIFICATION_SERVICE_SEND_EMAIL_ENDPOINT_URL).toURI())
                        .contentType(MediaType.APPLICATION_JSON) .body(emailRequests);

                RestTemplate restTemplate = new RestTemplate(factory);
                ResponseEntity<Map> response = restTemplate.exchange(requestEntity,Map.class);

                if(response.getStatusCode() == HttpStatus.OK) {
                        log.info("sendEmail - success: {}", response.getStatusCode());
                } else {
                        log.info("sendEmail - failed: {}, body: {}", response.getStatusCode(), response.getBody());
                }
        } catch (Exception ex) {
                log.error("sendEmail - Exception: {}", ex.toString());
        }
    }

    public SendCommonEmailRequest createCampaignScheduleAlertEmailRequest(String currentDate,Map<String, Object> mailInfo) throws TemplateException, IOException {
        SendCommonEmailRequest request = null;
        String body = freemarkerUtil.getEmailContentFromTemplate(mailInfo);

            request = new SendCommonEmailRequest(
                    messageSource.getMessage("spring.mail.subject.campaign.schedule.alert",
                    new String[] {currentDate}, Locale.ENGLISH),
                    null,
                    SystemParam.getInstance().getMailCampaignScheduleFailureHandleAlertTo().toArray(new String[0]),
                    body,
                    null
            );

        log.info("createCampaignScheduleAlertEmailRequest - send alert mail to {}", SystemParam.getInstance().getMailCampaignScheduleFailureHandleAlertTo());
        return request;
    }
}
