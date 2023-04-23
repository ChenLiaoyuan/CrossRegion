package com.liaoyuan.cross.region.module.high.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置，用于执行RestTemplate代理
 * @author CLY
 * @date 2022/3/10 17:06
 **/
@Configuration
public class ThreadPoolLocalConfig {

    @Bean("threadPoolLocalTaskExecutor")
    @Primary
    public ThreadPoolTaskExecutor createTaskExecutor(){
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        // 线程池主要用于从Oracle获取数据再插入到MySQL中，属于IO密集型，此时线程都是处于等待中的，因此尽量配置多的线程
        // 参考公式：CPU核数 /（1 - 阻系数）
        // 比如16核CPU：16/(1 - 0.8 )= 80个线程数
        // 阻塞系数在0.8~0.9之间
        int processors = Runtime.getRuntime().availableProcessors();
//        int corePoolSize = (int) (processors / (1 - 0.8));
        int corePoolSize = 500;
        // 正式环境中为了避免频繁的关闭开启线程，直接设置核心线程=最大线程
        int maxPoolSize = 500;

        // 核心线程，32个，如果执行的线程大于32个，则放入队列
        threadPoolTaskExecutor.setCorePoolSize(corePoolSize);
        // 最大线程数量，如果队列也满了，则自动将核心线程扩展到最大线程，如果此时队列依然还是满了，则拒绝执行
        threadPoolTaskExecutor.setMaxPoolSize(maxPoolSize);
        // 线程队列数量，超过核心线程就将该线程放入队列
        threadPoolTaskExecutor.setQueueCapacity(200);
        // 用来设置线程池关闭的时候等待所有任务都完成再继续销毁其他的Bean
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        // 该方法用来设置线程池中任务的等待时间，如果超过这个时候还没有销毁就强制销毁，以确保应用最后能够被关闭，而不是阻塞住
        threadPoolTaskExecutor.setAwaitTerminationSeconds(60);
        // 允许核心线程超时
//        threadPoolTaskExecutor.setAllowCoreThreadTimeOut(true);
        // 空闲线程存活时间
        threadPoolTaskExecutor.setKeepAliveSeconds(60);
        // 线程名称前缀
        threadPoolTaskExecutor.setThreadNamePrefix("==>cross-region-high-proxy==>>");
        //修改拒绝策略为使用当前线程执行
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //初始化线程池
        threadPoolTaskExecutor.initialize();

        return threadPoolTaskExecutor;
    }

}
