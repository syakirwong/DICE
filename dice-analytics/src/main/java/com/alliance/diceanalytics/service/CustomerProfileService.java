package com.alliance.diceanalytics.service;

import com.alliance.diceanalytics.request.ProfileRequest;
import com.alliance.diceanalytics.response.CustomerProfileEFormResponse;
import com.alliance.diceanalytics.response.CustomerProfileResponse;
import com.alliance.diceanalytics.response.PLoanCustomerProfileResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Service
@Slf4j
public class CustomerProfileService {


    @Value("${check.profile.api}")
    private String CUSTOMER_PROFILE_API;

    private ObjectMapper mapper = new ObjectMapper();


    public CustomerProfileResponse checkCustomerProfile(String cifNo) {
        if (cifNo == null)
            return new CustomerProfileResponse();

        String data=  getCustomerProfile(new ProfileRequest("cif_no",cifNo,"EVT_SOLE_CC_VIEW"));

        try {
            return mapper.readValue(data,CustomerProfileResponse.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return new CustomerProfileResponse();
    }

    public CustomerProfileEFormResponse getCustomerProfileEformID(String referenceId) throws JSONException, IOException {
        if (referenceId == null)
            return new CustomerProfileEFormResponse();

        String data=getCustomerProfile(
                new ProfileRequest("UUID",referenceId,"EVT_INTERNET_BANKING_ACTIVATION_VIEW")
        );


        try {
            return mapper.readValue(data,CustomerProfileEFormResponse.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }


        return new CustomerProfileEFormResponse();

    }

    public PLoanCustomerProfileResponse getPLoanCustomerProfileByDevice(String deviceID) {
        if (deviceID == null)
            return new PLoanCustomerProfileResponse();

       String data = getCustomerProfile(
               new ProfileRequest("DEVICE_UUID",deviceID,"EVT_PLOAN_APPLICATION_VIEW")
       );

        try {
            return mapper.readValue(data,PLoanCustomerProfileResponse.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return new PLoanCustomerProfileResponse();

    }

    private RestTemplate getDefaultHttpTemplate() {
        try {

            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
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
            return new RestTemplate(requestFactory);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        return null;

    }

    private String getCustomerProfile(ProfileRequest profileRequest) {
        RestTemplate restTemplate = getDefaultHttpTemplate();
        ResponseEntity<String> response = null;


        ProfileRequest request = new ProfileRequest(
                profileRequest.getIdType(),
                profileRequest.getIdValue(),
                profileRequest.getTableName()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ProfileRequest> entity = new HttpEntity<>(request, headers);


        try {
            response = restTemplate.postForEntity(
                    CUSTOMER_PROFILE_API,
                    entity,
                    String.class
            );

            if (response.getBody() != null && !response.getBody().isEmpty()) {
                return new JSONObject(response.getBody()).getJSONObject("data").toString();
            }


        } catch (HttpClientErrorException e){
            log.error("Profile is Not Found");

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return "{}";

    }
}