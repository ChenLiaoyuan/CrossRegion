package com.liaoyuan.cross.region.common.kafka.util;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author CLY
 * @date 2023/3/21 10:11
 **/
public class CrossRegionKafkaUtils {

    /**
     * consumerGroup消费组的topic offset跳转到最新位置，即不消费以前未消费的数据
     * @param kafkaConsumer
     * @param topic
     */
    public static void seekToEnd(KafkaConsumer kafkaConsumer, String topic){
        Map<TopicPartition, OffsetAndMetadata> offset = new HashMap<>();
        //找到topic下所有的分区
        List<PartitionInfo> partitionInfos = kafkaConsumer.partitionsFor(topic);

        if (null != partitionInfos && partitionInfos.size() > 0) {

            partitionInfos.forEach(p -> {
                TopicPartition tp = new TopicPartition(topic,p.partition());
                List<TopicPartition> tpList = Arrays.asList(tp);

                //消费者分配到该分区
                kafkaConsumer.assign(tpList);

                //移动到最新offset
                kafkaConsumer.seekToEnd(tpList);

                //获取到该分区的last offset
                long position = kafkaConsumer.position(tp);
                // 保存消费位置，用于手动commit，position + 1 > position指消费下一条的最新数据
                offset.put(tp, new OffsetAndMetadata(position));
            });
        }
        // 提交offset
        kafkaConsumer.commitSync(offset, Duration.ofSeconds(10));
//        kafkaConsumer.subscribe(Collections.singletonList(topic));
//        kafkaConsumer.poll(Duration.ofSeconds(10)); // ensure consumer is assigned to a partition
//        //Arrays.asList(new TopicPartition(topic,0),new TopicPartition(topic,1))
//        kafkaConsumer.seekToEnd(kafkaConsumer.assignment());
//        kafkaConsumer.commitSync(Duration.ofSeconds(10));
//        kafkaConsumer.close(Duration.ofSeconds(10));
    }
}
