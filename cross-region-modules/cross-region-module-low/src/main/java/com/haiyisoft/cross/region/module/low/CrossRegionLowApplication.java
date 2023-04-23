package com.haiyisoft.cross.region.module.low;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author CLY
 * @date 2023/2/24 10:15
 **/
@SpringBootApplication(scanBasePackages = {"com.haiyisoft"})
@MapperScan("com.haiyisoft.cross.region.module.low.mapper")
@EnableDiscoveryClient
@EnableKafka
@EnableScheduling
public class CrossRegionLowApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrossRegionLowApplication.class,args);
    }
}
