package com.liaoyuan.cross.region.module.low.config;

import com.liaoyuan.cross.region.common.core.model.SnowFlakeId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author CLY
 * @date 2023/3/21 16:19
 **/
@Configuration
public class SnowFlakeIdConfig {

    @Value("${snow-flake.worker-id}")
    private long workerId;
    @Value("${snow-flake.datacenter-id}")
    private long dataCenterId;

    @Bean
    public SnowFlakeId snowFlakeId(){
        return new SnowFlakeId(workerId,dataCenterId);
    }

}
