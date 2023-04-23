package com.haiyisoft.cross.region.module.high.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author CLY
 * @date 2023/2/24
 **/
@Component
//@ConfigurationProperties(prefix = "okhttp3")
@Data
public class OkHttp3Config {

    /**
     * 内部执行http请求的实际线程池。
     * 核心线程数，默认为0
     */
    @Value("${okhttp3.thread-pool.core-pool-size}")
    private Integer corePoolSize;

    /**
     * 最大核心线程数，默认为 Integer.MAX_VALUE
     */
    @Value("${okhttp3.thread-pool.max-pool-size}")
    private Integer maxPoolSize;

    /**
     * 空闲线程存活时间，默认为60秒
     */
    @Value("${okhttp3.thread-pool.keep-alive-time-seconds}")
    private Integer threadPoolKeepAliveTimeSeconds;

    /**
     * 阻塞队列长度
     */
    @Value("${okhttp3.thread-pool.blocking-queue-size}")
    private Integer blockingQueueSize;

    /**
     * 请求分发，maxRequests和maxRequestPerHost是okhttp内部维持的请求队列，而executorservice是实际发送请求的线程。
     * 当前okhttpclient实例最大的并发请求数
     */
    @Value("${okhttp3.dispatcher.max-request}")
    private Integer maxRequest;

    /**
     * 单个主机最大请求并发数，这里的主机指被请求方主机，一般可以理解对调用方有限流作用。
     */
    @Value("${okhttp3.dispatcher.max-request-per-host}")
    private Integer maxRequestPerHost;


    /**
     * 连接池，每个TCP连接都会进行三次握手，并且因为TCP的拥塞控制使用的滑动窗口和慢开始算法导致网络带宽利用率不高。所以，在HTTP/2不可用时，OkHttp使用了连接池，避免为每个请求都创建连接。
     * 注意没有最大连接数，最大连接数受threadPool和dispatcher影响。
     * 最大空闲连接数，请求完后http连接不会立即关闭，少于指定的数量会存活到指定时间，后面来了相同地址的请求可以复用。
     */
    @Value("${okhttp3.connection-pool.max-idle-connection}")
    private Integer maxIdleConnection;

    /**
     * OkHttp在创建连接时，默认创建长连接，空闲连接存活时间
     */
    @Value("${okhttp3.connection-pool.keep-alive-seconds}")
    private Integer connectionPoolKeepAliveSeconds;

    /**
     * http连接超时时间，指定时间还没与服务端连接成功则报超时，默认为10秒
     */
    @Value("${okhttp3.connect-timeout-seconds}")
    private Integer connectTimeoutSeconds;

    /**
     * http读取服务端响应数据超时时间，指定时间服务端还没响应读取请求则报超时，默认为10秒
     */
    @Value("${okhttp3.read-timeout-seconds}")
    private Integer readTimeoutSeconds;

    /**
     * http将数据写入服务端超时时间，指定时间服务端还没响应写入请求则报超时，默认为10秒
     */
    @Value("${okhttp3.write-timeout-seconds}")
    private Integer writeTimeoutSeconds;

}
