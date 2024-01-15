// package com.alliance.dicenotification.utility;

// import java.util.Date;

// import javax.jms.Connection;
// import javax.jms.JMSException;
// import org.springframework.beans.factory.annotation.Value;
// import org.apache.activemq.ActiveMQConnectionFactory;
// import org.apache.activemq.pool.PooledConnectionFactory;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import lombok.extern.slf4j.Slf4j;

// @Slf4j
// public class ActiveMQPoolsUtil {

// 	private static PooledConnectionFactory pooledConnectionFactory = null;
// 	private static ActiveMQConnectionFactory connectionFactory = null;
// 	private static Thread minConnectionThread = new Thread();
// 	private static Integer count = 0;

// 	private static Date refreshDate = new Date();

// 	@Value("${activemq.pooledConnectionFactory}")
// 	private static String pooledConnectionFactoryConfig;

// 	@Value("${activemq.service.ip}")
// 	private static String ACTIVEMQ_BOCKER_IP;

// 	public static ActiveMQConnectionFactory getConnectionFactory() {
// 		return connectionFactory;
// 	}

// 	public static int getNumConnections() {
// 		return pooledConnectionFactory.getNumConnections();
// 	}

// 	public static void init() throws Exception {
// 		// logger.debug(LogUtil.l("[ActiveMQPoolsUtil] : [init]", new String[0]));
// 		// logger.debug(LogUtil.l("[ActiveMQPoolsUtil] :
// 		// [connectionFactory="+connectionFactory+"]", new String[0]));
// 		// logger.debug(LogUtil.l("[ActiveMQPoolsUtil] :
// 		// [pooledConnectionFactory="+pooledConnectionFactory+"]", new String[0]));
// 		System.out.println("Initializing");
// 		boolean isInit = isRefresh();
// 		if (isInit) {
// 			// Initiate DB connections object prior access to system parameters
// 			// BeanFactoryUtilCMv2.getBean( "mySQLCMv2" );

// 			if (connectionFactory == null) {
// 				connectionFactory = new ActiveMQConnectionFactory();
// 			}

// 			// logger.debug(LogUtil.l("[ActiveMQPoolsUtil] :
// 			// [connectionFactory="+connectionFactory+"]", new String[0]));
// 			System.out.println("[ActiveMQPoolsUtil] : [connectionFactory=" + connectionFactory + "]");
// 			// logger.debug(LogUtil.l("[ActiveMQPoolsUtil] :
// 			// [PropertiesUtil.systemParam().getAmqUrl()="+PropertiesUtil.systemParam().getAmqUrl()+"]",
// 			// new String[0]));
// 			// System.out.println("[ActiveMQPoolsUtil] :
// 			// [PropertiesUtil.systemParam().getAmqUrl()="+"tcp://192.168.0.121:61616"+"]");

// 			System.out.println("username=" + SystemParam.getInstance().getAmqUsername());
// 			System.out.println("password=" + SystemParam.getInstance().getAmqPassword());
// 			System.out.println("broker url = " + SystemParam.getInstance().getBrokerUrl());
// 			System.out.println("pooled factory name = " + SystemParam.getInstance().getPooledConnectionFactory());
// 			System.out.println("getMaxConnection=" + Integer.parseInt(SystemParam.getInstance().getMaxConnection()));
// 			System.out.println("getMinConnection=" + Integer.parseInt(SystemParam.getInstance().getMinConnection()));
// 			System.out.println("getMaxThread=" + Integer.parseInt(SystemParam.getInstance().getMaxThreadPoolSize()));

// 			// connectionFactory.setBrokerURL(ACTIVEMQ_BOCKER_IP);
// 			connectionFactory.setBrokerURL(SystemParam.getInstance().getBrokerUrl());
// 			connectionFactory.setUserName(SystemParam.getInstance().getAmqUsername()); // hardcode
// 			connectionFactory.setPassword(SystemParam.getInstance().getAmqPassword()); // hardcode

