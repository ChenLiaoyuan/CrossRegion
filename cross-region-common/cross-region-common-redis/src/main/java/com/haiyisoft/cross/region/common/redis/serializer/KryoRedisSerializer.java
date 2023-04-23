package com.haiyisoft.cross.region.common.redis.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;
import com.haiyisoft.cross.region.common.core.model.CrossRegionRequestEntity;
import com.haiyisoft.cross.region.common.core.model.MyByteArrayResource;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

/**
 * @author CLY
 * @date 2023/3/24 18:11
 **/
public class KryoRedisSerializer<T> implements RedisSerializer<T> {
    private static final int MAXIMUM_POOL_SIZE = 100;
    private static final byte[] EMPTY_BYTE = new byte[0];
    private static final int BUFFER_SIZE = 1024;

    private static final Pool<Kryo> KRYO_POOL = new Pool<Kryo>(true,false,MAXIMUM_POOL_SIZE) {
        @Override
        protected Kryo create() {
            Kryo kryo = new Kryo();
            kryo.setRegistrationRequired(false);
            kryo.register(CrossRegionRequestEntity.class);
            kryo.register(LinkedMultiValueMap.class);
            kryo.register(ArrayList.class);
            kryo.register(HttpHeaders.class);
            kryo.register(HttpMethod.class);
            kryo.register(MyByteArrayResource.class);
            return kryo;
        }
    };

    private static final Pool<Output> OUTPUT_POOL = new Pool<Output>(true,false,MAXIMUM_POOL_SIZE) {
        @Override
        protected Output create() {
            return new Output(BUFFER_SIZE,-1);
        }
    };

    private static final Pool<Input> INPUT_POOL = new Pool<Input>(true,false,MAXIMUM_POOL_SIZE) {
        @Override
        protected Input create() {
            return new Input(BUFFER_SIZE);
        }
    };

    @Override
    public byte[] serialize(T t) throws SerializationException {
        if (t == null){
            return EMPTY_BYTE;
        }
        Kryo kryo = KRYO_POOL.obtain();
        Output output = OUTPUT_POOL.obtain();
        try{
            kryo.writeClassAndObject(output,t);
            output.flush();
            final byte[] bytes = output.toBytes();
//            System.out.println("Kryo序列化未压缩的大小："+bytes.length+"字节");
            return bytes;
        }finally{
            KRYO_POOL.free(kryo);
            OUTPUT_POOL.free(output);
        }
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException{
        if (null == bytes || bytes.length <= 0) {
            return null;
        }
        Kryo kryo = KRYO_POOL.obtain();
        Input input = INPUT_POOL.obtain();
        try{
            input.setInputStream(new ByteArrayInputStream(bytes));
            T target = (T) kryo.readClassAndObject(input);
            return target;
        }finally{
            KRYO_POOL.free(kryo);
            INPUT_POOL.free(input);
        }
    }


}
