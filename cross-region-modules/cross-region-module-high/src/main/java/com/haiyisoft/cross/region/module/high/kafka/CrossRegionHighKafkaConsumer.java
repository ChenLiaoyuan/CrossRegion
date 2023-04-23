package com.haiyisoft.cross.region.module.high.kafka;

import com.haiyisoft.cross.region.common.core.constants.Constants;
import com.haiyisoft.cross.region.common.core.model.CrossRegionRequestEntity;
import com.haiyisoft.cross.region.common.core.model.CrossRegionResponseEntity;
import com.haiyisoft.cross.region.module.high.httpclient.HttpClientProxy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * @author CLY
 * @date 2023/3/20 20:21
 **/
@Component
// 先初始化kafkaConsumer，将offset初始化为最新位置，再启动消费
@DependsOn("myKafkaConsumer")
@Slf4j
public class CrossRegionHighKafkaConsumer implements InitializingBean {

    @Autowired
    KafkaConsumer<Object, Object> consumer;

    @Autowired
    private HttpClientProxy httpClientProxy;

    @Autowired
    private RedisTemplate<String, Object> jdkRedisTemplate;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * 第2步，将request请求从kafka REQUEST主题中读取出来重放请求
     * @param consumerRecords
     */
    @KafkaListener(topics = Constants.KAFKA_TOPIC_REQUEST,concurrency = "2",
            groupId = Constants.CONSUMER_REQUEST_GROUP,
            errorHandler = "kafkaDefaultListenerErrorHandler")
    public void consumerTopic(ConsumerRecords<?,?> consumerRecords){
//        log.info("消费者组为：{}", KafkaUtils.getConsumerGroupId());
        // 使用线程池执行，避免有些请求一直卡着，导致后面的请求也跟着卡了
        threadPoolTaskExecutor.submit(()->{
            proxy(consumerRecords);
        });
//        acknowledgment.acknowledge();
    }

    private void proxy(ConsumerRecords<?,?> consumerRecords){
        CrossRegionRequestEntity<Object> crossRegionRequestEntity = null;
        for (ConsumerRecord<?,?> consumerRecord : consumerRecords){
            try{
//                log.info("key为：{}",consumerRecord.key());
                log.info("第2步，将request请求从kafka REQUEST主题中读取出来重放请求。");
//                String value = consumerRecord.value().toString();
                // 对于泛型转换需要用TypeReference指明
//                TypeReference<CrossRegionRequestEntity<byte[]>> typeReference = new TypeReference<CrossRegionRequestEntity<byte[]>>() {};
//                CrossRegionRequestEntity<byte[]> crossRegionRequestEntity = JSONObject.parseObject(value, typeReference,
//                        JSONReader.Feature.SupportAutoType, // 支持自动类型，要读取带"@type"类型信息的JSON数据，需要显示打开SupportAutoType
//                        JSONReader.Feature.FieldBased, // 基于字段反序列化，如果不配置，会默认基于public的field和getter方法序列化。配置后，会基于非static的field（包括private）做反序列化。在fieldbase配置下会更安全
//                        JSONReader.Feature.SupportClassForName, //支持类型为Class的字段，使用Class.forName。为了安全这个是默认关闭的
////                        JSONReader.Feature.SupportArrayToBean, // 支持将JSON数组反序列化为Java数组或集合类型
//                        JSONReader.Feature.Base64StringAsByteArray); // base64字符串转化为byte[]数组
//                String s = crossRegionRequestEntity.toString();
//                log.info("value为CrossRegionRequestEntity：{}",s.substring(0,s.length() > 1024 ? 1024 : s.length()));

                crossRegionRequestEntity = (CrossRegionRequestEntity<Object>)consumerRecord.value();
                CrossRegionResponseEntity<byte[]> crossRegionResponseEntity = this.startProxy(crossRegionRequestEntity);

                // 第3步，将response响应结果传输到Redis集群
                this.sendResponseToRedisCluster(consumerRecord.key().toString(),crossRegionResponseEntity);
            }catch (Exception e){
                log.error("第2步，将request请求从kafka REQUEST主题中读取出来重放请求发生错误！url为：{}", crossRegionRequestEntity != null ? crossRegionRequestEntity.getUrl() : "", e);
                // 返回错误信息，避免客户端一直等待超时
                this.sendResponseToRedisCluster(consumerRecord.key().toString(),new CrossRegionResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
            }
        }
    }

    private CrossRegionResponseEntity<byte[]> startProxy(CrossRegionRequestEntity<Object> crossRegionRequestEntity){
        final CrossRegionResponseEntity<byte[]> execute = httpClientProxy.execute(crossRegionRequestEntity);
        return execute;
    }

    /**
     * 第3步，将response响应结果传输到Redis集群
     * @param key
     * @param regionResponseEntity
     */
    private void sendResponseToRedisCluster(String key,CrossRegionResponseEntity<byte[]> regionResponseEntity){
        log.info("第3步，将response响应结果传输到Redis集群");
        jdkRedisTemplate.opsForValue().set(key, regionResponseEntity, Duration.ofMinutes(5));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
//        consumer.subscribe(Collections.singletonList(KafkaTopics.REQUEST));
//        consumer.poll(Duration.ofSeconds(10)); // ensure consumer is assigned to a partition
//        //Arrays.asList(new TopicPartition(KafkaTopics.REQUEST,0),new TopicPartition(KafkaTopics.REQUEST,1))
//        consumer.seekToEnd(consumer.assignment());
//        consumer.commitSync(Duration.ofSeconds(10));
//        consumer.close(Duration.ofSeconds(10));
//        log.info("kafka消费组：{} offset初始化为最新位置",KafkaConsumerGroups.CONSUMER_GROUP);
    }
}
