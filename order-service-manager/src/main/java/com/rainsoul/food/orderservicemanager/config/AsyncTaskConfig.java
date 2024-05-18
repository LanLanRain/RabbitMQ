package com.rainsoul.food.orderservicemanager.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncTaskConfig implements AsyncConfigurer {

    /**
     * 获取一个用于异步执行任务的线程池执行器。
     * 这个方法配置了一个线程池任务执行器（ThreadPoolTaskExecutor），用于处理异步任务。
     * 它设置了线程池的核心大小、最大大小、队列容量等参数，并初始化执行器。
     *
     * @return Executor 返回配置好的线程池执行器实例。
     */
    @Bean
    public Executor getAsyncExecutor() {
        // 创建线程池任务执行器实例
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();

        // 配置线程池基本参数
        threadPoolTaskExecutor.setCorePoolSize(10); // 设置核心线程数为10
        threadPoolTaskExecutor.setMaxPoolSize(100); // 设置最大线程数为100
        threadPoolTaskExecutor.setQueueCapacity(10); // 设置队列容量为10

        // 配置线程池在关闭时的等待行为
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true); // 线程池关闭时等待所有任务完成
        threadPoolTaskExecutor.setAwaitTerminationSeconds(60); // 等待时间设置为60秒

        // 设置线程名称前缀
        threadPoolTaskExecutor.setThreadNamePrefix("RabbitMQ-Async-");

        // 初始化线程池执行器
        threadPoolTaskExecutor.initialize();

        return threadPoolTaskExecutor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return null;
    }
}
