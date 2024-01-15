package com.alliance.diceintegration.service;

import java.lang.reflect.Field;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.alliance.diceintegration.constant.ApiResponse;
import com.alliance.diceintegration.request.ProfileRequest;
import com.alliance.diceintegration.response.CacheCustomerProfileSoleCCResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CacheService {

    @Value("${dice.cache.get.profile.endpointURL}")
    private String DICE_CACHE_GET_PROFILE_ENDPOINT_URL;
    
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
}
