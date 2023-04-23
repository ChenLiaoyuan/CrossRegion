package com.liaoyuan.cross.region.common.kafka.serialization;

import com.liaoyuan.cross.region.common.core.utils.SerializerUtils;
import org.apache.kafka.common.serialization.Deserializer;

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
