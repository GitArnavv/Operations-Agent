package com.aiops.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Asynchronous configuration for the Document Intelligence & OCR pipeline.
 * Configures a customized ThreadPoolTaskExecutor to asynchronously offload
 * document ingestion and Gemini Flash multimodal streaming.
 */
@Configuration
@EnableAsync
public class AsyncDocumentProcessingConfig {

    private static final Logger log = LoggerFactory.getLogger(AsyncDocumentProcessingConfig.class);

    public static final String DOCUMENT_OCR_TASK_EXECUTOR = "documentOcrTaskExecutor";

    private final org.springframework.core.task.TaskDecorator taskDecorator;

    public AsyncDocumentProcessingConfig(org.springframework.core.task.TaskDecorator taskDecorator) {
        this.taskDecorator = taskDecorator;
    }

    @Bean(name = DOCUMENT_OCR_TASK_EXECUTOR)
    public Executor documentOcrTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("ocr-worker-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.setTaskDecorator(taskDecorator);
        // If queue overflows under extreme burst, use CallerRunsPolicy so jobs are never dropped
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();

        log.info("Initialized custom ThreadPoolTaskExecutor [{}]: corePool=4, maxPool=10, queueCapacity=50, prefix='ocr-worker-', with MDC TaskDecorator",
                DOCUMENT_OCR_TASK_EXECUTOR);
        return executor;
    }
}
