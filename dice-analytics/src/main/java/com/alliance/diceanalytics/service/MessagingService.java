package com.alliance.diceanalytics.service;

import com.alliance.diceanalytics.request.SendCommonEmailRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MessagingService {
//        @Value("${notification.service.pandaiBroadcast.endpointUrl}")
//        private String NOTIFICATION_SERVICE_PANDAI_BROADCAST_ENDPOINT_URL;
//        @Value("${pandaiBroadcast.abbr}")
//        private String PANDAI_BROADCAST_ABBR;
//        @Value("${pandaiBroadcast.platform}")
//        private String PANDAI_BROADCAST_PLATFORM;
//        @Value("${pandaiBroadcast.message.type}")
//        private String PANDAI_BROADCAST_MESSAGE_TYPE;
        @Value("${notification.service.sendEmail.endpointUrl}")
        private String NOTIFICATION_SERVICE_SEND_EMAIL_ENDPOINT_URL;

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
//                        ResponseEntity<Map> response = restTemplate.postForEntity(NOTIFICATION_SERVICE_SEND_EMAIL_ENDPOINT_URL, emailRequests, Map.class);
                        ResponseEntity<Map> response = restTemplate.exchange(requestEntity,Map.class);


                        if(response.getStatusCode() == HttpStatus.OK) {
                                log.info("sendEmail - success: {}", response.getStatusCode());
                        }else {
                                log.info("sendEmail - failed: {}, body: {}", response.getStatusCode(), response.getBody());
                        }
                }catch (Exception ex) {
                        log.error("sendEmail - Exception: {}", ex.toString());
                }
        }
}
