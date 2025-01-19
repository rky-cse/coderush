package me.rkycse.coderush.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

@Configuration
public class ExecutorConfig {


    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(6); // Minimum number of threads
        executor.setMaxPoolSize(12); // Maximum number of threads
        executor.setQueueCapacity(10000); // Queue size
        executor.setThreadNamePrefix("MyExecutor-");
        executor.initialize();
        return executor;
    }
}

