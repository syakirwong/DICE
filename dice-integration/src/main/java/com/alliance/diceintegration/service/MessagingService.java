package com.alliance.diceintegration.service;

import com.alliance.diceintegration.request.*;
import com.alliance.diceintegration.response.PandaiBroadcastResponse;
import com.alliance.diceintegration.utility.ListUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class MessagingService {
    @Value("${notification.service.sendEmail.endpointUrl}")
    private String NOTIFICATION_SERVICE_SEND_EMAIL_ENDPOINT_URL;
    @Value("${notification.service.pandaiBroadcast.endpointUrl}")
    private String NOTIFICATION_SERVICE_PANDAI_BROADCAST_ENDPOINT_URL;
    @Value("${notification.service.pushNotis.endpointUrl}")
    private String NOTIFICATION_SERVICE_PUSH_NOTIS_ENDPOINT_URL;
    @Value("${pandaiBroadcast.abbr}")
    private String PANDAI_BROADCAST_ABBR;
    @Value("${pandaiBroadcast.platform}")
    private String PANDAI_BROADCAST_PLATFORM;

    public void sendEmail(List<SendCommonEmailRequest> emailRequests) {
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

    public PandaiBroadcastResponse pandaiBroadcast(String name, String mobileNumber, String referralCode, String text,
            String type, String flowName, String language, String inviteCode, String inviteId, String platformChannel) {
        PandaiBroadcastRequest request = new PandaiBroadcastRequest();
        BroadcastData broadcastData = new BroadcastData();
        Messages messages = new Messages();
        UserDetails userDetails = new UserDetails();
        Map<String, Object> user_data = new HashMap<String, Object>();
        // user_data.put("name", name);
        Map<String, Object> object_data = new HashMap<String, Object>();

        if (referralCode != null && !referralCode.isEmpty()) {
            object_data.put("referral_code", referralCode);
        }

        if (inviteCode != null && !inviteCode.isEmpty()) {
            object_data.put("invite_code", inviteCode);
        }

        if (inviteId != null && !inviteId.isEmpty()){
            object_data.put("invite_id", inviteId);
        }

        if(!object_data.isEmpty()){
            user_data.put(platformChannel, object_data);
        } else {
            log.info("pandaiBroadcast - object_data is empty for user_id : {}", mobileNumber);
        }

        userDetails.setUser_id(mobileNumber);
        userDetails.setUser_name(name);
        userDetails.setOverwrite_user_data(false);
        userDetails.setUser_data(user_data);
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

    public void pushNotification(ReferralPushNotisRequest request) {

        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(15000);
            factory.setReadTimeout(15000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            RestTemplate restTemplate = new RestTemplate(factory);
            ResponseEntity<Map> response = restTemplate.postForEntity(NOTIFICATION_SERVICE_PUSH_NOTIS_ENDPOINT_URL,
                    request, Map.class);

            log.debug("pushNotification - request: {}", request);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("pushNotification - success: {}", response.getStatusCode());

            } else {
                log.error("pushNotification - failed: {}, body: {}", response.getStatusCode(), response.getBody());
            }
        } catch (Exception ex) {
            log.error("pushNotification - Exception: {}", ex);
        }
    }
}
