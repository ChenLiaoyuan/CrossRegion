package com.haiyisoft.cross.region.module.low.redis;

import com.haiyisoft.cross.region.common.core.constants.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisClusterNode;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Redis事件监听容器更新，因为使用的是redis集群，要监听集群的键事件（notify-keyspace-events "E$"），
 * 那么就要监听redis集群所有master节点的键事件，否则就会监听补全。
 * 如果master主节点故障了，salve会变为master节点，那么此时监听的主节点也要跟着变化。
 * @author CLY
 * @date 2023/3/21 20:17
 **/
@Component
@Slf4j
@Profile("dev")
public class RedisMessageListenerContainerUpdater implements InitializingBean {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired
    private CrossRegionMessageListener crossRegionMessageListener;

    @Value("${spring.redis.password}")
    private String redisPassword;

    private Map<String,RedisMessageListenerContainer> containerMap = new HashMap<>(16);

    /**
     * 定时调用该方法，动态更新监听的主节点。
     * @param
     */
    @Scheduled(fixedDelay = 10000)
    public void updateContainer() {
//        log.info("监听redis集群主节点是否有变化");
        RedisClusterConnection redisClusterConnection = redisConnectionFactory.getClusterConnection();
        if (redisClusterConnection != null) {
            Iterable<RedisClusterNode> nodes = redisClusterConnection.clusterGetNodes();
            for (RedisClusterNode node : nodes) {
                String containerBeanName = "messageContainer" + node.hashCode();
                if (node.isMaster() && !node.isMarkedAsFail() && node.isConnected()) {
                    if (containerMap.containsKey(containerBeanName)) {
                        continue;
                    }

                    log.info("添加Redis事件监听容器{}，主节点ip为{}，port为{}。",containerBeanName,node.getHost(),node.getPort());
                    RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(node.getHost(), node.getPort());
                    redisStandaloneConfiguration.setPassword(redisPassword);
                    LettuceConnectionFactory factory = new LettuceConnectionFactory(redisStandaloneConfiguration);
                    // 需要调用afterPropertiesSet()方法以完成初始化
                    factory.afterPropertiesSet();

                    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
                    container.setConnectionFactory(factory);
                    container.addMessageListener(crossRegionMessageListener, new ChannelTopic(Constants.REDIS_KEY_EVENT_TOPIC));
                    // 初始化容器
                    container.afterPropertiesSet();
                    // 启动监听
                    container.start();

                    containerMap.put(containerBeanName, container);

                    // 过期的主节点移除监听
                }else if (containerMap.containsKey(containerBeanName)){
                    log.info("主节点发生故障，移除Redis事件监听容器{}，主节点ip为{}，port为{}。",containerBeanName,node.getHost(),node.getPort());
                    try {
                        containerMap.get(containerBeanName).destroy();
                        containerMap.remove(containerBeanName);
                    } catch (Exception e) {
                        log.error("移除主节点发生异常", e);
                    }
                }
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.updateContainer();
    }
}
