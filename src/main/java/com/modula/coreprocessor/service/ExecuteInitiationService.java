package com.modula.coreprocessor.service;

import com.modula.common.domain.workflow.Workflow;
import com.modula.coreprocessor.client.CoreBuilderClient;
import com.modula.coreprocessor.domain.dto.execution.StartConfDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExecuteInitiationService {

    private final CoreBuilderClient coreBuilderClient;
    private final WorkflowInterpreter workflowInterpreter;

    public void executeWorkflow(StartConfDTO startConfDTO) {
        UUID id = startConfDTO.getWorkflowId();
        Workflow workflow = coreBuilderClient.getWorkflow(id);

        if (workflow == null) return;

        workflowInterpreter.executeWorkflow(workflow);
    }
}
