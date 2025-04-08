package com.modula.coreprocessor.domain.dto.execution;

import com.modula.common.domain.workflow.execution.IntegrationOutputObject;
import lombok.*;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutorTask {
    private UUID workflowInstanceId;
    private Boolean isFirstStep;
    private IntegrationOutputObject integrationOutput;
}
