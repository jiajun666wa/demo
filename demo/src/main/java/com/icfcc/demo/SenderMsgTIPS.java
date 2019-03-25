package com.icfcc.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsOperations;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class SenderMsgTIPS {
    @Autowired
    @Qualifier("jmsOperationsTIPS")
    JmsOperations jmsOperations;

    //@PostConstruct在服务器加载Servle的时候运行，并且只会被服务器执行一次, @PreDestroy在destroy()方法执行执行之后执行
//    @PostConstruct
    public void send(){
        System.out.println("开始发送消息");
        jmsOperations.convertAndSend("PBC.EXT.ONLINE.IN", "TIPS my message...");
    }



}
