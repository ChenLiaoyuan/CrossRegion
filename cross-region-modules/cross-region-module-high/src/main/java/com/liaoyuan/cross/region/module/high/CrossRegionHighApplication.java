package com.liaoyuan.cross.region.module.high;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * @author CLY
 * @date 2023/2/24 10:15
 **/
@SpringBootApplication(scanBasePackages = {"com.haiyisoft"})
@MapperScan("com.haiyisoft.cross.region.module.high.mapper")
@EnableDiscoveryClient
@EnableKafka
public class CrossRegionHighApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrossRegionHighApplication.class,args);
    }
}
