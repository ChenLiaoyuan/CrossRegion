package com.haiyisoft.cross.region.common.redis.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 访问次数控制工具类
 * @author CLY
 * @date 2022/4/22 11:22
 **/
@Component
public class FrequencyControlUtil {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 检测该SessionID在second秒内访问的次数是否超过times次
     * @param sessionId
     * @param second
     * @param times
     * @return
     */
    public Boolean accept(String sessionId,String second,String times){
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setLocation(new ClassPathResource("lua/frequency_control_lua.lua"));
        redisScript.setResultType(Long.class);

        Long result = stringRedisTemplate.execute(redisScript, Arrays.asList(sessionId), second, times);

        // 返回1则允许访问
        return result.equals(1L);

    }


}
