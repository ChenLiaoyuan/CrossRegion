package com.liaoyuan.cross.region.module.low.rest;

import com.liaoyuan.cross.region.common.core.constants.Constants;
import com.liaoyuan.cross.region.common.core.model.CrossRegionRequestEntity;
import com.liaoyuan.cross.region.common.core.model.CrossRegionResponseEntity;
import com.liaoyuan.cross.region.common.core.model.SnowFlakeId;
import com.liaoyuan.cross.region.common.core.response.ResponseStatus;
import com.liaoyuan.cross.region.common.core.utils.HttpEntityConvertUtils;
import com.liaoyuan.cross.region.module.low.kafka.CrossRegionLowKafkaProducer;
import com.liaoyuan.cross.region.module.low.redis.CrossRegionMessageListener;
import com.liaoyuan.cross.region.module.low.service.ResponseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

/**
 * @author CLY
 * @date 2023/2/24 18:20
 **/
@Controller
@Slf4j
public class ProxyController {

    @Autowired
    private SnowFlakeId snowFlakeId;

    @Autowired
    private CrossRegionLowKafkaProducer crossRegionKafkaProducer;

    @Autowired
    private RedisTemplate<String,Object> jdkRedisTemplate;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * 第1步，将HttpServletRequest请求数据封装传输到kafka REQUEST主题
     * 代理，数据放在response返回
     * @param host 实际请求的域名，即从哪个域名的网页发过来的
     * @param realIP 实际请求的客户端ip
     * @param forwardFor 代理服务器的 IP 地址
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping("/proxy/**")
    public void proxy(@RequestHeader(name = "Target-Host", required = false) String targetHost, @RequestHeader(name = "X-Original-URL", required = false) String originalUrl, @RequestHeader(name = "Host", required = false) String host, @RequestHeader(name = "X-Real-IP", required = false) String realIP, @RequestHeader(name = "X-Forwarded-For", required = false) String forwardFor,
//                      @RequestBody Integer age,
                      HttpServletRequest request, HttpServletResponse response) throws IOException {

//        System.out.println("实际发起请求的域名："+host);
//        System.out.println("实际请求的客户端ip："+realIP);
//        System.out.println("代理服务器的IP地址："+forwardFor);
//        System.out.println("实际请求的url链接："+originalUrl+":"+request.getRequestURL());
//        System.out.println("要代理的目标域名："+targetHost);
//        System.out.println(request.getQueryString());
//        String url = targetHost+request.getRequestURI()+"?"+request.getQueryString();
//        System.out.println(url);

        String uri = request.getRequestURI();
        if (uri.contains("domp")){
            targetHost = targetHost == null ? "http://172.19.17.122:7300" : targetHost;
        }else if(uri.contains("gpsp")){
            targetHost = targetHost == null ? "http://172.19.17.73:17999" : targetHost;
        }else if(uri.contains("dmad")){
            targetHost = targetHost == null ? "http://172.19.17.73:8989" : targetHost;
        }else{
            targetHost = targetHost == null ? "http://172.19.17.73:8989" : targetHost;
        }

        // 先转换HttpServletRequest对象
        CrossRegionRequestEntity<Object> crossRegionRequestEntity = HttpEntityConvertUtils.convert2CrossRegionRequestEntity(request,targetHost);

//        if (crossRegionRequestEntity.getUrl().contains("websocket")){
//            return;
//        }

        String responseKey = snowFlakeId.nextStringId();
        log.info("第1步，将HttpServletRequest请求数据封装传输到kafka REQUEST主题。");
//        String jsonString = JSON.toJSONString(crossRegionRequestEntity,
//                JSONWriter.Feature.FieldBased, // 基于字段反序列化，如果不配置，会默认基于public的field和getter方法序列化。配置后，会基于非static的field（包括private）做反序列化。
//                JSONWriter.Feature.WriteClassName, // 序列化时输出类型信息，@type
//                JSONWriter.Feature.IgnoreNonFieldGetter, // 忽略非filed字段get方法的序列化
//                JSONWriter.Feature.IgnoreErrorGetter,  // 忽略错误的getter方法
//                JSONWriter.Feature.WriteBigDecimalAsPlain, // BigDecimal序列化为字符串
//                JSONWriter.Feature.WriteByteArrayAsBase64); // 将byte[]数组转化为base64编码
        crossRegionKafkaProducer.sendKafkaTopic(Constants.KAFKA_TOPIC_REQUEST,responseKey, crossRegionRequestEntity);
        // 添加key，用于获取通知，然后从redis获取响应结果
        CrossRegionMessageListener.addKey(responseKey);

        log.info("第4步，监听redis事件从redis集群获取响应结果。");
        synchronized (responseKey){
            try {
                responseKey.wait(Constants.PROXY_RESPONSE_TIMEOUT_MILLISECOND);
                // 如果还存在该key，说明超时了，还没返回响应结果
                if (CrossRegionMessageListener.containsKey(responseKey)){
                    CrossRegionMessageListener.remove(responseKey);
                    ResponseService.httpResponse(response, ResponseStatus.CROSS_REGION_RESPONSE_TIMEOUT);
                    return;
                }

//                String value = redisTemplate.opsForValue().get(responseKey).toString();
//                TypeReference<CrossRegionResponseEntity<byte[]>> typeReference = new TypeReference<CrossRegionResponseEntity<byte[]>>() {};
//                CrossRegionResponseEntity<byte[]> crossRegionResponseEntity = JSON.parseObject(value,typeReference);

                Object value = jdkRedisTemplate.opsForValue().get(responseKey);
                CrossRegionResponseEntity<byte[]> crossRegionResponseEntity = (CrossRegionResponseEntity<byte[]>)value;
                HttpEntityConvertUtils.convert2HttpResponse(crossRegionResponseEntity,response);
            } catch (Exception e) {
                log.error("获取跨区请求响应结果出错，请求url为：{}",crossRegionRequestEntity.getUrl(),e);
                ResponseService.httpResponse(response,ResponseStatus.CROSS_REGION_RESPONSE_ERROR);
            }

        }

//        final CrossRegionResponseEntity<byte[]> crossRegionResponseEntity = this.proxy(crossRegionRequestEntity);
    }

