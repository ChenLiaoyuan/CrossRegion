package com.liaoyuan.cross.region.common.core.constants;

/**
 * 常量设置
 * @author CLY
 * @date 2023/3/22 17:34
 **/
public interface Constants {

    /**
     * kafka低安全区消费者组，接收response响应结果
     */
    String CONSUMER_RESPONSE_GROUP = "cross-region-response-group";

    /**
     * kafka高安全区消费者组，接收request跨区请求
     */
    String CONSUMER_REQUEST_GROUP = "cross-region-request-group";

    /**
     * kafka接收低安全区跨区请求的主题
     */
    String KAFKA_TOPIC_REQUEST = "REQUEST";

    /**
     * kafka接收高安全区返回响应的主题
     */
    String KAFKA_TOPIC_RESPONSE = "RESPONSE";

    /**
     * 监听key的set事件
     */
    String REDIS_KEY_EVENT_TOPIC = "__keyevent@0__:set";

    /**
     * 跨区请求响应超时时间
     */
    long PROXY_RESPONSE_TIMEOUT_MILLISECOND = 10000;
}
