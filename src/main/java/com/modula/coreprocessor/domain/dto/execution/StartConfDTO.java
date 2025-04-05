package com.modula.coreprocessor.domain.dto.execution;

import lombok.Data;

import java.util.UUID;

@Data
public class StartConfDTO {
    private UUID workflowId;
}
