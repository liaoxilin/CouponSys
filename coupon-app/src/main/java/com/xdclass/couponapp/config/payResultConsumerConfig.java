package com.xdclass.couponapp.config;


import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public abstract class payResultConsumerConfig {


    private static final Logger log= LoggerFactory.getLogger(payResultConsumerConfig.class);

    @Value("${rocketmq.consumer.pay.groupName}")
    private String groupName;
    @Value("${rocketmq.consumer.pay.namesrvAddr}")
    private String namesrvAddr;


    public void consumer(String topic ,String tag) throws MQClientException {
        DefaultMQPushConsumer consumer =new DefaultMQPushConsumer(groupName);
        consumer.setNamesrvAddr(namesrvAddr);
        consumer.subscribe(topic,tag);
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                return payResultConsumerConfig.this.dealBody(msgs);
            }
        });
        consumer.start();
        log.info("rocketmq启动成功");
    }

    /**
     * 要实现的时候继承这个抽象类
     * @param msgs
     * @return
     */
    public abstract ConsumeConcurrentlyStatus dealBody(List<MessageExt> msgs) ;


}
