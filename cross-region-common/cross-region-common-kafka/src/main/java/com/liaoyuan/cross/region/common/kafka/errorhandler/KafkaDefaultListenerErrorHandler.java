package com.liaoyuan.cross.region.common.kafka.errorhandler;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component("kafkaDefaultListenerErrorHandler")
@Slf4j
public class KafkaDefaultListenerErrorHandler implements KafkaListenerErrorHandler {
    @Override
    public Object handleError(Message message, ListenerExecutionFailedException exception) {
        log.error("从kafka主题消费数据异常", exception);
        return null;
    }

    @Override
    public Object handleError(Message message, ListenerExecutionFailedException exception, Consumer consumer) {
        log.error("从kafka主题{}消费数据异常", consumer.listTopics(),exception);
        return null;
    }
}