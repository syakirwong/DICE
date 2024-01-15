// package com.alliance.dicecampaign.utility;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.kafka.annotation.KafkaListener;
// import org.springframework.stereotype.Component;

// import com.alliance.dicecampaign.service.CampaignService;
// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.JsonMappingException;

// import lombok.extern.slf4j.Slf4j;

// @Slf4j
// @Component
// public class KafkaListeners {

//     @Autowired
//     private CampaignService campaignService;
    
//     @KafkaListener(topics = "customer-profile",groupId = "groupId")
//     void listener(String data) throws JsonMappingException, JsonProcessingException {
//         log.info("Listener topic abmb received "+data);
//         log.info("Start recommendation process");

//         campaignService.testProcessRecommendation(data);
//     }

// }
