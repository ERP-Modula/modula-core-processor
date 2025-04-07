package com.modula.coreprocessor.domain.dto.integration;

import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class IntegrationTask {
    private UUID workflowInstanceId;
    Map<String, String> params;
}
