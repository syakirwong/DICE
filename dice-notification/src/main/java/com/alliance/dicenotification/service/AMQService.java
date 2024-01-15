// package com.alliance.dicenotification.service;

// import lombok.extern.slf4j.Slf4j;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// @Service
// @Slf4j
// public class AMQService {

//     @Autowired
//     private ActiveMQService activeMQService;

//     public boolean sendMessage() {

//         try {
//             activeMQService.sendDummyProducerMessage();
//             return true;

//         } catch (Exception ex) {
//             log.error("Error Occur ; " + ex.getMessage());
//             return false;
//         }

//     }

// }