    private CrossRegionResponseEntity<byte[]> proxy(CrossRegionRequestEntity<Object> crossRegionRequestEntity){
//        final CrossRegionResponseEntity<byte[]> execute = httpClientProxy.execute(crossRegionRequestEntity);
        return null;
    }


    @GetMapping("/testProxyHtml")
    public void testProxyHtml(HttpServletResponse response,HttpServletRequest request,
                              @RequestHeader(value = "If-Modified-Since", required = false) Date ifModifiedSince,
                              @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch
                              ) throws IOException {
//        CrossRegionResponseEntity<byte[]> crossRegionResponseEntity = new CrossRegionResponseEntity<>();
//        crossRegionResponseEntity.setStatus(HttpStatus.OK);
//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.set("Content-Type","text/html");
//        httpHeaders.set("MyCookie","YangYuxian");
//        crossRegionResponseEntity.setHeaders(httpHeaders);

        // 使用协商缓存策略，当检测到资源没有变时，直接返回304，那么就不用传输数据回去，浏览器直接使用之前的缓存即可
        // 第一访问资源时返回值中添加Last-Modified或ETag头信息，
        // 浏览器再次请求该资源时，服务器需要根据请求头信息判断是否可以使用缓存。在Controller方法中添加If-Modified-Since或If-None-Match请求头信息
//        User user = userService.getUserById(id);
//        long lastModified = user.getLastModified();
//        String etag = user.getEtag();
        long lastModified = System.currentTimeMillis() - 24 * 60 * 60 * 1000;
        String etag = "432434245464dfdsfsgfd";
        if (ifModifiedSince != null && ifModifiedSince.getTime() >= lastModified) {
//            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
            response.setStatus(HttpStatus.NOT_MODIFIED.value());
            return;
        } else if (ifNoneMatch != null && ifNoneMatch.equals(etag)) {
//            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
            response.setStatus(HttpStatus.NOT_MODIFIED.value());
            return;
        }

//        if (1 == 1){
//            response.setStatus(HttpStatus.NOT_MODIFIED.value());
//            return;
//        }

        String html = "<html><body><h1>Hello, world!</h1></body></html>";
//        crossRegionResponseEntity.setBody(html.getBytes("UTF-8"));
//
//        HttpEntityConvertUtils.convert2HttpResponse(crossRegionResponseEntity,response);
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Encoding", "gzip");
        response.setHeader("Transfer-Encoding","chunked");

        OutputStream outputStream = response.getOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(outputStream);
        gzip.write(html.getBytes());
        gzip.finish();
//        gzip.close(); // 不直接close，因为使用chunked分片传输，最后需要传输'0\r\n'代表分块结束
//        outputStream.write(html.getBytes());
        outputStream.flush();
        outputStream.close();
    }

}
