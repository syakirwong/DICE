// package com.alliance.dicenotification.utility;

// import javax.jms.ConnectionFactory;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.jms.annotation.EnableJms;
// import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
// import org.springframework.jms.config.JmsListenerEndpoint;
// import org.springframework.jms.listener.SimpleMessageListenerContainer;
// import org.springframework.stereotype.Service;
// import org.springframework.util.ErrorHandler;
// import org.springframework.util.backoff.BackOff;
// import org.springframework.util.backoff.FixedBackOff;

// @Configuration
// @EnableJms
// public class MessagingListnerConfiguration {

//     @Bean
//     public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(SampleErrorHandler errorHandler) {
//         DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
//         BackOff backOff = new FixedBackOff();

//         factory.setConnectionFactory(ActiveMQPoolsUtil.getConnectionFactory());
//         // factory.setConcurrency("5-10");
//         // factory.setMaxMessagesPerTask(10);
//         // factory.setAutoStartup(true);
//         // factory.setBackOff(backOff);
//         // factory.setRecoveryInterval(10000L);
//         // factory.setSessionTransacted(true);
//         // factory.setSubscriptionDurable(true);
//         // factory.createListenerContainer((JmsListenerEndpoint)
//         // messageListenerContainer());

//         // factory.setErrorHandler(errorHandler);

//         return factory;
//     }

//     @Service
//     public class SampleErrorHandler implements ErrorHandler {
//         @Override
//         public void handleError(Throwable t) {

//             System.out.println("SampleErrorHandler : " + t.getMessage());
//             // handle exception here
//         }
//     }

//     // @Bean
//     // public SimpleMessageListenerContainer messageListenerContainer() {

//     // SimpleMessageListenerContainer container = new
//     // SimpleMessageListenerContainer();

//     // AMQExceptionListener amqExceptionListener = new AMQExceptionListener();
//     // container.setExceptionListener(amqExceptionListener);
//     // container.setMessageListener(new
//     // ConsumerMessageListener(SystemParam.getInstance().getAmqConsumer()));
//     // container.setConnectionFactory(ActiveMQPoolsUtil.getConnectionFactory());

//     // container.start();

//     // return container;
//     // }
// }
