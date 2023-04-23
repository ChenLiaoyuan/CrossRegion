package com.haiyisoft.crossregion.test;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.haiyisoft.cross.region.common.core.model.CrossRegionRequestEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.MimeTypeUtils;

import java.util.Collections;

/**
 * @author CLY
 * @date 2023/2/24 13:57
 **/
public class RequestEntityTest {

    @Test
    public void testRequestEntitySerialize(){
        try {
//            RequestEntity<String> requestEntity = new RequestEntity<String>(HttpMethod.GET,new URI("http://localhost:8080"));
//            final String jsonString = JSON.toJSONString(requestEntity);
//            System.out.println(JSON.toJSONString(requestEntity));
            // 无法反序列化，因为RequestEntity的属性为final
//            final RequestEntity requestEntity1 = JSONObject.parseObject(jsonString, RequestEntity.class);
//            System.out.println(requestEntity1.equals(requestEntity));

            final HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setAccept(Collections.singletonList(MediaType.asMediaType(MimeTypeUtils.APPLICATION_JSON)));
            CrossRegionRequestEntity<byte[]> crossRegionRequestEntity = new CrossRegionRequestEntity<>(HttpMethod.GET,"http://localhost:8080","请求实体".getBytes("UTF-8"),httpHeaders);
            final String jsonString1 = JSON.toJSONString(crossRegionRequestEntity);
            System.out.println(jsonString1);

            // 对于泛型转换需要用TypeReference指明
            final TypeReference<CrossRegionRequestEntity<byte[]>> typeReference = new TypeReference<CrossRegionRequestEntity<byte[]>>() {};

            final CrossRegionRequestEntity crossRegionRequestEntity1 = JSONObject.parseObject(jsonString1,typeReference);

            System.out.println(crossRegionRequestEntity1);
            System.out.println(crossRegionRequestEntity1.equals(crossRegionRequestEntity));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
