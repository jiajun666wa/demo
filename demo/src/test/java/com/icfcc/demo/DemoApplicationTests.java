package com.icfcc.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoApplicationTests {

    @Test
    public void contextLoads() {
    }

    @Autowired
    SenderMsgTIPS senderMsgTIPS;
    @Autowired
    SenderMsg senderMsgTax;


    @Test
    public void testSender(){

//        senderMsgTIPS.send("123456789");
    }

    @Test
    public void send() {
        System.out.println(senderMsgTIPS);
        senderMsgTIPS.send();
    }

    @Test
    public void sendTax() {
        System.out.println(senderMsgTax);
        senderMsgTax.send();
    }
}
