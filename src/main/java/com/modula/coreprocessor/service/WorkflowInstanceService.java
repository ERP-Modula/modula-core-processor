package com.modula.coreprocessor.service;

import com.modula.common.domain.workflow.Workflow;
import com.modula.common.domain.workflow.execution.WorkflowInstance;
import com.modula.coreprocessor.repository.StepRepository;
import com.modula.coreprocessor.repository.WorkflowInstanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkflowInstanceService {

    private final WorkflowInstanceRepository workflowInstanceRepository;

    private final StepService stepService;
    private final IntegrationOutputObjectService integrationOutputObjectService;

    public WorkflowInstance saveInstance(WorkflowInstance instance) {
        return workflowInstanceRepository.save(instance);
    }

    public WorkflowInstance getInstance(UUID instanceId) {
        return workflowInstanceRepository.findById(instanceId).orElseThrow(
                () -> new IllegalArgumentException("workflow instance with such id doesn't exist")
        );
    }

    public WorkflowInstance createInstance(Workflow workflow) {
        WorkflowInstance instance = new WorkflowInstance();

        instance.setWorkflowId(workflow.getId());
        instance.setSteps(
                workflow.getSteps().stream()
                        .map(step -> stepService.getStepById(step.getId()))
                        .collect(Collectors.toList())
        );
        instance.setCurrentStepId(null);
        instance.setIsRoot(true);
        instance.setStartTime(new java.sql.Date(System.currentTimeMillis()));

        instance.setContext(new ArrayList<>());

        return instance;
    }

    public WorkflowInstance cloneInstance(WorkflowInstance workflowInstance, UUID currStepId) {
        WorkflowInstance newInstance = new WorkflowInstance();

        newInstance.setWorkflowId(workflowInstance.getWorkflowId());
        newInstance.setSteps(
                workflowInstance.getSteps().stream()
                        .map(step -> stepService.getStepById(step.getId()))
                        .collect(Collectors.toList())
        );
        newInstance.setCurrentStepId(currStepId);

        newInstance.setContext(
                workflowInstance.getContext().stream()
                        .map(object -> integrationOutputObjectService.getIntegrationOutputObject(object.getId()))
                        .collect(Collectors.toList())
        );

        newInstance.setIsRoot(false);
        workflowInstance.getSub().add(newInstance);

        saveInstance(workflowInstance);

        return newInstance;
    }
}
