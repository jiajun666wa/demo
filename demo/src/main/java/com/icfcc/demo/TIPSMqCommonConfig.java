package com.icfcc.demo;

import com.ibm.mq.jms.MQQueue;
import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;
import org.springframework.jms.core.JmsOperations;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.transaction.PlatformTransactionManager;

import javax.jms.JMSException;
import javax.jms.MessageListener;

@Configuration
public class TIPSMqCommonConfig {

    //初始化成员变量
    @Value("${tips.mq.manager.host}")
    private String host;
    @Value("${tips.mq.manager.port}")
    private String port;
    @Value("${tips.mq.manager.name}")
    private String qmanager;
    @Value("${tips.mq.manager.channel}")
    private String channel;
    @Value("${tips.mq.manager.transportType}")
    private String transportType;
    @Value("${tips.mq.manager.receiveQName}")
    private String receiveQname;
    @Value("${tips.mq.manager.sendQName}")
    private String sendQname;

    /**
     * 创建工厂类
     *
     * @return
     */
    @Bean
    @Qualifier("mqQueueConnectionFactoryTIPS")
    public MQQueueConnectionFactory mqQueueConnectionFactoryTIPS() {

        MQQueueConnectionFactory mqQueueConnectionFactory = new MQQueueConnectionFactory();
        try {
            mqQueueConnectionFactory.setHostName(host);
            //端口
            mqQueueConnectionFactory.setPort(Integer.valueOf(port));
            //队列
            mqQueueConnectionFactory.setQueueManager(qmanager);
            //连接方式transportType不对 默认为0 远程调用要改成1(TCP)连接，
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
     * @param mqQueueConnectionFactoryTIPS
     * @return
     */
    @Bean
    @Qualifier("userCredentialsConnectionFactoryAdapterTIPS")
    UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapterTIPS(@Qualifier("mqQueueConnectionFactoryTIPS") MQQueueConnectionFactory mqQueueConnectionFactoryTIPS) {
        UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter = new UserCredentialsConnectionFactoryAdapter();
       // userCredentialsConnectionFactoryAdapter.setUsername(password);
       // userCredentialsConnectionFactoryAdapter.setPassword(username);
        userCredentialsConnectionFactoryAdapter.setTargetConnectionFactory(mqQueueConnectionFactoryTIPS);
        return userCredentialsConnectionFactoryAdapter;
    }

    /**
     * 设置缓存
     *
     * @param
     * @return
     */
    @Bean
    @Qualifier("cachingConnectionFactoryTIPS")
    public CachingConnectionFactory cachingConnectionFactoryTIPS(
            @Qualifier("userCredentialsConnectionFactoryAdapterTIPS")UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdaptertips) {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
//        cachingConnectionFactory.setTargetConnectionFactory(mqQueueConnectionFactory);
        cachingConnectionFactory.setTargetConnectionFactory(userCredentialsConnectionFactoryAdaptertips);
        cachingConnectionFactory.setSessionCacheSize(500);
        cachingConnectionFactory.setReconnectOnException(true);
        return cachingConnectionFactory;
    }

    /**
     * 设置消息事务管理器
     * @param cachingConnectionFactory
     * @return
     */
    @Bean
    @Qualifier("jmsTransactionManagerTIPS")
    PlatformTransactionManager jmsTransactionManagerTIPS(@Qualifier("cachingConnectionFactoryTIPS") CachingConnectionFactory cachingConnectionFactory){
        JmsTransactionManager jmsTransactionManager = new JmsTransactionManager();
        jmsTransactionManager.setConnectionFactory(cachingConnectionFactory);
        return  jmsTransactionManager;
    }

    @Bean
    @Qualifier("jmsOperationsTIPS")
    JmsOperations jmsOperationsTIPS(@Qualifier("cachingConnectionFactoryTIPS")CachingConnectionFactory cachingConnectionFactory){
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory(cachingConnectionFactory);
        jmsTemplate.setReceiveTimeout(500);
        return jmsTemplate;
    }

    /**
     * 注入监听
     *
     * @return
     */
    @Bean
    @Qualifier("jmsListenerContainerFactoryTIPS")
    public DefaultMessageListenerContainer jmsListenerContainerFactoryTIPS(@Qualifier("cachingConnectionFactoryTIPS")CachingConnectionFactory cachingConnectionFactory
            ,@Qualifier("receiverQueueTIPS")MQQueue receiverQueueTIPS,@Qualifier("receiverMsgListenerTIPS")MessageListener receiverMsgListenerTIPS) {
        DefaultMessageListenerContainer factory = new DefaultMessageListenerContainer();
        factory.setConnectionFactory(cachingConnectionFactory);
        //factory.setTransactionManager(jmsTransactionManager(cachingConnectionFactory));
        factory.setDestination(receiverQueueTIPS);
        factory.setMessageListener(receiverMsgListenerTIPS);
        factory.setConcurrentConsumers(20);
        factory.setSessionTransacted(true);
        factory.setPubSubDomain(false);
        return factory;
    }


    @Bean
    @Qualifier("receiverQueueTIPS")
    public MQQueue ReceiverQueueTIPS() {
        MQQueue receiverQueue = new MQQueue();
        try {
            receiverQueue.setBaseQueueManagerName(qmanager);
            receiverQueue.setBaseQueueName(receiveQname);
            receiverQueue.setReceiveCCSID(819);

        } catch (JMSException e) {
            System.out.println(e.toString());
        }

        return receiverQueue;
    }
}
