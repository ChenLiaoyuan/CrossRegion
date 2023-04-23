package com.liaoyuan.cross.region.common.core.utils;

import com.liaoyuan.cross.region.common.core.exception.BaseException;

import java.io.*;

/**
 * 序列化工具
 * @author CLY
 * @date 2023/3/23 12:02
 **/
public class SerializerUtils {

    /**
     * 将对象序列化为二进制数组byte[]
     * @param object
     * @return
     * @throws IOException
     */
    public static byte[] serialize(Object object) {
        try{
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
            objectOutputStream.close();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            throw new BaseException(e.getMessage());
        }
    }

    /**
     * 将二进制数组byte[]反序列化为对象，并转换类型
     * @param bytes
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T deserialize(byte[] bytes, Class<T> type) {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            T cast = type.cast(objectInputStream.readObject());
            return cast;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new BaseException(e.getMessage());
        }
    }

    /**
     * 将二进制数组byte[]反序列化为对象
     * @param bytes
     * @return
     */
    public static Object deserialize(byte[] bytes) {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            Object object = objectInputStream.readObject();
            return object;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new BaseException(e.getMessage());
        }
    }

}
