package com.alliance.dicenotification.service;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PandaiTokenService {
    @Value("${pand.ai.accessToken.username}")
    private String USERNAME;

    @Value("${pand.ai.accessToken.password}")
    private String PASSWORD;

    @Value("${pand.ai.accessToken.abbr}")
    private String ABBR;

    @Value("${pand.ai.accessToken.expiry}")
    private Integer EXPIRY;

    @Value("${pand.ai.accessToken.endpointUrl}")
    private String ENDPOINT_URL;

    @Value("${infobip.proxy.ip}")
    private String INFOBIP_PROXY_IP;

    @Value("${infobip.proxy.port}")
    private String INFOBIP_PROXY_PORT;

    private static String TOKEN_VALUE;

    private static Long TOKEN_EXPIRY;

        public String getToken() {
        Date startGetToken =new Date();
        try {
            Calendar calendar = Calendar.getInstance();

            if (TOKEN_VALUE != null && !TOKEN_VALUE.isEmpty() && TOKEN_EXPIRY != null) {
                if (calendar.getTimeInMillis() < TOKEN_EXPIRY) {
                    return TOKEN_VALUE;
                }
            }

            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(15000);
            factory.setReadTimeout(15000);
            Proxy proxy = new Proxy(Type.HTTP, new InetSocketAddress(INFOBIP_PROXY_IP, 8082));
            factory.setProxy(proxy);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> bodyPair = new HashMap<String, Object>();
            bodyPair.put("user_id", USERNAME);
            bodyPair.put("password", PASSWORD);
            bodyPair.put("abbr", ABBR);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(bodyPair, headers);

            RestTemplate restTemplate = new RestTemplate(factory);
            Date startSendGetToken =new Date();
            ResponseEntity<Map> response = restTemplate.postForEntity(ENDPOINT_URL, entity, Map.class);
            Date endSendGetToken =new Date();
            log.info("Pand.ai Get Token Overall = {}", endSendGetToken.getTime()-startSendGetToken.getTime());
            log.info("getToken - responseCode: {}, responseBody: {}", response.getStatusCode(), response.getBody());

            if (response.getStatusCodeValue() == 200) {
                TOKEN_VALUE = (String) response.getBody().get("token");
                // Minus 30 seconds from the expiry to avoid scenario where there is delay from API
                calendar.add(Calendar.SECOND, EXPIRY - 30);
                TOKEN_EXPIRY = calendar.getTimeInMillis();
            }
        } catch (Exception ex) {
            log.error("getToken - Error: {}", ex);
        }
        Date endGetToken =new Date();
        log.info("Pand.ai Get Token Overall = {}", endGetToken.getTime()-startGetToken.getTime());
        return TOKEN_VALUE;
    }
}
