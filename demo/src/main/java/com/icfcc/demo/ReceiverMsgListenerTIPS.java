package com.icfcc.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsOperations;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;


//消息消费者的类上必须加上@Component，或者是@Service，
// 这样的话，消息消费者类就会被委派给Listener类，
// 原理类似于使用SessionAwareMessageListener以及MessageListenerAdapter来实现消息驱动POJO
//public class ReceiverMsgListener extends MessageListenerAdapter {
@Component
@Qualifier("receiverMsgListenerTIPS")
public class ReceiverMsgListenerTIPS  implements MessageListener {

    @Autowired
    @Qualifier("jmsOperationsTIPS")
    JmsOperations jmsOperations;



    @Override
//    @JmsListener(destination = "TCQS.EXT.BATCH.IN")
    public void onMessage(Message message) {
        String messageBody = new String(message.toString());

        System.out.println("TIPS成功监听Q1消息队列，传来的值为:" + messageBody);

    }
}
