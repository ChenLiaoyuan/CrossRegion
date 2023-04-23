package com.liaoyuan.cross.region.common.kafka.serialization;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.Charset;

/**
 * @author CLY
 * @date 2023/3/21 11:35
 **/
public class FastJson2JsonKafkaSerializer implements Serializer<Object> {
    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    @Override
    public byte[] serialize(String topic, Object data) {
        if (data == null)
        {
            return new byte[0];
        }
        final String jsonString = JSON.toJSONString(data, JSONWriter.Feature.WriteClassName);
        return jsonString.getBytes(DEFAULT_CHARSET);
    }
}
