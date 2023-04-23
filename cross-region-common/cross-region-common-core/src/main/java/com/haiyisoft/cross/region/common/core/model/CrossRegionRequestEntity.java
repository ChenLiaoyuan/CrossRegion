package com.haiyisoft.cross.region.common.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * http request请求实体
 * @author CLY
 * @date 2023/2/24 14:22
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrossRegionRequestEntity<T> extends CrossRegionHttpEntity<T>{

    private static final long serialVersionUID = 1L;

    private HttpMethod method;
    private String url;

    public CrossRegionRequestEntity(HttpMethod method,String url,T body,HttpHeaders headers){
        super(body, headers);
        this.method = method;
        this.url = url;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (!super.equals(other)) {
            return false;
        }
        CrossRegionRequestEntity<?> otherEntity = (CrossRegionRequestEntity<?>) other;
        return (ObjectUtils.nullSafeEquals(this.method, otherEntity.method) &&
                ObjectUtils.nullSafeEquals(this.url, otherEntity.url));
    }

    @Override
    public int hashCode() {
        int hashCode = super.hashCode();
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.method);
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.url);
        return hashCode;
    }

    @Override
    public String toString() {
        return format(getMethod(), getUrl().toString(), getBody(), getHeaders());
    }

    static <T> String format(@Nullable HttpMethod httpMethod, String url, @Nullable T body, HttpHeaders headers) {
        StringBuilder builder = new StringBuilder("<");
        builder.append(httpMethod);
        builder.append(' ');
        builder.append(url);
        builder.append(',');
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
