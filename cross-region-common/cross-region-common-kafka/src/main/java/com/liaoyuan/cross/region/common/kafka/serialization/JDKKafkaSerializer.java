package com.liaoyuan.cross.region.common.kafka.serialization;

import com.liaoyuan.cross.region.common.core.utils.SerializerUtils;
import org.apache.kafka.common.serialization.Serializer;

/**
 * 使用JDK序列化
 * @author CLY
 * @date 2023/3/21 11:35
 **/
public class JDKKafkaSerializer implements Serializer<Object> {

    @Override
    public byte[] serialize(String topic, Object data) {
        if (data == null)
        {
            return new byte[0];
        }

        return SerializerUtils.serialize(data);
    }
}
