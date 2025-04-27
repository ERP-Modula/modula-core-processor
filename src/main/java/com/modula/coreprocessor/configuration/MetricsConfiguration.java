package com.modula.coreprocessor.configuration;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@RequiredArgsConstructor
@Configuration
public class MetricsConfiguration {

    private final MeterRegistry meterRegistry;
    private final ThreadPoolTaskExecutor taskExecutor;

    @PostConstruct
    public void registerCustomMetrics() {
        Gauge.builder("executor.active", taskExecutor, ThreadPoolTaskExecutor::getActiveCount)
                .description("Активные потоки")
                .register(meterRegistry);

        Gauge.builder("executor.pool.size", taskExecutor, ThreadPoolTaskExecutor::getPoolSize)
                .description("Текущий размер пула")
                .register(meterRegistry);
    }
}