// 			// connectionFactory.setAlwaysSessionAsync(true);//When true a separate thread
// 			// is used for dispatching messages for each Session in the Connection. A
// 			// separate thread is always used when there's more than one session, or the
// 			// session isn't in Session.AUTO_ACKNOWLEDGE or Session.DUPS_OK_ACKNOWLEDGE
// 			// mode.
// 			// connectionFactory.setAlwaysSyncSend(false);//When true a MessageProducer will
// 			// always use Sync sends when sending a Message even if it is not required for
// 			// the Delivery Mode.
// 			// connectionFactory.setAuditDepth(2048);//The size of the message window that
// 			// will be audited for duplicates and out of order messages.
// 			// connectionFactory.setAuditMaximumProducerNumber(64);//Maximum number of
// 			// producers that will be audited.
// 			connectionFactory.setCheckForDuplicates(true);// When true the consumer will check for duplicate messages
// 															// and properly handle the message to make sure that it is
// 															// not processed twice inadvertently.
// 			// connectionFactory.setClientID("");//Sets the JMS clientID to use for the
// 			// connection.
// 			connectionFactory.setCloseTimeout(3000);// Sets the timeout, in milliseconds, before a close is considered
// 													// complete. Normally a close() on a connection waits for
// 													// confirmation from the broker. This allows the close operation to
// 													// timeout preventing the client from hanging when no broker is
// 													// available.
// 			connectionFactory.setConsumerExpiryCheckEnabled(true);// Controls whether message expiration checking is
// 																	// done in each MessageConsumer prior to dispatching
// 																	// a message. Disabling this check can lead to
// 																	// consumption of expired messages. (since 5.11).
// 			// connectionFactory.setConsumerFailoverRedeliveryWaitPeriod(3000);
// 			connectionFactory.setCopyMessageOnSend(false);// Should a JMS message be copied to a new JMS Message object
// 															// as part of the send() method in JMS. This is enabled by
// 															// default to be compliant with the JMS specification. For a
// 															// performance boost set to false if you do not mutate JMS
// 															// messages after they are sent.
// 			connectionFactory.setDisableTimeStampsByDefault(true);// Sets whether or not timestamps on messages should
// 																	// be disabled or not. For a small performance boost
// 																	// set to true.
// 			connectionFactory.setDispatchAsync(true);// Should the broker dispatch messages asynchronously to the
// 														// consumer?
// 			connectionFactory.setExclusiveConsumer(true);// Use a queue to maintain the queue sequence info to make sure
// 															// message queue in sequence
// 			connectionFactory.setMaxThreadPoolSize(Integer.parseInt(SystemParam.getInstance().getMaxThreadPoolSize()));// max
// 																														// thread
// 																														// limit
// 																														// a
// 																														// session
// 																														// can
// 																														// use
// 			connectionFactory.setMessagePrioritySupported(true);// Support message priority arrangement
// 			connectionFactory.setNestedMapAndListEnabled(true);// Controls whether Structured Message Properties and
// 																// MapMessages are supported so that Message properties
// 																// and MapMessage entries can contain nested Map and
// 																// List objects. Available from version 4.1.
// 			connectionFactory.setNonBlockingRedelivery(false);// Not block even a message rollback and required
// 																// re-deliver
// 			connectionFactory.setObjectMessageSerializationDefered(false);// When an object is set on an ObjectMessage
// 																			// the JMS spec requires the object be
// 																			// serialized by that set method. When true
// 																			// the object will not be serialized. The
// 																			// object may subsequently be serialized if
// 																			// the message needs to be sent over a
// 																			// socket or stored to disk.
// 			connectionFactory.setOptimizeAcknowledge(false);// Enables an optimized acknowledgement mode where messages
// 															// are acknowledged in batches rather than individually.
// 															// Alternatively, you could use Session.DUPS_OK_ACKNOWLEDGE
// 															// acknowledgement mode for the consumers which can often be
// 															// faster. WARNING: enabling this issue could cause some
// 															// issues with auto-acknowledgement on reconnection.
// 			connectionFactory.setOptimizeAcknowledgeTimeOut(300);// If > 0, specifies the max time, in milliseconds,
// 																	// between batch acknowledgements when
// 																	// optimizeAcknowledge is enabled. (since 5.6).
// 			connectionFactory.setOptimizedAckScheduledAckInterval(0);// If > 0, specifies a time interval upon which all
// 																		// the outstanding ACKs are delivered when
// 																		// optimized acknowledge is used so that a long
// 																		// running consumer that doesn't receive any
// 																		// more messages will eventually ACK the last
// 																		// few un-ACK'ed messages (since 5.7).
// 			connectionFactory.setOptimizedMessageDispatch(true);// If true a larger prefetch limit is used - only
// 																// applicable for durable topic subscribers.
// 			connectionFactory.setProducerWindowSize(1);// Pre-fetches messages into a buffer on each consumer. The total
// 														// maximum size of messages (in bytes) that will be buffered on
// 														// each consumer is determined by the consumerWindowSize
// 														// parameter. 1 - Fast Consumer - This will allow unbounded
// 														// message buffering on the client side. 0 - No buffer at all,
// 														// this will prevent the slow consumer from buffering any
// 														// messages on the client side
// 			// connectionFactory.setSendAcksAsync(true);
// 			// connectionFactory.setSendTimeout(3);//Time to wait on Message Sends for a
// 			// Response, default value of zero indicates to wait forever. Waiting forever
// 			// allows the broker to have flow control over messages coming from this client
// 			// if it is a fast producer or there is no consumer such that the broker would
// 			// run out of memory if it did not slow down the producer. Does not affect Stomp
// 			// clients as the sends are ack'd by the broker. (Since ActiveMQ-CPP 2.2.1)
// 			connectionFactory.setStatsEnabled(false);// For retrieve connection statistics
// 			connectionFactory.setTransactedIndividualAck(true);// when true, submit individual transacted acks
// 																// immediately rather than with transaction completion.
// 			connectionFactory.setUseAsyncSend(true);// Forces the use of Async Sends which adds a massive performance
// 													// boost; but means that the send() method will return immediately
// 													// whether the message has been sent or not which could lead to
// 													// message loss.
// 			connectionFactory.setUseCompression(false);// Enables the use of compression on the message's body. Suitable
// 														// for large message body but increase CPU usage
// 			connectionFactory.setUseDedicatedTaskRunner(false);// should be set to false so that the task runner
// 																// actually pools the threads
// 			connectionFactory.setUseRetroactiveConsumer(false);// Sets whether or not retroactive consumers are enabled.
// 																// Retroactive consumers allow non-durable topic
// 																// subscribers to receive old messages that were
// 																// published before the non-durable subscriber started.
// 			connectionFactory.setWarnAboutUnstartedConnectionTimeout(500);// The timeout, in milliseconds, from the time
// 																			// of connection creation to when a warning
// 																			// is generated if the connection is not
// 																			// properly started via Connection.start()
// 																			// and a message is received by a consumer.
// 																			// It is a very common gotcha to forget to
// 																			// start the connection and then wonder why
// 																			// no messages are delivered so this option
// 																			// makes the default case to create a
// 																			// warning if the user forgets. To disable
// 																			// the warning just set the value to < 0.
// 			connectionFactory.setWatchTopicAdvisories(false);
// 			connectionFactory.setTrustAllPackages(true);
// 			if (SystemParam.getInstance().getPooledConnectionFactory()
// 					.equalsIgnoreCase(PooledConnectionFactory.class.getSimpleName())) {
// 				// Pool Connection Factory
// 				pooledConnectionFactory = new PooledConnectionFactory(connectionFactory);
// 				pooledConnectionFactory.setBlockIfSessionPoolIsFull(true); // If enabled, block createSession() until
// 																			// a
// 																			// session becomes available in the
// 																			// pool. It
// 																			// is enabled by default.
// 				pooledConnectionFactory.setBlockIfSessionPoolIsFullTimeout(-1);// The time in milliseconds before a
// 																				// blocked call to createSession()
// 																				// throws an IllegalStateException. The
// 																				// default is -1, meaning the call
// 																				// blocks forever.
// 				pooledConnectionFactory.setCreateConnectionOnStartup(true);
// 				pooledConnectionFactory.setExpiryTimeout(0);// Allow connections to expire, irrespective of load or idle
// 															// time. This is useful with failover to force a reconnect
// 															// from the pool, to reestablish load balancing or use of
// 															// the master post recovery
// 				pooledConnectionFactory.setIdleTimeout(0); // The time in seconds before a connection not currently on
// 															// loan can be evicted from the pool. The default is 30
// 															// seconds. A value of 0 disables the timeout.
// 				pooledConnectionFactory
// 						.setMaxConnections(Integer.parseInt(SystemParam.getInstance().getMaxConnection())); // The
// 																											// maximum
// 																											// number
// 																											// oconnections
// 																											// for
// 																											// asingle
// 																											// pool.The
// 																											// default
// 																											// is 1.
// 				pooledConnectionFactory.setMaximumActiveSessionPerConnection(500);// The maximum number of sessions for
// 																					// each connection. The default is
// 																					// 500. A negative value removes any
// 																					// limit.
// 				pooledConnectionFactory.setReconnectOnException(true); // Controls weather the underlying connection
// 																		// should be reset (and renewed) on JMSException
// 				pooledConnectionFactory.setTimeBetweenExpirationCheckMillis(200000);// The time in milliseconds between
// 																					// periodic checks for expired
// 																					// connections. The default is 0,
// 																					// meaning the check is disabled.
// 				pooledConnectionFactory.setUseAnonymousProducers(false);// If enabled, use a single anonymous JMS
// 																		// MessageProducer for all calls to
// 																		// createProducer(). It is enabled by default.
// 				pooledConnectionFactory.clear();

