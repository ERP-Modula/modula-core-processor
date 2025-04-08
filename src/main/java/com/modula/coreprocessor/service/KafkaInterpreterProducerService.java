package com.modula.coreprocessor.service;

import com.modula.coreprocessor.domain.dto.execution.ExecutorTask;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KafkaInterpreterProducerService {

    @Value("${kafka.topics.workflow-executor-topic}")
    private String execTopic;

    private final KafkaTemplate<Object, Object> kafkaTemplate;

    public void startWorkflowExecution(UUID workflowInstanceID) {
        ExecutorTask executorTask = ExecutorTask.builder()
                .workflowInstanceId(workflowInstanceID)
                .isFirstStep(true)
                .integrationOutput(null)
                .build();
        kafkaTemplate.send(execTopic, executorTask);
    }
}
