package com.haiyisoft.crossregion.test;

import com.liaoyuan.cross.region.common.core.model.MyByteArrayResource;
import com.liaoyuan.cross.region.common.core.utils.SerializerUtils;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;

/**
 * @author CLY
 * @date 2023/3/23 16:25
 **/
public class SerializerTest {

    @Test
    public void testSerialize() throws UnsupportedEncodingException {
        MyByteArrayResource myByteArrayResource = new MyByteArrayResource("test".getBytes("UTF-8"),"text.txt");

        final byte[] serialize = SerializerUtils.serialize(myByteArrayResource);
        final MyByteArrayResource deserialize = (MyByteArrayResource)SerializerUtils.deserialize(serialize);
        System.out.println(deserialize.getFileName());
    }
}