// 				pooledConnectionFactory.start();// warm up by create a connection based on CreateOnStartup indicator
// 				// pooledConnectionFactory.stop();
// 			} else {// Unknown Connection Factory
// 					// logger.debug(LogUtil.l("[ActiveMQPoolsUtil] : [Refresh] : [No
// 					// PooledConnectionFactory]", new String[0]));
// 				System.out.println("[ActiveMQPoolsUtil] : [Refresh] : [No PooledConnectionFactory]");
// 			}

// 			// logger.debug(LogUtil.l("[ActiveMQPoolsUtil] : [Refresh] :
// 			// [connectionFactory="+connectionFactory+"]", new String[0]));
// 			// logger.debug(LogUtil.l("[ActiveMQPoolsUtil] : [Refresh] :
// 			// [pooledConnectionFactory="+pooledConnectionFactory+"]", new String[0]));
// 			System.out.println("[ActiveMQPoolsUtil] : [Refresh] : [connectionFactory=" + connectionFactory + "]");
// 			System.out.println(
// 					"[ActiveMQPoolsUtil] : [Refresh] : [pooledConnectionFactory=" + pooledConnectionFactory + "]");

// 			// // Clear messages on startup, if needed
// 			// if(Boolean.valueOf(PropertiesUtil.amqPool().getClearMessagesOnStartup())) {
// 			// new ActiveMqHelper().removeAllMessages();
// 			// }

