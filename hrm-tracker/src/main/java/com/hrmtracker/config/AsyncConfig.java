package com.hrmtracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "fileUploadExecutor")
    public Executor fileUploadExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3); // Minimum threads
        executor.setMaxPoolSize(6);  // Maximum threads
        executor.setQueueCapacity(20); // Kitne pending uploads allow
        executor.setThreadNamePrefix("FileUploadWorker-");
        executor.initialize();
        return executor;
    }
}
