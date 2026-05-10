package com.jieni.mqxq.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置类
 * 用于配置系统异步任务执行的线程池，提高系统并发处理能力
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Configuration
public class ThreadPoolConfig {
    
    /** 核心线程池大小 - 始终保持活跃的线程数量 */
    private static final int corePoolSize = 20;

    /** 最大可创建的线程数 - 线程池能创建的最大线程数量 */
    private static final int maxPoolSize = 40;

    /** 队列最大长度 - 等待执行任务的队列容量 */
    private static final int queueCapacity = 1000;

    /** 线程池维护线程所允许的空闲时间（秒） - 超过核心线程数的线程空闲超时时间 */
    private static final int keepAliveSeconds = 300;

    /**
     * 配置线程池任务执行器
     * 用于处理异步任务，如邮件发送、文件处理、日志记录等
     * 
     * @return ThreadPoolTaskExecutor 配置好的线程池执行器
     */
    @Bean(name = "threadPoolTaskExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 设置线程名称前缀，便于日志追踪和问题排查
        executor.setThreadNamePrefix("MQXQ-ThreadPool-");
        
        // 设置核心线程数
        executor.setCorePoolSize(corePoolSize);
        
        // 设置最大线程数
        executor.setMaxPoolSize(maxPoolSize);
        
        // 设置队列容量
        executor.setQueueCapacity(queueCapacity);
        
        // 设置线程空闲时间
        executor.setKeepAliveSeconds(keepAliveSeconds);
        
        // 线程池对拒绝任务(无线程可用)的处理策略
        // CallerRunsPolicy: 由调用线程处理该任务，保证任务不丢失
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        return executor;
    }
}
