package com.liaoyuan.cross.region.module.high.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * @author CLY
 * @date 2023/3/20 20:45
 **/
@Component
@Slf4j
public class CrossRegionHighKafkaProducer {

    @Autowired
    private KafkaTemplate<Object,Object> kafkaTemplate;

    public void sendKafkaTopic(String topic,Object key, Object value){
        kafkaTemplate.send(topic, key,value).addCallback(new ListenableFutureCallback<SendResult<Object, Object>>() {
            @Override
            public void onFailure(Throwable ex) {
                log.error("传输数据到kafka主题：{}出错", topic,ex);
            }

            @Override
            public void onSuccess(SendResult<Object, Object> result) {
                log.info("传输数据到kafka主题成功");
            }
        });
    }

}
