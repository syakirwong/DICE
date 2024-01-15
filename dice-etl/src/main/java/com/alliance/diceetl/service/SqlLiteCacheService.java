package com.alliance.diceetl.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@Service
public class SqlLiteCacheService {

//    @Value("${sqlite.service.endpointUrl}")
    private String SQLITE_SERVICE_ENDPOINTURL = System.getenv("sqlite.service.endpointUrl");

//    private String testUrl = System.getenv("sqlite.service.endpointUrl");

    private RestTemplate getDefaultHttpTemplate(){
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(15000);
        return new RestTemplate(factory);
    }

    public String startEvent(String eventType) {
        URI uri = UriComponentsBuilder.fromHttpUrl(SQLITE_SERVICE_ENDPOINTURL)
                .queryParam("source", eventType)
                .build()
                .toUri();

        ResponseEntity<String> response = getDefaultHttpTemplate().postForEntity(uri, null, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            return "Error: " + response.getStatusCode();
        }
    }


}
