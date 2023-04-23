package com.haiyisoft.crossregion.test;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TypeReference;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.haiyisoft.cross.region.common.core.model.CrossRegionRequestEntity;
import com.haiyisoft.cross.region.common.core.model.MyByteArrayResource;
import com.haiyisoft.cross.region.common.core.model.User;
import com.haiyisoft.cross.region.common.core.utils.HttpEntityConvertUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.ArrayList;

/**
 * @author CLY
 * @date 2023/2/24 15:10
 **/
public class RestTemplateTest {

    @Test
    public void testRestTemplate() throws IOException, ClassNotFoundException {
        final OkHttp3ClientHttpRequestFactory okHttp3ClientHttpRequestFactory = new OkHttp3ClientHttpRequestFactory();
        RestTemplate restTemplate = new RestTemplate(new OkHttp3ClientHttpRequestFactory());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String contentType = "age=13";

        MultiValueMap<String,Object> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("age", 13);

        Object body = multiValueMap;

        CrossRegionRequestEntity<Object> requestEntity = new CrossRegionRequestEntity<>(HttpMethod.POST, "http://localhost:8060/cloud-consumer/testGateWay2", body,headers);

        // 使用JDK序列化
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        FileOutputStream fileOutputStream = new FileOutputStream("E:\\Git\\oms2\\基础服务平台\\电厂服务\\20-source\\cross-region\\cross-region-modules\\cross-region-module-high\\src\\test\\resources\\objectout.txt");
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(requestEntity);
        objectOutputStream.flush();
        objectOutputStream.close();

        FileInputStream fileInputStream = new FileInputStream("E:\\Git\\oms2\\基础服务平台\\电厂服务\\20-source\\cross-region\\cross-region-modules\\cross-region-module-high\\src\\test\\resources\\objectout.txt");
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        CrossRegionRequestEntity<Object> requestEntity_deserializer = (CrossRegionRequestEntity<Object>)objectInputStream.readObject();

        // fastJSON序列化不行，byte[]数组识别为JSONArray数组了
        String value = "{\"body\":[123,13,10,32,32,32,32,34,97,103,101,34,58,32,49,51,44,13,10,32,32,32,32,34,110,97,109,101,34,58,32,34,-23,-103,-120,-25,-121,-114,-27,-114,-97,34,13,10,125,32,32,32],\"headers\":{\"host\":[\"localhost:9898\"],\"x-real-ip\":[\"127.0.0.1\"],\"x-forwarded-for\":[\"127.0.0.1\"],\"x-original-url\":[\"/cloud-consumer/testGateWay?name=cly\"],\"content-length\":[\"48\"],\"user-agent\":[\"PostmanRuntime-ApipostRuntime/1.1.0\"],\"cache-control\":[\"no-cache\"],\"content-type\":[\"application/json\"],\"accept\":[\"*/*\"],\"accept-encoding\":[\"gzip, deflate, br\"],\"target-host\":[\"http://localhost:8060\"],\"cookie\":[\"MyCookie=YangYuxian;JSESSIONID=B11500B16EBCB27B5A9E58E2213C393D;MyCookie=YangYuxian;MyCookie=YangYuxian;JSESSIONID=548DEF614DA6AFB537218EEA61062F5B;JSESSIONID=08841EC90333D59FF5C3B36F06A58B95\"]},\"method\":2,\"url\":\"http://localhost:8060/cloud-consumer/testGateWay?name=cly\"}";
//        value = "{\"@type\":\"com.haiyisoft.cross.region.common.core.model.CrossRegionRequestEntity\",\"body\":\"ew0KICAgICJhZ2UiOiAxMywNCiAgICAibmFtZSI6ICLpmYjnh47ljp8iDQp9ICAg\",\"headers\":{\"host\":[\"localhost:9898\"],\"x-real-ip\":[\"127.0.0.1\"],\"x-forwarded-for\":[\"127.0.0.1\"],\"x-original-url\":[\"/cloud-consumer/testGateWay?name=cly\"],\"content-length\":[\"48\"],\"user-agent\":[\"PostmanRuntime-ApipostRuntime/1.1.0\"],\"cache-control\":[\"no-cache\"],\"content-type\":[\"application/json\"],\"accept\":[\"*/*\"],\"accept-encoding\":[\"gzip, deflate, br\"],\"target-host\":[\"http://localhost:8060\"],\"cookie\":[\"MyCookie=YangYuxian;JSESSIONID=B11500B16EBCB27B5A9E58E2213C393D;MyCookie=YangYuxian;MyCookie=YangYuxian;JSESSIONID=548DEF614DA6AFB537218EEA61062F5B;JSESSIONID=08841EC90333D59FF5C3B36F06A58B95\"]},\"method\":2,\"url\":\"http://localhost:8060/cloud-consumer/testGateWay?name=cly\"}";

        TypeReference<CrossRegionRequestEntity<byte[]>> typeReference = new TypeReference<CrossRegionRequestEntity<byte[]>>() {};
        CrossRegionRequestEntity<byte[]> crossRegionRequestEntity = JSONObject.parseObject(value,CrossRegionRequestEntity.class,
//                JSONReader.Feature.SupportArrayToBean,
                JSONReader.Feature.SupportAutoType,
//                JSONReader.Feature.FieldBased,
//                JSONReader.Feature.SupportClassForName,
                JSONReader.Feature.Base64StringAsByteArray);

        // 使用Kryo序列化，听说比JDK序列化快
        requestEntity_deserializer = requestEntitySerialize(requestEntity);

        final RequestEntity<Object> requestEntity1 = HttpEntityConvertUtils.convert2RequestEntity(requestEntity_deserializer);
        final ResponseEntity<byte[]> responseEntity = restTemplate.exchange(requestEntity1, byte[].class);
        System.out.println(responseEntity);
        System.out.println(new String(responseEntity.getBody(),"UTF-8"));

    }

    @SneakyThrows
    public CrossRegionRequestEntity<Object> requestEntitySerialize(CrossRegionRequestEntity<Object> requestEntity){
        Kryo kryo = new Kryo();
        kryo.register(CrossRegionRequestEntity.class);
        kryo.register(LinkedMultiValueMap.class);
        kryo.register(ArrayList.class);
        kryo.register(HttpHeaders.class);
        kryo.register(HttpMethod.class);
        kryo.register(MyByteArrayResource.class);
        kryo.register(User.class);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Output output = new Output(byteArrayOutputStream);
        User user = User.builder().name("cly").age(28).build();

        kryo.writeClassAndObject(output,requestEntity);
        output.close();

        byte[] bytes = byteArrayOutputStream.toByteArray();
        System.out.println(new String(bytes,"UTF-8"));
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(bytes);
        Input input = new Input(byteInputStream);
        CrossRegionRequestEntity crossRegionRequestEntity = (CrossRegionRequestEntity) kryo.readClassAndObject(input);
        input.close();
        return crossRegionRequestEntity;
    }

}