// 			refreshDate = new Date();

// 			// logger.debug(LogUtil.l("[ActiveMQPoolsUtil] : [init] :
// 			// [refreshDate="+refreshDate+"]", new String[0]));
// 			System.out.println("[ActiveMQPoolsUtil] : [init] : [refreshDate=" + refreshDate + "]");
// 		}

// 		initMinConnections(isInit);
// 	}

// 	public static void initMinConnections(boolean isInit) throws JMSException {
// 		synchronized (minConnectionThread) {
// 			System.out.println("[ActiveMQPoolsUtil] : pooledConnectionFactory.getNumConnections():"
// 					+ pooledConnectionFactory.getNumConnections() + " => Count :" + count);
// 			count += 1;
// 			if (pooledConnectionFactory.getNumConnections() < Integer
// 					.parseInt(SystemParam.getInstance().getMinConnection())) {
// 				// logger.debug(LogUtil.l("[ActiveMQPoolsUtil] : [initMinConnections] :
// 				// [isInit="+isInit+"]", new String[0]));
// 				// logger.debug(LogUtil.l("[ActiveMQPoolsUtil] : [initMinConnections] :
// 				// [minConnectionThread.isAlive()="+minConnectionThread.isAlive()+"]", new
// 				// String[0]));
// 				if (isInit) {// Create immediately during init, else only use thread to maintain
// 					createMinConnections();
// 					// logger.debug(LogUtil.l("[ActiveMQPoolsUtil] : [initMinConnections] : [Done
// 					// CreateMinConnections()]", new String[0]));
// 				} else if (!minConnectionThread.isAlive()) {
// 					minConnectionThread = new Thread() {
// 						public void run() {
// 							try {
// 								// logger.debug(LogUtil.l("[ActiveMQPoolsUtil] : [initMinConnections] :
// 								// [pooledConnectionFactory.getNumConnections()="+pooledConnectionFactory.getNumConnections()+"]",
// 								// new String[0]));
// 								System.out.println(
// 										"[ActiveMQPoolsUtil] : [initMinConnections] : [pooledConnectionFactory.getNumConnections()="
// 												+ pooledConnectionFactory.getNumConnections() + "]");
// 								createMinConnections();

