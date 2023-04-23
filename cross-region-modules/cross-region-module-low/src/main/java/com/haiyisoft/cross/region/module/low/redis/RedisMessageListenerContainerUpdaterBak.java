package com.haiyisoft.cross.region.module.low.redis;

import com.haiyisoft.cross.region.common.core.constants.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisClusterNode;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * Redis事件监听容器更新，因为使用的是redis集群，要监听集群的键事件（notify-keyspace-events "E$"），
 * 那么就要监听redis集群所有master节点的键事件，否则就会监听补全。
 * 如果master主节点故障了，salve会变为master节点，那么此时监听的主节点也要跟着变化。
 * @author CLY
 * @date 2023/3/21 20:17
 **/
//@Component
@Slf4j
public class RedisMessageListenerContainerUpdaterBak implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired
    private DefaultListableBeanFactory beanFactory;

    @Value("${spring.redis.password}")
    private String redisPassword;

    /**
     * 当Spring应用程序上下文被初始化或刷新时（redis集群变化不会触发该事件），就会调用该方法，从而动态更新监听的主节点。
     * @param contextRefreshedEvent
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {

        RedisClusterConnection redisClusterConnection = redisConnectionFactory.getClusterConnection();
        if (redisClusterConnection != null) {
            Iterable<RedisClusterNode> nodes = redisClusterConnection.clusterGetNodes();
            for (RedisClusterNode node : nodes) {
                if (node.isMaster() && !node.isMarkedAsFail() && node.isConnected()) {
                    String containerBeanName = "messageContainer" + node.hashCode();
                    if (beanFactory.containsBean(containerBeanName)) {
                        continue;
                    }

                    RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(node.getHost(), node.getPort());
                    redisStandaloneConfiguration.setPassword(redisPassword);
                    LettuceConnectionFactory factory = new LettuceConnectionFactory(redisStandaloneConfiguration);
                    // 需要调用afterPropertiesSet()方法以完成初始化
                    factory.afterPropertiesSet();

                    BeanDefinitionBuilder containerBeanDefinitionBuilder = BeanDefinitionBuilder
                            .genericBeanDefinition(RedisMessageListenerContainer.class);
                    containerBeanDefinitionBuilder.addPropertyValue("connectionFactory", factory);
                    containerBeanDefinitionBuilder.setScope(BeanDefinition.SCOPE_SINGLETON);
                    containerBeanDefinitionBuilder.setLazyInit(false);
                    beanFactory.registerBeanDefinition(containerBeanName,
                            containerBeanDefinitionBuilder.getRawBeanDefinition());

                    RedisMessageListenerContainer container = beanFactory
                            .getBean(containerBeanName, RedisMessageListenerContainer.class);
                    String listenerBeanName = "crossRegionMessageListener";
                    if (!beanFactory.containsBean(listenerBeanName)) {
                        return;
                    }
                    log.info("Spring应用程序上下文初始化/刷新，添加Redis事件监听容器{}，主节点ip为{}，port为{}。",containerBeanName,node.getHost(),node.getPort());
                    CrossRegionMessageListener crossRegionMessageListener = (CrossRegionMessageListener)beanFactory.getBean("crossRegionMessageListener");
                    container.addMessageListener(crossRegionMessageListener, new ChannelTopic(Constants.REDIS_KEY_EVENT_TOPIC));
                    container.start();
                }
            }
        }
    }
}
