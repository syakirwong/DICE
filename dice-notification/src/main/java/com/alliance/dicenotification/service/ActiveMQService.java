// package com.alliance.dicenotification.service;

// import lombok.extern.slf4j.Slf4j;

// import org.springframework.stereotype.Repository;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import com.alliance.dicenotification.utility.AMQConnection;
// import com.alliance.dicenotification.utility.SystemParam;

// import java.io.Serializable;
// import java.util.Date;

// import javax.jms.Connection;

// @Repository
// @Transactional
// @Service
// @Slf4j

// public class ActiveMQService {

//     public <T extends Serializable> void sendProducerMessage(String test) throws Exception {

//         Connection connection = null;
//         try {


//             System.out.println("test content = " +test);

//             Date startTime = new Date();
//             AMQConnection amq = new AMQConnection();
//             Date endTime = new Date();
//             System.out.println("AMQConnection define : " + (endTime.getTime() - startTime.getTime()));

//             startTime = new Date();
//             amq.sendMessage(SystemParam.getInstance().getTopic(), test);
//             endTime = new Date();
//             System.out.println("AMQConnection send Process : " + (endTime.getTime() - startTime.getTime()));

//             startTime = new Date();
//             amq.closeConnection();
//             endTime = new Date();
//             System.out.println("AMQConnection empty Closed : " + (endTime.getTime() - startTime.getTime()));

//             // msg.setObject((Serializable) baseLead);

//             // MessageProducer producer = session.createProducer(topic);

//             // producer.setDeliveryMode(DeliveryMode.PERSISTENT);
//             // producer.send(msg);
//             // Thread.sleep(1000);
//             // session.close();
//             // connection.close();

//         } finally {
//             Date startTime = new Date();
//             if (connection != null) {
//                 connection.close();
//             }
//             Date endTime = new Date();
//             System.out.println("AMQConnection another Closed : " + (endTime.getTime() - startTime.getTime()));

//         }
//     }

//     public <T extends Serializable> void sendDummyProducerMessage() {

        
//         try {
//             System.out.println("Topic sendDummyProducerMessage : " + SystemParam.getInstance().getTopic());
//             AMQConnection amq = new AMQConnection();
//             amq.sendMessage(SystemParam.getInstance().getTopic(), "test message");
//             amq.closeConnection();

//         } catch (Exception ex) {
//             log.error(" sendDummyProducerMessage Exception : " + ex.toString());

//         }
//     }

// }