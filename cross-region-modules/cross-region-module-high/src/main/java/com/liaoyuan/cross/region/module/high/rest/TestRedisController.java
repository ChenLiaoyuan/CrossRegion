package com.liaoyuan.cross.region.module.high.rest;

import com.liaoyuan.cross.region.common.core.model.User;
import com.liaoyuan.cross.region.common.core.model.SnowFlakeId;
import com.liaoyuan.cross.region.common.redis.util.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author CLY
 * @date 2023/3/15 11:29
 **/
@RestController
@Api(tags = "Redis集群测试")
public class TestRedisController {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private SnowFlakeId snowFlakeId;

    @PostMapping("/{key}")
    @ApiOperation(value = "测试redis分布式锁")
    public String setKey(@ApiParam @PathVariable("key") String key){
        redisTemplate.opsForValue().set("user",User.builder().age(28).name("cly").id(999L).build());

        final User user = (User)redisTemplate.opsForValue().get("user");

        String value = snowFlakeId.nextStringId();
//        redisUtil.set("test-key","test");
        return redisUtil.setIfNotExist(key, value, 30) ? value : "null";
    }

}
