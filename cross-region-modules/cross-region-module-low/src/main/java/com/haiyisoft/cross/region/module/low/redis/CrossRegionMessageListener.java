package com.haiyisoft.cross.region.module.low.redis;

import com.haiyisoft.cross.region.common.core.exception.BaseException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author CLY
 * @date 2023/3/21 20:37
 **/
@Component("crossRegionMessageListener")
@Slf4j
public class CrossRegionMessageListener implements MessageListener {

    /**
     * 缓存redis集群的ResponseKey
     */
    private static ConcurrentHashMap<String,String> responseKeyMap = new ConcurrentHashMap<>();

    /**
     * 监听redis集群主节点的键set事件
     * @param message 为set的key值
     * @param pattern 为监听的频道，比如__keyevent@0__:set
     */
    @SneakyThrows
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String s = new String(pattern,"UTF-8");
        String key = new String(message.getBody(),"UTF-8");
        log.info("监听到redis集群set事件，key：{}，pattern：{}。",key,s);
        if (responseKeyMap.containsKey(key)){
            // 此时的key是新new的String，与原来的responseKey不是同一个对象，起不到通知的作用，因此要获取原来同一个对象
            String responseKey = responseKeyMap.get(key);
            synchronized (responseKey){
                // 删除key
                responseKeyMap.remove(key);
                // 通知ProxyController获取响应结果
                responseKey.notify();
            }
        }
    }

    /**
     * 添加跨区请求的key，监听到redis set该key后，用于通知ProxyController获取响应结果
     * @param responseKey 跨区请求的key，必须唯一
     * @return
     */
    public static boolean addKey(String responseKey){
        if (responseKeyMap.containsKey(responseKey)){
            throw new BaseException("cross-region-module-low",500,"跨区请求的key不唯一，会造成获取错误的响应结果",responseKey);
        }
        responseKeyMap.put(responseKey,responseKey);
        return true;
    }

    public static void remove(String keyId){
        responseKeyMap.remove(keyId);
    }

    public static boolean containsKey(String responseKey){
        return responseKeyMap.containsKey(responseKey);
    }


}
