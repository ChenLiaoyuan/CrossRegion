package com.haiyisoft.cross.region.common.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * http response实体
 * @author CLY
 * @date 2023/2/24 14:24
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrossRegionResponseEntity<T> extends CrossRegionHttpEntity<T>{

    private static final long serialVersionUID = 1L;
    private Object status;

    public CrossRegionResponseEntity(Object status,T body,HttpHeaders headers){
        super(body, headers);
        this.status = status;
    }

    /**
     * Return the HTTP status code of the response.
     * @return the HTTP status as an HttpStatus enum entry
     */
    public HttpStatus getStatusCode() {
        if (this.status instanceof HttpStatus) {
            return (HttpStatus) this.status;
        } else if (this.status instanceof String){
            return HttpStatus.valueOf((String) this.status);
        }
        else {
            return HttpStatus.valueOf((Integer) this.status);
        }
    }

    /**
     * Return the HTTP status code of the response.
     * @return the HTTP status as an int value
     * @since 4.3
     */
    public int getStatusCodeValue() {
        if (this.status instanceof HttpStatus) {
            return ((HttpStatus) this.status).value();
        } else if (this.status instanceof String){
            return HttpStatus.valueOf((String) this.status).value();
        }else {
            return (Integer) this.status;
        }
    }


    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (!super.equals(other)) {
            return false;
        }
        CrossRegionResponseEntity<?> otherEntity = (CrossRegionResponseEntity<?>) other;
        return ObjectUtils.nullSafeEquals(this.status, otherEntity.status);
    }

    @Override
    public int hashCode() {
        return (29 * super.hashCode() + ObjectUtils.nullSafeHashCode(this.status));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("<");
        builder.append(this.status);
        if (this.status instanceof HttpStatus) {
            builder.append(' ');
            builder.append(((HttpStatus) this.status).getReasonPhrase());
        }
        builder.append(',');
        T body = getBody();
        HttpHeaders headers = getHeaders();
        if (body != null) {
            builder.append(body);
            builder.append(',');
        }
        builder.append(headers);
        builder.append('>');
        return builder.toString();
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
