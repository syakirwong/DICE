package com.alliance.diceintegration.service;


import com.alliance.diceintegration.request.AuditTrailRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class AuditTrailService {

    @Value("${create.audit.trail.endpointUrl}")
    private String CREATE_AUDIT_TRAIL_URL;

    public boolean saveAuditTrail(AuditTrailRequest request){
        RestTemplate template = getDefaultHttpTemplate();
        ResponseEntity<Object> response = template.postForEntity(CREATE_AUDIT_TRAIL_URL,request,Object.class);
        if ( response.getStatusCode().is2xxSuccessful() ){
            // log.info("Successfully Created Audit Trail for event {} ", request.getEvent());
            return true;
        }
        else{
            log.warn("Failed to Create Audit Trail for event {} ", request.getEvent());
            return false;
        }


    }

    private RestTemplate getDefaultHttpTemplate(){
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(15000);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        RestTemplate restTemplate = new RestTemplate(factory);
        return restTemplate;

    }
}
