package com.alliance.dicesqlitecache.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.alliance.dicesqlitecache.request.SendCommonEmailRequest;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MessagingService {

    @Value("${notification.service.sendEmail.endpointUrl}")
    private String NOTIFICATION_SERVICE_SEND_EMAIL_ENDPOINT_URL;

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

}
