package com.liaoyuan.cross.region.common.core.utils;

import com.liaoyuan.cross.region.common.core.exception.BaseException;
import com.liaoyuan.cross.region.common.core.model.CrossRegionRequestEntity;
import com.liaoyuan.cross.region.common.core.model.CrossRegionResponseEntity;
import com.liaoyuan.cross.region.common.core.model.MyByteArrayResource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * @author CLY
 * @date 2023/2/24 15:12
 **/
public class HttpEntityConvertUtils {

    /**
     * 将CrossRegionRequestEntity转换为不可变的RequestEntity，用于在高安全区重放请求
     * @param <T>
     * @return
     */
    public static <T> RequestEntity<T> convert2RequestEntity(CrossRegionRequestEntity<? extends T> crossRegionRequestEntity) {
        Assert.notNull(crossRegionRequestEntity.getMethod(),"请求方法不能为空");
        Assert.notNull(crossRegionRequestEntity.getUrl(),"请求url不能为空");

        RequestEntity<T> requestEntity = null;
        try {
            T body = crossRegionRequestEntity.getBody();;
            // 将MyByteArrayResource转换为ByteArrayResource，否则Spring无法识别为Multipart
            if (crossRegionRequestEntity.getHeaders().getContentType() != null && crossRegionRequestEntity.getHeaders().getContentType().includes(MediaType.MULTIPART_FORM_DATA) && body instanceof LinkedMultiValueMap){
                LinkedMultiValueMap<Object,Object> linkedHashMap = (LinkedMultiValueMap<Object,Object>)body;
                for (Map.Entry<Object, List<Object>> entry : linkedHashMap.entrySet()) {
                    List<Object> list = new ArrayList<>();
                    boolean flag = false;
                    for (Object value : entry.getValue()) {
                        if (value instanceof MyByteArrayResource){
                            flag = true;
                            MyByteArrayResource myByteArrayResource = (MyByteArrayResource)value;
                            list.add(new ByteArrayResource(myByteArrayResource.getByteArray()) {
                                @Override
                                public String getFilename() {
                                    return myByteArrayResource.getFileName();
                                }
                            });
                        }
                    }
                    if (flag){
                        linkedHashMap.put(entry.getKey(),list);
                    }
                }
            }

            requestEntity = new RequestEntity<>(body, crossRegionRequestEntity.getHeaders(), crossRegionRequestEntity.getMethod(), new URI(crossRegionRequestEntity.getUrl()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new BaseException("url转换为URI对象出错："+e.getMessage(),crossRegionRequestEntity.getUrl());
        }
        return requestEntity;
    }

    /**
     * 将不可变的ResponseEntity转换为crossRegionResponseEntity，用于将响应结果传回Redis集群
     * @param responseEntity
     * @param <T>
     * @return
     */
    public static <T> CrossRegionResponseEntity<T> convert2CrossRegionResponseEntity(ResponseEntity<? extends T> responseEntity){
        // 转换为可读写的HttpHeaders，而不是ReadOnlyHttpHeaders，否则不能反序列化
        HttpHeaders headers = new HttpHeaders();
        headers.addAll(responseEntity.getHeaders());

        CrossRegionResponseEntity<T> crossRegionResponseEntity = new CrossRegionResponseEntity<>(responseEntity.getStatusCode(), responseEntity.getBody(),headers);
        return crossRegionResponseEntity;
    }

    /**
     * 将HttpServletRequest转换为跨区请求CrossRegionRequestEntity，用于传输到Kafka
     * @param request
     * @return
     * @throws IOException
     */
    public static CrossRegionRequestEntity<Object> convert2CrossRegionRequestEntity(HttpServletRequest request,String targetHost) throws IOException {
        // 读取请求体
        Object body = null;
        // 1、如果Content-Type为application/x-www-form-urlencoded或者application/json等，那么请求数据是存放在request.getInputStream()的流中
        final ServletInputStream inputStream = request.getInputStream();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while((length = inputStream.read(buffer)) != -1){
            byteArrayOutputStream.write(buffer,0,length);
        }
        byte[] bodyByte = byteArrayOutputStream.toByteArray();

        // 2、如果Content-Type为multipart/form-data，那么请求数据是保存在request.getParameterMap()里的
        if(bodyByte.length <= 0){
            final UriComponents uriComponents = UriComponentsBuilder.fromUriString("?"+request.getQueryString()).build();
            final MultiValueMap<String, String> queryParams = uriComponents.getQueryParams();

            Enumeration<String> parameterNames = request.getParameterNames();
            MultiValueMap<String,Object> multiValueMap = new LinkedMultiValueMap<>();
            while (parameterNames.hasMoreElements()){
                final String parameterName = parameterNames.nextElement();
                // 避免和query的参数重复
                if (!queryParams.containsKey(parameterName)){
                    multiValueMap.addAll(parameterName, Arrays.asList(request.getParameterValues(parameterName)));
                }
            }

            if (request.getContentType() != null && request.getContentType().indexOf("multipart") > -1){
                MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) request;
                final Iterator<String> fileNames = multipartHttpServletRequest.getFileNames();
                while (fileNames.hasNext()){
                    final String fileName = fileNames.next();
                    for (MultipartFile multipartFile : multipartHttpServletRequest.getFiles(fileName)) {
//                        multiValueMap.add(fileName,new ByteArrayResource(multipartFile.getBytes()){
//                            @Override
//                            public String getFilename() {
//                                return multipartFile.getOriginalFilename();
//                            }
//                        });
//                        multiValueMap.add(fileName,multipartFile.getBytes());
                        multiValueMap.add(fileName,new MyByteArrayResource(multipartFile.getBytes(),multipartFile.getOriginalFilename()));
                    }
                }
            }

//            try(ByteArrayOutputStream byteArrayOutputStream1 = new ByteArrayOutputStream();
//                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream1)){
//                objectOutputStream.writeObject(multiValueMap);
//                body = byteArrayOutputStream1.toByteArray();
//            }
            if (multiValueMap.size() > 0){
                body = multiValueMap;
            }
        }else{
            body = bodyByte;
        }

        // 读取请求头
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()){
            final String headerName = headerNames.nextElement();
            headers.set(headerName,request.getHeader(headerName));
        }

        // 请求url
        String uri = request.getRequestURI().replace("/proxy", "");
        String url = targetHost+uri+"?"+request.getQueryString();
        System.out.println("代理的url为："+url);
        //        final String url = request.getRequestURL().toString(); // 这个是代理服务器的url，不是最终要访问的url
        // 请求方法
        final HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod());
        System.out.println("代理的方法为："+httpMethod);

