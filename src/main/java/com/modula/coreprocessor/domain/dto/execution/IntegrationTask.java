package com.modula.coreprocessor.domain.dto.execution;

import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class IntegrationTask {
    private UUID workflowInstanceId;
    private String actionName;
    private Map<String, String> params;
}
