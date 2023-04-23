package com.haiyisoft.cross.region.module.high.httpclient;

import com.haiyisoft.cross.region.common.core.model.CrossRegionRequestEntity;
import com.haiyisoft.cross.region.common.core.model.CrossRegionResponseEntity;
import com.haiyisoft.cross.region.common.core.utils.HttpEntityConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 请求重放，访问高安全区的电厂应用后台服务
 * @author CLY
 * @date 2023/2/24 10:19
 **/
@Component
public class HttpClientProxy {

    @Autowired
    private RestTemplate restTemplate;

    public CrossRegionResponseEntity<byte[]> execute(CrossRegionRequestEntity<Object> crossRegionRequestEntity){
        final RequestEntity<Object> requestEntity = HttpEntityConvertUtils.convert2RequestEntity(crossRegionRequestEntity);
//        final ResponseEntity<byte[]> response = restTemplate.exchange(requestEntity, byte[].class);
//        return response;
        return this.execute(requestEntity);
    }

    public CrossRegionResponseEntity<byte[]> execute(RequestEntity<Object> requestEntity){
        final ResponseEntity<byte[]> response = restTemplate.exchange(requestEntity, byte[].class);
        final CrossRegionResponseEntity<byte[]> crossRegionResponseEntity = HttpEntityConvertUtils.convert2CrossRegionResponseEntity(response);
        return crossRegionResponseEntity;
    }

}
