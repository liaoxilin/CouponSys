package com.xdclass.couponapp.service.listener;

import com.alibaba.fastjson.JSONObject;
import com.xdclass.couponapp.config.OrderConsumerConfig;
import com.xdclass.couponapp.config.payResultConsumerConfig;
import com.xdclass.couponapp.dto.OderCouponDto;
import com.xdclass.couponapp.service.CouponService;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.util.List;


@Service
public class PayResultListener extends payResultConsumerConfig implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log= LoggerFactory.getLogger(PayResultListener.class);

    @Resource
    private CouponService service;

    @Override
    public ConsumeConcurrentlyStatus dealBody(List<MessageExt> msgs) {
        msgs.forEach(msg->{
            byte[] bytes=msg.getBody();
            try {
                String msgStr=new String(bytes,"utf-8");
                OderCouponDto oderCouponDto=JSONObject.parseObject(msgStr,OderCouponDto.class);
                service.payResult(oderCouponDto.getOrderId(),oderCouponDto.getUserId());
                log.info("pay result recivice meg:{}",msgStr);
            } catch (UnsupportedEncodingException e) {
                log.error("body转字符串失败");
            }
        });
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent arg0) {
        try {
            super.consumer("PayResult","Tag1");
        } catch (MQClientException e) {
            log.error("消费者监听器启动失败",e);
        }
    }
}
