package com.liaoyuan.cross.region.common.redis.serializer;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 使用Kryo序列化器和Gzip压缩
 * @author CLY
 * @date 2023/3/24 17:01
 **/
public class GzipRedisSerializer<T> extends KryoRedisSerializer<T> {
 
    @Override
    public T deserialize(byte[] bytes) {
        return super.deserialize(decompress(bytes));
    }
 
    @Override
    public byte[] serialize(T object) {
        return compress(super.serialize(object));
    }
 
 
    private byte[] compress(byte[] content) {
        byte[] ret = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            GZIPOutputStream gzipOutputStream= new GZIPOutputStream(byteArrayOutputStream);
            gzipOutputStream.write(content);
            //gzipOutputStream.flush();     //只调用flush不会刷新，压缩类型的流需要执行close或者finish才会完成
            gzipOutputStream.close();   //内部调用finish
 
            ret = byteArrayOutputStream.toByteArray();
//            System.out.println("gzip压缩后的大小："+ret.length+"字节");
            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();
        } catch (IOException e) {
            throw new SerializationException("Unable to compress data", e);
        }
        return ret;
    }
 
    private byte[] decompress(byte[] contentBytes) {
        byte[] ret = null;
        ByteArrayOutputStream out = null;
        try {
            out = new ByteArrayOutputStream();
            GZIPInputStream stream = new GZIPInputStream(new ByteArrayInputStream(contentBytes));
            IOUtils.copy(stream, out);
            stream.close();
 
            ret = out.toByteArray();
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new SerializationException("Unable to decompress data", e);
        }
        return ret;
    }
 
}