        // 封装为跨区请求对象
        CrossRegionRequestEntity<Object> crossRegionRequestEntity = new CrossRegionRequestEntity<>(httpMethod,url,body,headers);
        return crossRegionRequestEntity;

    }



    /**
     * 将从Redis获取的跨区响应结果转换到HttpServletResponse，用于返回给用户
     * @param crossRegionResponseEntity
     * @param response
     * @throws IOException
     */
    public static void convert2HttpResponse(CrossRegionResponseEntity<byte[]> crossRegionResponseEntity, HttpServletResponse response) throws IOException {

        if (crossRegionResponseEntity.getStatusCodeValue() == HttpStatus.INTERNAL_SERVER_ERROR.value()){
            throw new BaseException("高安全区重放请求发生错误! ");
        }

        // 设置header
        final HttpHeaders headers = crossRegionResponseEntity.getHeaders();
        for(Map.Entry<String, List<String>> header: headers.entrySet()){
            for(String headerValue : header.getValue()){
                // 不设置Connection头部，restTemplate响应的实体Connection可能会为Close（即不是keep-alive，不是长连接），
                // 那么就会导致将数据返回给用户浏览器或者nginx代理也不能使用长连接，无法正常传输内容产生错误。
//                if(!"Connection".equals(header.getKey())
                    // 设置Transfer-Encoding为chunked需要nginx代理开启HTTP 1.1，默认是不开启的，不开启会出错。
//                        && !"Transfer-Encoding".equals(header.getKey())
//                ){
//                System.out.println("response的响应头为："+header.getKey()+":"+headerValue);
                    // 注意不是setHeader，而是addHeader，setHeader会覆盖
                    response.addHeader(header.getKey(),headerValue);
//                }
            }
        }

        for (String headerName : response.getHeaderNames()) {
            System.out.println("response的响应头为："+headerName+":"+response.getHeaders(headerName));
        }
//        System.out.println("响应头为："+headers);

        // 设置status
        response.setStatus(crossRegionResponseEntity.getStatusCodeValue());

//        response.setContentType("text/html");
//        response.setCharacterEncoding("UTF-8");

//        System.out.println("请求成功："+new String(crossRegionResponseEntity.getBody(),"UTF-8"));

        // 设置body，不为null才设置，因为有可能是304，304不需要设置body
        if (crossRegionResponseEntity.getBody() != null){
            OutputStream outputStream = response.getOutputStream();
            outputStream.write(crossRegionResponseEntity.getBody());
    //        final Object body = crossRegionResponseEntity.getBody();
    //        if (body instanceof String){
    //            outputStream.write(((String) body).getBytes("UTF-8"));
    //        }else if(body instanceof byte[]){
    //            outputStream.write((byte[]) body);
    //        }
            outputStream.flush();
            outputStream.close();
        }

    }

}
