package com.liaoyuan.cross.region.common.redis.config;

import com.haiyisoft.cross.region.common.redis.serializer.*;
import com.liaoyuan.cross.region.common.redis.serializer.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author CLY
 * @date 2021/8/25 15:56
 * 配置JSON序列化的RedisTemplate，
 * 并且配置RedisManager
 **/
@EnableCaching
@Configuration
// 在RedisAutoConfiguration之前初始化
@AutoConfigureBefore(RedisAutoConfiguration.class)
public class RedisConfiguration {

    /**
     * 初始化Value为JSON序列化器的RedisTemplate
     * @param redisConnectionFactory
     * @return
     */
    @Bean("jdkRedisTemplate")
    public RedisTemplate<String,Object> initJsonRedisTemplate(@Autowired RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String,Object> redisTemplate = new RedisTemplate<String,Object>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer();

        FastJson2JsonRedisSerializer fastJson2JsonRedisSerializer = new FastJson2JsonRedisSerializer(Object.class);

        JdkSerializationRedisSerializer jdkSerializationRedisSerializer = new JdkSerializationRedisSerializer();

        JDKRedisSerializer jdkRedisSerializer = new JDKRedisSerializer();

        KryoRedisSerializer<Object> kryoRedisSerializer = new KryoRedisSerializer<>();

        SnappyRedisSerializer<Object> snappyRedisSerializer = new SnappyRedisSerializer<>();

        GzipRedisSerializer<Object> gzipRedisSerializer = new GzipRedisSerializer<>();

        //key使用String序列化器，value使用JSON序列化器（JSON反序列化出现错误，全部使用String）
        redisTemplate.setDefaultSerializer(stringRedisSerializer);

        // String类型
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(kryoRedisSerializer);

        // Hash类型
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setHashValueSerializer(kryoRedisSerializer);

        return redisTemplate;
    }

    /**
     * 初始化Redis缓存管理器
     * @param redisConnectionFactory
     * @return
     */
    @Bean("redisCacheManager")
    public CacheManager initRedisCacheManager2(@Autowired RedisConnectionFactory redisConnectionFactory){
        RedisCacheConfiguration hourConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60)) //设置缓存60分钟时效
                .serializeKeysWith(keyPair()) //key使用String序列化器
                .serializeValuesWith(valuePair()) //value使用JSON序列化器
                .prefixKeysWith("HourCached:"); //后面的版本被弃用了，旧版本的redis key是prefix + cache entry key；
                // 2.3.0版本改为prefix + cache name +  "::"  + cache entry key。
                //.prefixCacheNameWith("cacheNamePrefix:");

        RedisCacheConfiguration halfHourConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) //设置缓存30分钟时效
                .serializeKeysWith(keyPair()) //key使用String序列化器
                .serializeValuesWith(valuePair()) //value使用JSON序列化器
                .prefixKeysWith("HalfHourCached:"); //redis key由prefix + cache entry key组成

        RedisCacheConfiguration oneDayConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofDays(1)) //设置缓存1天时效
                .serializeKeysWith(keyPair()) //key使用String序列化器
                .serializeValuesWith(valuePair()) //value使用JSON序列化器
                .prefixKeysWith("OneDayCached:"); //redis key由prefix + cache entry key组成

        //缓存名称与配置，不同的服务使用不同的配置
        Map<String, RedisCacheConfiguration> redisConfigurationMap = new HashMap<>();
        redisConfigurationMap.put("HourCached",hourConfiguration);
        redisConfigurationMap.put("HalfHourCached",halfHourConfiguration);
        redisConfigurationMap.put("OneDayCached",oneDayConfiguration);

        RedisCacheManager redisCacheManager = RedisCacheManager.builder(redisConnectionFactory)
                //.withCacheConfiguration("gpspCache", redisCacheConfiguration)
                .withInitialCacheConfigurations(redisConfigurationMap)
                .build();
        return redisCacheManager;
    }

    /**
     * 配置键序列化
     * @return StringSerializer
     */
    private RedisSerializationContext.SerializationPair<String> keyPair(){
        return RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer(StandardCharsets.UTF_8));
    }

    /**
     * 配置值序列化，使用GenericJackson2JsonSerializer替换默认值序列化
     * @return
     */
    private RedisSerializationContext.SerializationPair<Object> valuePair(){
        return RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer());
    }

}
