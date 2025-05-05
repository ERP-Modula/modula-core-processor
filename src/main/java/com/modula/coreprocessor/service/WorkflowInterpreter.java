package com.modula.coreprocessor.service;

import com.modula.common.domain.workflow.Workflow;
import com.modula.common.domain.workflow.execution.WorkflowInstance;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkflowInterpreter {

    private final WorkflowInstanceService workflowInstanceService;
    private final KafkaInterpreterProducerService kafkaProducerService;

    @Transactional
    public void executeWorkflow(Workflow workflow) {
        WorkflowInstance workflowInstance = workflowInstanceService.createInstance(workflow);
        workflowInstance = workflowInstanceService.saveInstance(workflowInstance);

        // send to kafka
        kafkaProducerService.startWorkflowExecution(workflowInstance.getId());
    }
}
