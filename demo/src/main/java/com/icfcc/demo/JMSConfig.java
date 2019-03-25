package com.icfcc.demo;

import com.ibm.mq.jms.MQQueue;
import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpoint;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;
import org.springframework.jms.core.JmsOperations;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.transaction.PlatformTransactionManager;

import javax.jms.JMSException;
import javax.jms.MessageListener;

@Configuration
public class JMSConfig {

    @Value("${tax.mq.manager.host}")
    private String host;
    @Value("${tax.mq.port}")
    private Integer port;
    @Value("${tax.mq.manager.name}")
    private String queueManager;
    @Value("${tax.mq.channel}")
    private String channel;

    //@Value("${project.mq.username}")
    private String username;
    //@Value("${project.mq.password}")
    private String password;

//    @Value("${tax.mq.receive-timeout}")
//    private long receiveTimeout;

    @Value("${tax.mq.receiveQ.name}")
    private String receiverQueueName; //接收队列

    @Value("${tax.mq.sendQ.name}")
    private String senderQueueName;//发送队列
    @Value("${tax.mq.transportType}")
    private int transportType;
    //连接方式transportType不对 默认为0 远程调用要改成1(TCP)连接，
    /**
     * 创建工厂类
     *
     * @return
     */
    @Bean
    @Qualifier("mqQueueConnectionFactoryTax")
    public MQQueueConnectionFactory mqQueueConnectionFactoryTax() {

        MQQueueConnectionFactory mqQueueConnectionFactory = new MQQueueConnectionFactory();
        try {
            mqQueueConnectionFactory.setHostName(host);
            //端口
            mqQueueConnectionFactory.setPort(port);
            //队列
            mqQueueConnectionFactory.setQueueManager(queueManager);
            mqQueueConnectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT); //TCP访问远程队列
            //字符编码
            mqQueueConnectionFactory.setCCSID(819);
            //通道
            mqQueueConnectionFactory.setChannel(channel);
        } catch (JMSException e) {
            e.printStackTrace();
        }
        return mqQueueConnectionFactory;
    }

    /**
     * 设置用户信息
     *
     * @param mqQueueConnectionFactoryTax
     * @return
     */
    @Bean
    @Qualifier("userCredentialsConnectionFactoryAdapterTax")
    UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter(@Qualifier("mqQueueConnectionFactoryTax") MQQueueConnectionFactory mqQueueConnectionFactoryTax) {
        UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter = new UserCredentialsConnectionFactoryAdapter();
       // userCredentialsConnectionFactoryAdapter.setUsername(password);
       // userCredentialsConnectionFactoryAdapter.setPassword(username);

        userCredentialsConnectionFactoryAdapter.setTargetConnectionFactory(mqQueueConnectionFactoryTax);
        return userCredentialsConnectionFactoryAdapter;
    }

    /**
     * 设置缓存
     *
     * @param userCredentialsConnectionFactoryAdapter
     * @return
     */
    @Bean
    @Primary
    @Qualifier("cachingConnectionFactoryTax")
    public CachingConnectionFactory cachingConnectionFactoryTax(
            @Qualifier("userCredentialsConnectionFactoryAdapterTax")UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter) {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
//        cachingConnectionFactory.setTargetConnectionFactory(mqQueueConnectionFactory);
        cachingConnectionFactory.setTargetConnectionFactory(userCredentialsConnectionFactoryAdapter);

        cachingConnectionFactory.setSessionCacheSize(500);
        cachingConnectionFactory.setReconnectOnException(true);
        return cachingConnectionFactory;
    }

    /**
     * 设置消息事务管理器
     * @param
     * @return
     */
    @Bean
    @Qualifier("jmsTransactionManagerTax")
    PlatformTransactionManager jmsTransactionManagerTax(
            @Qualifier("cachingConnectionFactoryTax")CachingConnectionFactory cachingConnectionFactoryTax){
            //@Qualifier("mqQueueConnectionFactoryTax") MQQueueConnectionFactory mqQueueConnectionFactoryTax){
        JmsTransactionManager jmsTransactionManager = new JmsTransactionManager();
        jmsTransactionManager.setConnectionFactory(cachingConnectionFactoryTax);
        return  jmsTransactionManager;
    }

    @Bean
    @Primary
    @Qualifier("jmsOperationsTax")
    JmsOperations jmsOperationsTax(
            @Qualifier("mqQueueConnectionFactoryTax") MQQueueConnectionFactory mqQueueConnectionFactoryTax){
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory(mqQueueConnectionFactoryTax);
        jmsTemplate.setReceiveTimeout(500);
        return jmsTemplate;
    }

    /**
     * 注入监听
     *
     * @return
     */
    @Bean
    @Qualifier("jmsListenerContainerFactoryTax")
    public DefaultMessageListenerContainer jmsListenerContainerFactoryTax(
            //@Qualifier("mqQueueConnectionFactoryTax") MQQueueConnectionFactory mqQueueConnectionFactoryTax
            @Qualifier("cachingConnectionFactoryTax")CachingConnectionFactory cachingConnectionFactoryTax
            ,@Qualifier("receiverQueueTax")MQQueue receiverQueueTax
            ,@Qualifier("receiverMsgListenerTAX")MessageListener receiverMsgListenerTAX ) {
        DefaultMessageListenerContainer factory = new DefaultMessageListenerContainer();
        factory.setConnectionFactory(cachingConnectionFactoryTax);
//        factory.setTransactionManager(jmsTransactionManager(cachingConnectionFactory));
        factory.setDestination(receiverQueueTax);
        factory.setMessageListener(receiverMsgListenerTAX);
        factory.setConcurrentConsumers(20);
        factory.setSessionTransacted(true);
        factory.setPubSubDomain(false);
        return factory;
    }


    @Bean
    @Qualifier("receiverQueueTax")
    public MQQueue ReceiverQueueTax() {
        MQQueue receiverQueue = new MQQueue();
        try {
            receiverQueue.setBaseQueueManagerName(queueManager);
            receiverQueue.setBaseQueueName(receiverQueueName);

            receiverQueue.setReceiveCCSID(819);

        } catch (JMSException e) {
            System.out.println(e.toString());
        }
        return receiverQueue;
    }
}
