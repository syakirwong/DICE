// package com.alliance.dicenotification.utility;

// import java.io.Serializable;
// import java.util.ArrayList;
// import java.util.Date;
// import java.util.List;
// import java.util.Random;

// import javax.jms.Connection;
// import javax.jms.DeliveryMode;
// import javax.jms.ExceptionListener;
// import javax.jms.JMSException;
// import javax.jms.Message;
// import javax.jms.MessageConsumer;
// import javax.jms.MessageProducer;
// import javax.jms.MessageListener;
// import javax.jms.ObjectMessage;
// import javax.jms.Queue;
// import javax.jms.ServerSessionPool;
// import javax.jms.Session;
// import javax.jms.Topic;

// import org.apache.activemq.jms.pool.PooledProducer;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

// public class AMQConnection {
// 	private int messageTimeout = 500;

// 	public AMQConnection() {
// 		try {
// 			System.out.println("[ActiveMQPoolsUtil] : pooledConnectionFactory.getNumConnections() :"
// 					+ ActiveMQPoolsUtil.getNumConnections());
// 		} catch (Exception e) {
// 			e.printStackTrace();
// 		}
// 	}

// 	public void closeConnection() {

// 	}

// 	public void setMessageListener(String topic, MessageListener listener) {
// 		setMessageListener(topic, listener, false);
// 	}

// 	public void setMessageListener(String topicName, MessageListener listener, boolean transacted) {
// 		Connection connection = null;

// 		try {
// 			// ActiveMQPoolsUtil.init(logger);
// 			System.out.println("[AMQConnection] : [getMessage] : [Get Pooled Connection]");
// 			Date startTime = new Date();
// 			connection = ActiveMQPoolsUtil.getConnection();// Slow 30s
// 			Date endTime = new Date();
// 			System.out.println("[AMQConnection] : [getMessage] : [Pooled Connection] [Time Taken (ms)="
// 					+ (endTime.getTime() - startTime.getTime()) + "]");
// 			System.out.println("[AMQConnection] : [getMessage] : [Pooled Connection=" + connection + "]");
// 			startTime = new Date();
// 			Session session = connection.createSession(transacted, Session.AUTO_ACKNOWLEDGE);
// 			endTime = new Date();
// 			// Queue destination = session.createQueue(queue);
// 			Topic topic = session.createTopic(topicName);
// 			startTime = new Date();
// 			// QueueBrowser browser = session.createBrowser(destination);
// 			MessageConsumer consumer = session.createConsumer(topic);// Slow 3s
// 			endTime = new Date();

// 			consumer.setMessageListener(listener);
// 			// connection.start(); // originally started very early

// 			// consumer.close(); // consumer side cannot close
// 			// session.close(); // consumer side cannot close
// 			// connection.close(); // consumer side cannot close
// 		} catch (Exception e) {
// 			// logger.error(LogUtil.l("[AMQConnection] : [getMessage] : [EXCEPTION] , ",
// 			// threadId), e);
// 			e.printStackTrace();
// 		}
// 	}

// 	public <T extends Serializable> void sendDelayMessage(String topicName, T messageAMQ, long seconds) {
// 		send(topicName, messageAMQ, seconds);
// 	}

// 	public <T extends Serializable> void sendMessage(String topicName, T messageAMQ) {
// 		send(topicName, messageAMQ, 0);
// 	}

// 	public <T extends Serializable> void sendMessage(String topicName, List<T> messageAMQ) {
// 		for (T message : messageAMQ) {
// 			send(topicName, message, 0);
// 		}
// 	}

// 	private <T extends Serializable> void send(String topicName, T messageAMQ, long delayInSecond) {
// 		Connection connection = null;
// 		// ProducerModel messageProducer=null;
// 		try {
// 			/*
// 			 * Create producer in memory, problem is not renewed upon connection
// 			 * expired/renewed
// 			 * logger.debug(LogUtil.l("[AMQConnection] : [send] : [Find Producer]", id));
// 			 * messageProducer = findInList(queue);
// 			 * logger.debug(LogUtil.l("[AMQConnection] : [send] : [Producer="
// 			 * +messageProducer+"]", id));
// 			 * 
// 			 * if(messageProducer == null || messageProducer.getConnection().getConnection()
// 			 * == null) {
// 			 * logger.debug(LogUtil.l("[AMQConnection] : [send] : [Create Producer]", id));
// 			 * messageProducer = createProducer(queue, logger);
// 			 * logger.debug(LogUtil.l("[AMQConnection] : [send] : [Created Producer="
// 			 * +messageProducer+"]", id));
// 			 * }
// 			 */

// 			System.out.println("[AMQTopic] : [Topic] :" + SystemParam.getInstance().getTopic());

