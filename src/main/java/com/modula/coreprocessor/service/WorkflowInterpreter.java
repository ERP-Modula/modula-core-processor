package com.modula.coreprocessor.service;

import com.modula.common.domain.workflow.Workflow;
import com.modula.common.domain.workflow.execution.WorkflowInstance;
import com.modula.coreprocessor.repository.StepRepository;
import com.modula.coreprocessor.repository.WorkflowInstanceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkflowInterpreter {

    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final StepRepository stepRepository;

    @Transactional
    public void executeWorkflow(Workflow workflow) {
        WorkflowInstance workflowInstance = createInstance(workflow);

        workflowInstanceRepository.save(workflowInstance);
        // send to kafka
    }

    private WorkflowInstance createInstance(Workflow workflow) {
        WorkflowInstance instance = new WorkflowInstance();

        instance.setWorkflowId(workflow.getId());
        instance.setSteps(
                workflow.getSteps().stream()
                .map(step -> stepRepository.findById(step.getId()).orElseThrow())
                .collect(Collectors.toList())
        );
        instance.setCurrentStepId(instance.getFirstStepId());


        instance.setContext(new ArrayList<>());

        return instance;
    }
}