// 								sleep(1000);
// 							} catch (JMSException e) {
// 								e.printStackTrace();
// 								// logger.debug(LogUtil.l("[ActiveMQPoolsUtil] : [initMinConnections] :
// 								// [JMSException="+e.getMessage()+"]", new String[0]));
// 								System.out.println("[ActiveMQPoolsUtil] : [initMinConnections] : [JMSException="
// 										+ e.getMessage() + "]");
// 							} catch (InterruptedException e) {
// 								// logger.debug(LogUtil.l("[ActiveMQPoolsUtil] : [initMinConnections] :
// 								// [InterruptException="+e.getMessage()+"]", new String[0]));
// 								System.out.println("[ActiveMQPoolsUtil] : [initMinConnections] : [InterruptException="
// 										+ e.getMessage() + "]");
// 							}
// 						}
// 					};
// 					minConnectionThread.start();
// 				}
// 				// logger.debug(LogUtil.l("[ActiveMQPoolsUtil] : [initMinConnections] :
// 				// [initMinConnections thread="+minConnectionThread.getId()+"]", new
// 				// String[0]));
// 				System.out.println("[ActiveMQPoolsUtil] : [initMinConnections] : [initMinConnections thread="
// 						+ minConnectionThread.getId() + "]");
// 			}
// 		}
// 	}

// 	public static void createMinConnections() throws JMSException {
// 		for (int i = pooledConnectionFactory.getNumConnections(); i <= Integer
// 				.parseInt(SystemParam.getInstance().getMinConnection()); i++) {
// 			Connection aConnection = getConnection();
// 			// logger.debug(LogUtil.l("[ActiveMQPoolsUtil] : [createMinConnections] :
// 			// [Created amq connection ("+i+")="+aConnection+"]", new String[0]));
// 		}
// 	}

// 	public static boolean isRefresh() {
// 		boolean loadObj = false;

// 		if (connectionFactory == null || pooledConnectionFactory == null) {
// 			loadObj = true;
// 		} /*
// 			 * else {
// 			 * Date current = new Date();
// 			 * if ((((current.getTime()-refreshDate.getTime())/1000)/60) >
// 			 * CACHE_TIME_MINUTE) {
// 			 * loadObj = true;
// 			 * }
// 			 * }
// 			 */
// 		return loadObj;
// 	}

// 	public static void close() throws JMSException {
// 		if (pooledConnectionFactory != null) {
// 			pooledConnectionFactory.clear();
// 			pooledConnectionFactory.stop();
// 		}

// 		/*
// 		 * Should not close connection but should close connection pool
// 		 * if(connection != null) {
// 		 * connection.stop();
// 		 * connection.close();
// 		 * connectionClose = true;
// 		 * }
// 		 */
// 	}

// 	public static Connection getConnection() throws JMSException {
// 		Connection connection = null;

// 		if (pooledConnectionFactory != null) {
// 			connection = pooledConnectionFactory.createConnection();
// 		} else {
// 			connection = connectionFactory.createConnection();
// 		}

// 		connection.start();
// 		// connectionClose = false;
// 		return connection;
// 	}

// }