// 			// logger.debug(LogUtil.l("[AMQConnection] : [send] : [Get Pooled Connection]",
// 			// threadId));
// 			Date startTime = new Date();
// 			connection = ActiveMQPoolsUtil.getConnection();// Slow 30s
// 			Date endTime = new Date();
// 			// logger.debug(LogUtil.l("[AMQConnection] : [send] : [Pooled Connection] [Time
// 			// Taken (ms)="+(endTime.getTime()-startTime.getTime())+"]", threadId));
// 			// logger.debug(LogUtil.l("[AMQConnection] : [send] : [Pooled
// 			// Connection="+connection+"]", threadId));
// 			System.out.println("[AMQConnection] : [send] : [Pooled Connection] [Time Taken (ms)="
// 					+ (endTime.getTime() - startTime.getTime()) + "]");
// 			System.out.println("[AMQConnection] : [send] : [Pooled Connection=" + connection + "]");

// 			// logger.debug(LogUtil.l("[AMQConnection] : [send] : [Create Session]",
// 			// threadId));
// 			startTime = new Date();
// 			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
// 			endTime = new Date();

// 			System.out.println("[AMQSession ] : [send] : [Create Pooled Session] [Time Taken (ms)="
// 					+ (endTime.getTime() - startTime.getTime()) + "]");
// 			// System.out.println("[AMQSession ] : [send] : [Create Pooled Session=" +
// 			// session + "]");

// 			// logger.debug(LogUtil.l("[AMQConnection] : [send] : [Pooled Session] [Time
// 			// Taken (ms)="+(endTime.getTime()-startTime.getTime())+"]", threadId));
// 			// logger.debug(LogUtil.l("[AMQConnection] : [send] : [Session="+session+"]",
// 			// threadId));

// 			// logger.debug(LogUtil.l("[AMQConnection] : [send] : [Create Queue
// 			// Destination]", threadId));
// 			// Queue destination = session.createQueue(queue); // currently Alliance project
// 			// uses topic rather than queue
// 			startTime = new Date();
// 			Topic topic = session.createTopic(topicName);
// 			// Queue destination = session.createQueue(topicName);
// 			endTime = new Date();

// 			System.out.println("[AMQSession ] : [send] : [Create Topic] [Time Taken (ms)="
// 					+ (endTime.getTime() - startTime.getTime()) + "]");
// 			// System.out.println("[AMQSession ] : [send] : [Create Topic=" + topic + "]");

// 			// logger.debug(LogUtil.l("[AMQConnection] : [send] : [Queue
// 			// Destination="+destination+"]", threadId));
// 			// connection.createDurableConnectionConsumer(topic, "DefaultTopic", topicName,
// 			// null, 0);

// 			// logger.debug(LogUtil.l("[AMQConnection] : [send] : [Create Message]",
// 			// threadId));
// 			// ObjectMessage message = messageProducer.getMessage();
// 			// ObjectMessage message = messageProducer.getSession().createObjectMessage();
// 			startTime = new Date();
// 			ObjectMessage message = session.createObjectMessage();
// 			endTime = new Date();

// 			System.out.println("[AMQSession ] : [send] : [Create ObjectMessage] [Time Taken (ms)="
// 					+ (endTime.getTime() - startTime.getTime()) + "]");
// 			// System.out.println("[AMQSession ] : [send] : [Create ObjectMessage=" +
// 			// message + "]");
// 			// logger.debug(LogUtil.l("[AMQConnection] : [send] : [Message="+message+"]",
// 			// threadId));
// 			startTime = new Date();
// 			if (delayInSecond > 0) // Set delay in sending for scheduling message
// 				message.setLongProperty("_AMQ_SCHED_DELIVERY", System.currentTimeMillis() + (delayInSecond * 1000));

// 			message.setObject(messageAMQ);
// 			endTime = new Date();
// 			System.out.println("[AMQSetObject] : [send] : [Create SetObject] [Time Taken (ms)="
// 					+ (endTime.getTime() - startTime.getTime()) + "]");
// 			// System.out.println("[AMQSetObject ] : [send] : [Create SetObject=" + message
// 			// + "]");

// 			// logger.debug(LogUtil.l("[AMQConnection] : [send] : [Create Producer]",
// 			// threadId));
// 			startTime = new Date();
// 			// MessageProducer producer = messageProducer.getProducer();

// 			MessageProducer producer = session.createProducer(topic);
// 			// PooledProducer producer = new PooledProducer(session.createProducer(topic),
// 			// topic);
// 			endTime = new Date();

// 			System.out.println("[AMQMessageProducer] : [send] : [Create MessageProducer] [Time Taken (ms)="
// 					+ (endTime.getTime() - startTime.getTime()) + "]");

// 			startTime = new Date();
// 			producer.setDeliveryMode(DeliveryMode.PERSISTENT);// Default, the message broker will always persist it
// 																// to
// 																// its message store before dispatching it to a
// 																// consumer
// 																// producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);//
// 																// Non persistent mode

// 			// for improve performance
// 			endTime = new Date();
// 			System.out.println("[AMQMessageProducer] : [send] : [Create setDeliveryMode] [Time Taken (ms)="
// 					+ (endTime.getTime() - startTime.getTime()) + "]");

