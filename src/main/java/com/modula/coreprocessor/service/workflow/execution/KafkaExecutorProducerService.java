package com.modula.coreprocessor.service.workflow.execution;

import com.modula.coreprocessor.domain.dto.execution.IntegrationTask;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KafkaExecutorProducerService {

    private final KafkaTemplate<Object, Object> kafkaTemplate;

    public void sendTaskToIntegrationModule(UUID workflowInstanceId, Map<String, String> params, String stepSource) {
        IntegrationTask integrationTask = new IntegrationTask();
        integrationTask.setParams(params);
        integrationTask.setWorkflowInstanceId(workflowInstanceId);

        String[] source = stepSource.split(":");
        if (source.length != 2)
            throw new IllegalArgumentException("step source not valid");

        String moduleName = source[0];
        String actionName = source[1];
        integrationTask.setActionName(actionName);

        kafkaTemplate.send("integration-task-" + moduleName, integrationTask);
    }
}
