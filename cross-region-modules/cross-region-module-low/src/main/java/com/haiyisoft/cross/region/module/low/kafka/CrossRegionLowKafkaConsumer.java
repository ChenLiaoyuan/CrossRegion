package com.haiyisoft.cross.region.module.low.kafka;

import com.haiyisoft.cross.region.common.core.constants.Constants;
import com.haiyisoft.cross.region.common.core.model.CrossRegionRequestEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.context.annotation.DependsOn;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaUtils;
import org.springframework.stereotype.Component;

/**
 * @author CLY
 * @date 2023/3/20 20:21
 **/
@Component
// 先初始化myKafkaConsumer，将offset初始化为最新位置，再启动消费
@DependsOn("myKafkaConsumer")
@Slf4j
public class CrossRegionLowKafkaConsumer {

    /**
     * 第3步，获取响应数据（暂改为从redis获取响应数据）
     * @param consumerRecords
     */
    @KafkaListener(topics = Constants.KAFKA_TOPIC_RESPONSE,concurrency = "2",
            groupId = Constants.CONSUMER_RESPONSE_GROUP,
            errorHandler = "kafkaDefaultListenerErrorHandler")
    public void consumerTopic(ConsumerRecords<?,?> consumerRecords){
        log.info("消费者组为：{}",KafkaUtils.getConsumerGroupId());
        for (ConsumerRecord<?,?> consumerRecord : consumerRecords){
            try{
                log.info("key为：{}",consumerRecord.key());
                final Object value = consumerRecord.value();
                if (value instanceof CrossRegionRequestEntity){
                    final CrossRegionRequestEntity value1 = (CrossRegionRequestEntity) value;
                    log.info("value为CrossRegionRequestEntity：{}",value1.toString().substring(0,1024));
                }else{
                    log.info("value为：{}",value);
                }
//                acknowledgment.acknowledge();
            }catch (Exception e){
                log.error("业务处理出错！", e);
            }
        }
    }

}
