package com.alliance.dicenotification.service;

import com.alliance.dicenotification.exception.ServiceException;
import com.alliance.dicenotification.request.EngagementTriggerPushNotiRequest;
import com.alliance.dicenotification.request.PandaiBroadcastRequest;
import com.alliance.dicenotification.request.ReferralPushNotisRequest;
import com.alliance.dicenotification.request.SendCommonEmailRequest;
import com.alliance.dicenotification.utility.SFTPUtil;
import com.alliance.dicenotification.utility.SystemParam;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class MessagingService {

        @Value("${spring.mail.from}")
        private String EMAIL_FROM;

        @Value("${pushNotification.endpointURL}")
        private String PUSH_NOTIFICATION_END_POINT_URL;

        @Value("${pushNotification.Authorization}")
        private String PUSH_NOTIFICATION_AUTHORIZATION;

        @Value("${pushBellNotification.endpointURL}")
        private String PUSH_BELL_NOTIFICATION_END_POINT_URL;

        @Value("${infobip.proxy.ip}")
        private String INFOBIP_PROXY_IP;

        @Value("${infobip.proxy.port}")
        private String INFOBIP_PROXY_PORT;

        @Value("${pand.ai.broadcast.endpointUrl}")
        private String PAND_AI_BROADCAST_ENDPOINT_URL;

        private static final String destiDirectory = "/home/email/";

        @Autowired
        private JavaMailSender javaMailSender;

        @Autowired
        private PandaiTokenService pandaiTokenService;

        @Async
        public void sendCommonEmail(List<SendCommonEmailRequest> sendCommonEmailRequests)
                        throws MessagingException, IOException,
                        com.alliance.dicenotification.exception.ServiceException {
                sendCommonEmailRequests.forEach(emailRequest -> {
                        try {
                                MimeMessage mimeMessage = javaMailSender.createMimeMessage();
                                MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);

                                mimeMessageHelper.setSubject(emailRequest.getMailSubject());
                                mimeMessageHelper.setFrom(EMAIL_FROM);
                                mimeMessageHelper.setTo(emailRequest.getMailTo());

                                mimeMessageHelper.setText(emailRequest.getMailContent(), true);

                                if (!Objects.isNull(emailRequest.getFilePaths())) {
                                        for (String path : emailRequest.getFilePaths()) {
                                                SFTPUtil sftpUtil = new SFTPUtil(
                                                                SystemParam.getInstance().getSFTPProfile());
                                                byte[] bytes = sftpUtil.downloadFile(path);
                                                log.debug("Downloaded bytes: {}", bytes);
                                                ByteArrayDataSource attachment = new ByteArrayDataSource(bytes,
                                                                "application/octet-stream");
                                                mimeMessageHelper.addAttachment(path, attachment);
                                        }
                                }

                                if (!Objects.isNull(emailRequest.getAttachmentBytes())) {
                                        // Attach the file to the email
                                        log.info("sendCommonEmail - Attach the  file to the email");
                                        // ByteArrayDataSource attachment = new
                                        // ByteArrayDataSource(emailRequest.getEncryptedBytes(),
                                        // "application/octet-stream");
                                        mimeMessageHelper.addAttachment(emailRequest.getAttachmentFileName(),
                                                        new ByteArrayResource(emailRequest.getAttachmentBytes()));
                                }

                                javaMailSender.send(mimeMessageHelper.getMimeMessage());
                        } catch (MessagingException ex) {
                                log.error("sendCommonEmail - MessagingException: {}", ex);
                        }
                });
        }

        @Async
        public void pushNotification(EngagementTriggerPushNotiRequest request) throws ServiceException {
                log.info("Start - pushNotification with PersonalInfoUpdatePushNotiRequest: {}", request);
                SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                factory.setConnectTimeout(60000);
                factory.setReadTimeout(60000);

                HttpHeaders headers = new HttpHeaders();
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Content-Type", "application/json");
                headers.set("Authorization", "Basic " + PUSH_NOTIFICATION_AUTHORIZATION);

                Date pushStart = new Date();

                Map<String, Object> body = new HashMap<String, Object>();
                body.put("title", request.getTitle());
                body.put("message", request.getContent());
                body.put("devicePlatform",
                                request.getDevicePlatform() != null ? request.getDevicePlatform().toUpperCase() : "");
                body.put("notificationType", request.getNotificationType());
                String[] deviceIds = { request.getDeviceId() };
                body.put("deviceIds", deviceIds);
                Map<String, Object> data = new HashMap<String, Object>();
                data.put("deviceUuid", request.getDeviceId());
                data.put("engagementId", request.getEngagementId());
                data.put("contentId", request.getContentId());
                if(request.getMessageDisplayMethod() != null && !request.getMessageDisplayMethod().isEmpty()){
                        data.put("messageDisplayMethod", request.getMessageDisplayMethod());
                }
                body.put("data", data);

                Date startPush = new Date();
                try {
                        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
                        RestTemplate restTemplate = new RestTemplate(factory);
                        ResponseEntity<Map> response = restTemplate.postForEntity(PUSH_NOTIFICATION_END_POINT_URL,
                                        entity, Map.class);
                        if (response.getStatusCode().value() != 202) {
                                log.error("pushNotification - statuscode {}, failed : {}",
                                                response.getStatusCode(), response);
                        } else {
                                Date endPush = new Date();
                                log.info("Push notification time - {}",
                                                endPush.getTime() - startPush.getTime());
                                log.info("pushNotification - statuscode {}, body{}",
                                                response.getStatusCode(),
                                                response.getBody());
                        }

                } catch (Exception ex) {
                        Date endPush = new Date();
                        log.info("Push notification time - {}", endPush.getTime() - startPush.getTime());
                        log.error("pushNotification - Error: {}", ex);
                }

                Date pushEnd = new Date();
                log.info("Push per notification - {} ", pushEnd.getTime() - pushStart.getTime());
        }

        @Async
        public void pushBellBox(EngagementTriggerPushNotiRequest request, String cifNo) throws ServiceException {

                try {
                        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                        factory.setConnectTimeout(60000);
                        factory.setReadTimeout(60000);

                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);

                        Map<String, Object> body = new HashMap<String, Object>();
                        body.put("_type", request.getType());
                        body.put("notificationType", request.getNotificationType());
                        body.put("notificationTitle", request.getTitle());
                        body.put("notificationValue", request.getContentId());
                        body.put("message", request.getContent());

                        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
                        RestTemplate restTemplate = createRestTemplate();
                        ResponseEntity<Map> response = restTemplate.exchange(
                                        PUSH_BELL_NOTIFICATION_END_POINT_URL + cifNo, HttpMethod.PUT, entity,
                                        Map.class);
                        log.debug("pushBellNotification - request: {}", body);

                        if (response.getStatusCode() == HttpStatus.OK) {
                                log.info("pushBellNotification - success: {}", response.getStatusCode());

                        } else {
                                log.error("pushBellNotification - failed: {}, body: {}", response.getStatusCode(),
                                                response.getBody());
                        }
                } catch (Exception ex) {
                        log.error("pushBellNotification - Exception: {}", ex);
                }
        }

        @Async
        public Object pandaiBroadcast(PandaiBroadcastRequest request) throws ServiceException {
                try {
                        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                        factory.setConnectTimeout(15000);
                        factory.setReadTimeout(15000);
                        Proxy proxy = new Proxy(Type.HTTP, new InetSocketAddress(INFOBIP_PROXY_IP, 8082));
                        factory.setProxy(proxy);

                        HttpHeaders headers = new HttpHeaders();
                        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                        headers.setContentType(MediaType.TEXT_PLAIN);
                        headers.set("Authorization", "Bearer " + pandaiTokenService.getToken());

                        ObjectMapper mapper = new ObjectMapper();

                        HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(request), headers);
                        log.info("pandaiBroadcast - request: {}", mapper.writeValueAsString(request));
                        RestTemplate restTemplate = new RestTemplate(factory);
                        // ResponseEntity<Map> response = restTemplate.postForEntity(PAND_AI_BROADCAST_ENDPOINT_URL,
                        //                 entity, Map.class);

                        // log.info("pandaiBroadcast : {}", response.getStatusCode());

                        // if (response.getStatusCode() == HttpStatus.OK) {
                        //         log.info("pandaiBroadcast - results: {}", response.getBody().get("results"));
                        //         return response.getBody().get("results");
                        // } else {
                        //         log.error("pandaiBroadcast - response body: {}", response.getBody());
                        // }

                        ResponseEntity<Object> response = restTemplate.postForEntity(PAND_AI_BROADCAST_ENDPOINT_URL, entity, Object.class);

                        if (response.getStatusCode() == HttpStatus.OK) {
                                Object responseBody = response.getBody();
                                if (responseBody instanceof Map) {
                                // Handle response as a JSON object (Map)
                                Map<String, Object> responseMap = (Map<String, Object>) responseBody;
                                log.info("pandaiBroadcast - results: {}", responseMap.get("results"));
                                return responseMap.get("results");
                                } else if (responseBody instanceof Integer) {
                                // Handle response as an integer
                                log.info("pandaiBroadcast - response is an integer: {}", responseBody);
                                // Handle the integer response as needed
                                return responseBody;
                                } else {
                                // Handle other response types if necessary
                                log.error("pandaiBroadcast - unexpected response type: {}", responseBody);
                                }
                        } else {
                                log.error("pandaiBroadcast - response body: {}", response.getBody());
                        }
                } catch (Exception ex) {
                        log.error("pandaiBroadcast - Exception: {}", ex);
                }
                return null;
        }

        @Async
        public void referralPushNotification(ReferralPushNotisRequest request) throws ServiceException {
                SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                factory.setConnectTimeout(60000);
                factory.setReadTimeout(60000);

                HttpHeaders headers = new HttpHeaders();
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Content-Type", "application/json");
                headers.set("Authorization", "Basic " + PUSH_NOTIFICATION_AUTHORIZATION);

                Date pushStart = new Date();

                log.info("referralPushNotification - referralPushNotiReq: {}", request);

                Map<String, Object> body = new HashMap<String, Object>();
                body.put("title", request.getTitle());
                body.put("message", request.getMessage());
                // body.put("deviceIds", request.getDeviceId());
                body.put("devicePlatform", request.getDevicePlatform() != null
                                ? request.getDevicePlatform().toUpperCase()
                                : "");
                body.put("notificationType", request.getNotificationType());
                String[] deviceIds = { request.getDeviceId() };
                body.put("deviceIds", deviceIds);
                Map<String, Object> data = new HashMap<String, Object>();
                data.put("deviceId", request.getDeviceId());
                data.put("deviceUuid", request.getDeviceId());
                data.put("contentId", request.getContentId());
                // data.put("engagementId", request.getEngagementId());
                body.put("data", data);
                Date startPush = new Date();
                try {
                        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
                        RestTemplate restTemplate = new RestTemplate(factory);
                        ResponseEntity<Map> response = restTemplate.postForEntity(PUSH_NOTIFICATION_END_POINT_URL,
                                        entity, Map.class);
                        if (response.getStatusCode().value() != 202) {
                                log.error("pushNotification - statuscode {}, failed : {}",
                                                response.getStatusCode(), response);
                        } else {
                                Date endPush = new Date();
                                log.info("Push notification time - {}",
                                                endPush.getTime() - startPush.getTime());
                                log.info("pushNotification - statuscode {}, body{}",
                                                response.getStatusCode(),
                                                response.getBody());
                        }

                } catch (Exception ex) {
                        Date endPush = new Date();
                        log.info("Push notification time - {}", endPush.getTime() - startPush.getTime());
                        log.error("pushNotification - Error: {}", ex);
                }

                Date pushEnd = new Date();
                log.info("Push per notification - {} ", pushEnd.getTime() - pushStart.getTime());
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
                requestFactory.setConnectTimeout(15000);
                requestFactory.setReadTimeout(15000);

                return new RestTemplate(requestFactory);
        }

}