// 			// System.out.println("[AMQMessageProducer ] : [send] : [Create
// 			// MessageProducer=" + producer + "]");

// 			// logger.debug(LogUtil.l("[AMQConnection] : [send] : [Pooled Producer] [Time
// 			// Taken (ms)="+(endTime.getTime()-startTime.getTime())+"]", threadId));
// 			// logger.debug(LogUtil.l("[AMQConnection] : [send] : [Producer="+producer+"]",
// 			// threadId));
// 			startTime = new Date();
// 			// Performance tuning
// 			producer.setDisableMessageID(true);// Disable if not needed to decreases the size of the message and also
// 												// avoids the overhead of creating a unique ID
// 			producer.setDisableMessageTimestamp(true);// disable message timestamps if don't need them
// 			// producer.setTimeToLive(5000);
// 			// Send message
// 			// logger.debug(LogUtil.l("[AMQConnection] : [send] : [Send Message]",
// 			// threadId)
// 			producer.send(message);
// 			endTime = new Date();

// 			System.out.println("[AMQProducer Send] : [send] : [Create Producer Send] [Time Taken (ms)="
// 					+ (endTime.getTime() - startTime.getTime()) + "]");
// 			// System.out.println("[AMQProducer Send] : [send] : [Create Producer Send=" +
// 			// producer + "]");
// 			// logger.debug(LogUtil.l("[AMQConnection] : [send] : [Done Send Message]",
// 			// threadId));
// 			// startTime = new Date();
// 			// session.commit();
// 			// endTime = new Date();
// 			// System.out.println("[AMQ session.commit()] : [send] : [Create
// 			// session.commit()] [Time Taken (ms)="
// 			// + (endTime.getTime() - startTime.getTime()) + "]");
// 			startTime = new Date();
// 			producer.close();
// 			session.close();
// 			connection.close();
// 			endTime = new Date();

// 			System.out.println("[AMQ Connection Closed] : [send] : [Create Connection Closed] [Time Taken (ms)="
// 					+ (endTime.getTime() - startTime.getTime()) + "]");
// 			// logger.debug(LogUtil.l("[AMQConnection] : [send] : [Connection closed]",
// 			// threadId));
// 			System.out.println("[AMQConnection] : [send] : [Connection closed]");
// 		} catch (Exception e) {
// 			System.out.println("Something went wrong during sending.");
// 			e.printStackTrace();
// 		} finally {
// 			Date startTime = new Date();
// 			try {
// 				// messageProducer.getConnection().close();
// 				if (connection != null)
// 					connection.close();
// 			} catch (JMSException e1) {
// 				e1.printStackTrace();
// 			}
// 			System.out.println("[AMQConnection] : [send] : [DONE]");
// 			Date endTime = new Date();
// 			System.out.println("[AMQ Connection Closed] : [send] : [Connection Closed] [Time Taken (ms)="
// 					+ (endTime.getTime() - startTime.getTime()) + "]");
// 		}
// 	}

// 	// public static void main(String args[]) {
// 	// System.out.println(AMQConnection.class.getSimpleName());

// 	// while(true) {
// 	// try {
// 	// ActiveMQPoolsUtil.init();

// 	// // Test Send Message
// 	// AMQConnection amq = new AMQConnection(workerId, logger);
// 	// BaseLead sendMessage = new BaseLead(lead.getLeadId(), lead.getCampaignId(),
// 	// lead.getExistingCustomerId(),
// 	// lead.getApplicationId(),
// 	// lead.getMyKadNo(), lead.getName(), lead.getMobileNumber(), lead.getEmail(),
// 	// lead.getDeviceId(),
// 	// lead.getUtmCampaignId(),
// 	// lead.getUtmCampaignSource(), lead.getUtmCampaignMedium(),
// 	// lead.getUtmCampaignName(),
// 	// lead.getEcId(), lead.getBannerId(),
// 	// lead.getAdvertisingId(), lead.getPreferredLanguage(),
// 	// lead.getLeadStatusId());
// 	// sendMessage.setVar1("first test");
// 	// amq.sendMessage(workerId, sendMessage);
// 	// System.out.println("sendMessage: "+sendMessage);
// 	// amq.closeConnection();

// 	// // Test Receive Message
// 	// amq = new AMQConnection(workerId, logger);
// 	// ObjectMessage receiveMessage = amq.getMessage(workerId);
// 	// if(receiveMessage != null && receiveMessage.getObject() instanceof
// 	// MessageAMQ) {
// 	// MessageAMQ msgObj = (MessageAMQ)receiveMessage.getObject();
// 	// System.out.println("msgObj: "+msgObj);
// 	// }
// 	// amq.closeConnection();

// 	// try {
// 	// // Thread.sleep(1000); // might have waited too long, disable for now
// 	// } catch (InterruptedException e) {
// 	// e.printStackTrace();
// 	// }
// 	// } catch (Exception e) {
// 	// e.printStackTrace();
// 	// }
// 	// }

// 	// }

// }
