package com.haiyisoft.cross.region.module.high.rest;

import com.haiyisoft.cross.region.common.core.model.CrossRegionRequestEntity;
import com.haiyisoft.cross.region.common.core.model.CrossRegionResponseEntity;
import com.haiyisoft.cross.region.common.core.utils.HttpEntityConvertUtils;
import com.haiyisoft.cross.region.module.high.httpclient.HttpClientProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
public class TestProxyController {

    @Autowired
    private HttpClientProxy httpClientProxy;

    /**
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

        targetHost = targetHost == null ? "http://localhost:8060" : targetHost;

        final CrossRegionRequestEntity<Object> crossRegionRequestEntity = HttpEntityConvertUtils.convert2CrossRegionRequestEntity(request,targetHost);
        final CrossRegionResponseEntity<byte[]> crossRegionResponseEntity = this.proxy(crossRegionRequestEntity);
        HttpEntityConvertUtils.convert2HttpResponse(crossRegionResponseEntity,response);
    }

    private CrossRegionResponseEntity<byte[]> proxy(CrossRegionRequestEntity<Object> crossRegionRequestEntity){
        final CrossRegionResponseEntity<byte[]> execute = httpClientProxy.execute(crossRegionRequestEntity);
        return execute;
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
