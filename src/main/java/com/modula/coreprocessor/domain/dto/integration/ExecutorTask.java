package com.modula.coreprocessor.domain.dto.integration;

import com.modula.common.domain.workflow.execution.IntegrationOutputObject;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ExecutorTask {
    private UUID workflowInstanceId;
    private Boolean isFirstStep;
    private IntegrationOutputObject integrationOutput;
}
