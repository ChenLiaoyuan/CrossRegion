package com.haiyisoft.cross.region.module.high.config;

import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.concurrent.*;

/**
 * @author CLY
 * @date 2023/2/24 11:28
 **/
@Configuration
public class RestTemplateConfig {

    @Autowired
    private OkHttp3Config okHttp3Config;

    /**
     * 创建一个RestTemplate实例，并设置它的请求工厂为OkHttp3ClientHttpRequestFactory
     * @return
     */
    @Bean
    public RestTemplate restTemplateWithOkHttpClient(){

        ArrayBlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<>(okHttp3Config.getBlockingQueueSize());
        final BasicThreadFactory threadFactory = new BasicThreadFactory.Builder().namingPattern("CLY-OKHTTP3-").build();

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                okHttp3Config.getCorePoolSize(), okHttp3Config.getMaxPoolSize(),
                okHttp3Config.getThreadPoolKeepAliveTimeSeconds(), TimeUnit.SECONDS,
                // 使用拒绝策略，队列都满了，则抛出RejectedExecutionException异常
                blockingQueue, threadFactory, new ThreadPoolExecutor.AbortPolicy());

        // 调度线程池，用于发送http请求
        Dispatcher dispatcher = new Dispatcher(threadPoolExecutor);
        dispatcher.setMaxRequests(okHttp3Config.getMaxRequest());
        dispatcher.setMaxRequestsPerHost(okHttp3Config.getMaxRequestPerHost());

        // http连接池，keep-alive相同请求地址可复用http连接
        final ConnectionPool connectionPool = new ConnectionPool(okHttp3Config.getMaxIdleConnection(), okHttp3Config.getThreadPoolKeepAliveTimeSeconds(), TimeUnit.SECONDS);

        // 创建OkHttp客户端
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(okHttp3Config.getConnectTimeoutSeconds()))
                .readTimeout(Duration.ofSeconds(okHttp3Config.getReadTimeoutSeconds()))
                .writeTimeout(Duration.ofSeconds(okHttp3Config.getWriteTimeoutSeconds()))
                .connectionPool(connectionPool)
                .dispatcher(dispatcher)
                .retryOnConnectionFailure(true)
                .build();

        // RestTemplate使用配置好的OkHttpClient来发送请求
        final OkHttp3ClientHttpRequestFactory okHttp3ClientHttpRequestFactory = new OkHttp3ClientHttpRequestFactory(okHttpClient);
        return new RestTemplate(okHttp3ClientHttpRequestFactory);
    }


}
