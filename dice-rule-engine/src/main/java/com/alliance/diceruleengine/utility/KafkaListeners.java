package com.alliance.diceruleengine.utility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.slf4j.Slf4j;

// @Slf4j
// @Component
// public class KafkaListeners {

//     // @Autowired
//     // private CampaignService campaignService;

//     @KafkaListener(topics = "customer-profile", groupId = "groupId")
//     void listener(String data) throws JsonMappingException, JsonProcessingException {
//         log.info("Listener topic abmb received " + data);
//         log.info("Start campaign manager process");

//         // campaignService.testProcessRecommendation(data);
//     }

// }
