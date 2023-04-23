package com.haiyisoft.cross.region.module.low.kafka;

import com.haiyisoft.cross.region.common.core.constants.Constants;
import com.haiyisoft.cross.region.common.kafka.util.CrossRegionKafkaUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author CLY
 * @date 2023/3/20 22:19
 **/
@Configuration
//@ConditionalOnBean(KafkaProperties.class)
@Slf4j
public class CrossRegionLowKafkaConfig {

    @Value("${spring.kafka.consumer.discard-unconsumed-data:true}")
    private boolean discardUnconsumedData;

    public DefaultKafkaConsumerFactory<Object, Object> kafkaConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "my-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    public ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(kafkaConsumerFactory());
        factory.setConcurrency(2); // 设置3个线程来处理消息
        factory.getContainerProperties().setPollTimeout(3000); // 设置3秒超时时间
        return factory;
    }

    @Autowired
    private KafkaProperties kafkaProperties;

    @Bean(name = "myKafkaConsumer")
    public KafkaConsumer<Object,Object> kafkaConsumer(){
        final KafkaProperties.Consumer consumerProp = kafkaProperties.getConsumer();
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, Constants.CONSUMER_RESPONSE_GROUP);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, consumerProp.getAutoOffsetReset());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, consumerProp.getKeyDeserializer());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, consumerProp.getValueDeserializer());
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, consumerProp.getProperties().get(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS));
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, consumerProp.getProperties().get(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS));

        KafkaConsumer<Object,Object> kafkaConsumer = new KafkaConsumer<>(props);

        if (!discardUnconsumedData){
            return kafkaConsumer;
        }

        // 低安全区消费RESPONSE主题，旧的数据跳过不消费
        CrossRegionKafkaUtils.seekToEnd(kafkaConsumer,Constants.KAFKA_TOPIC_RESPONSE);
        log.info("kafka消费组：{}，主题：{} offset的消费位置初始化为最新位置", Constants.CONSUMER_RESPONSE_GROUP,Constants.KAFKA_TOPIC_RESPONSE);

        return kafkaConsumer;
    }
}
