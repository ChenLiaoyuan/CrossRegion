package com.liaoyuan.cross.region.common.core.model;

import lombok.Data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author CLY
 * @date 2023/3/23 15:16
 **/
@Data
public class MyByteArrayResource implements Serializable {
    private static final long serialVersionUID = 1L;

    private String fileName;
    private byte[] byteArray;

    public MyByteArrayResource(byte[] byteArray) {
        this.byteArray = byteArray;
    }

    public MyByteArrayResource(byte[] byteArray, String fileName) {
        this(byteArray);
        this.fileName = fileName;
    }

    /**
     * 将对象序列化
     * @param outputStream
     * @throws IOException
     */
    private void writeObject(ObjectOutputStream outputStream) throws IOException {
        outputStream.defaultWriteObject();
    }

    /**
     * 反序列化读取对象
     * @param inputStream
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        inputStream.defaultReadObject();
    }

}
