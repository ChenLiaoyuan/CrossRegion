package com.liaoyuan.cross.region.module.low.redis;

import com.liaoyuan.cross.region.common.core.constants.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * Redis事件监听容器更新，因为使用的是redis集群，要监听集群的键事件（notify-keyspace-events "E$"），
 * 那么就要监听redis集群所有master节点的键事件，否则就会监听补全。
 * 如果master主节点故障了，salve会变为master节点，那么此时监听的主节点也要跟着变化。
 * @author CLY
 * @date 2023/3/21 20:17
 **/
@Configuration
@Slf4j
@Profile("local")
public class LocalRedisMessageListenerContainerConfig{

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory){
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
        redisMessageListenerContainer.addMessageListener(new CrossRegionMessageListener(), new ChannelTopic(Constants.REDIS_KEY_EVENT_TOPIC));
        return redisMessageListenerContainer;
    }

}
