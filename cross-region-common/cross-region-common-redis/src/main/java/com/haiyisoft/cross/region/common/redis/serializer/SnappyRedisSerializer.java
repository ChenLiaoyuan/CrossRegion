package com.haiyisoft.cross.region.common.redis.serializer;

import org.springframework.data.redis.serializer.SerializationException;
import org.xerial.snappy.Snappy;

import java.io.IOException;

/**
 * 先使用Kryo序列化，再使用Snappy压缩
 * @param <T>
 */
public class SnappyRedisSerializer<T> extends KryoRedisSerializer<T> {
 
    @Override
    public T deserialize(byte[] bytes) throws SerializationException{
        try {
            return super.deserialize(Snappy.uncompress(bytes));
        } catch (IOException e) {
            e.printStackTrace();
            throw new SerializationException(e.getMessage(),e);
        }
    }
 
    @Override
    public byte[] serialize(T object) throws SerializationException{
        try {
            final byte[] compress = Snappy.compress(super.serialize(object));
//            System.out.println("Snappy压缩后的大小："+compress.length+"字节");
            return compress;
        } catch (IOException e) {
            e.printStackTrace();
            throw new SerializationException(e.getMessage(),e);
        }
    }
 
}