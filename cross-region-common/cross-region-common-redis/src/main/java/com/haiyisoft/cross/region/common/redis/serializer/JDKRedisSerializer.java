package com.haiyisoft.cross.region.common.redis.serializer;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.haiyisoft.cross.region.common.core.utils.SerializerUtils;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * Redis使用JDK序列化
 * 
 * @author ruoyi
 */
public class JDKRedisSerializer implements RedisSerializer<Object>
{

    @Override
    public byte[] serialize(Object t) throws SerializationException
    {
        if (t == null)
        {
            return new byte[0];
        }
        byte[] serialize = SerializerUtils.serialize(t);
//        System.out.println("JDK序列化未压缩的大小："+serialize.length+"字节");
        return serialize;
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException
    {
        if (bytes == null || bytes.length <= 0)
        {
            return null;
        }
        return SerializerUtils.deserialize(bytes);
    }
}
