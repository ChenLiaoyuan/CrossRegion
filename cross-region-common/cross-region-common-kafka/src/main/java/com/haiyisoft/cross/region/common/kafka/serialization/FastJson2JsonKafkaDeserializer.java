package com.haiyisoft.cross.region.common.kafka.serialization;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.apache.kafka.common.serialization.Deserializer;

import java.nio.charset.Charset;

/**
 * kafka使用FastJson序列化
 * 
 */
public class FastJson2JsonKafkaDeserializer implements Deserializer<Object>
{
    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    @Override
    public Object deserialize(String topic, byte[] bytes) {
        if (bytes == null || bytes.length <= 0)
        {
            return null;
        }
        String str = new String(bytes, DEFAULT_CHARSET);

        return JSON.parseObject(str, Object.class, JSONReader.Feature.SupportAutoType);
    }
}
