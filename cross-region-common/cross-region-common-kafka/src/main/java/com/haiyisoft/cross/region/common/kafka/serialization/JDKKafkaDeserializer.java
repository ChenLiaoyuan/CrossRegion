package com.haiyisoft.cross.region.common.kafka.serialization;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.haiyisoft.cross.region.common.core.utils.SerializerUtils;
import org.apache.kafka.common.serialization.Deserializer;

import java.nio.charset.Charset;

/**
 * kafka使用FastJson序列化
 * 
 */
public class JDKKafkaDeserializer implements Deserializer<Object>
{

    @Override
    public Object deserialize(String topic, byte[] bytes) {
        if (bytes == null || bytes.length <= 0)
        {
            return null;
        }
        return SerializerUtils.deserialize(bytes);
    }
}
