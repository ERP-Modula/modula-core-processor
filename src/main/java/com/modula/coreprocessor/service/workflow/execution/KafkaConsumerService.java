package com.modula.coreprocessor.service.workflow.execution;

import com.modula.coreprocessor.domain.dto.execution.ExecutorTask;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final WorkflowExecutor workflowExecutor;

    @KafkaListener(
            id = "consumer-group-1",
            topics = "${kafka.topics.workflow-executor-topic}")
    public void handle(@Payload ExecutorTask task) {
        workflowExecutor.handleTask(task);
    }
}
