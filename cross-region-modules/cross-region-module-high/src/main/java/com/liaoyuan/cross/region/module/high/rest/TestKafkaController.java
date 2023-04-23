package com.liaoyuan.cross.region.module.high.rest;

import com.liaoyuan.cross.region.common.core.constants.Constants;
import com.liaoyuan.cross.region.common.core.model.CrossRegionRequestEntity;
import com.liaoyuan.cross.region.module.high.kafka.CrossRegionHighKafkaProducer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author CLY
 * @date 2023/3/20 20:53
 **/
@RestController
@Api(tags = "测试kafka")
public class TestKafkaController {

    @Autowired
    private CrossRegionHighKafkaProducer kafkaProducer;

    @ApiOperation("传输数据到kafka主题")
    @PostMapping("/sendToKafkaTopic")
    public void sendToKafkaTopic(String key, String value){
        kafkaProducer.sendKafkaTopic(Constants.KAFKA_TOPIC_REQUEST,key,value);
    }

    @ApiOperation("传输文件到kafka主题")
    @PostMapping(value = "/sendFileToKafkaTopic")
    public void sendFileToKafkaTopic(@RequestPart("key") String key,@RequestPart("file") MultipartFile file){

        try {
            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            CrossRegionRequestEntity<Object> regionRequestEntity = new CrossRegionRequestEntity<>(HttpMethod.POST,"/sendFileToKafkaTopic",file.getBytes(),headers);

            kafkaProducer.sendKafkaTopic(Constants.KAFKA_TOPIC_REQUEST,key,regionRequestEntity);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
