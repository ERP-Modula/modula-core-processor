package com.modula.coreprocessor.service;

import com.modula.common.domain.workflow.execution.IntegrationOutputObject;
import com.modula.coreprocessor.repository.IntegrationOutputObjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IntegrationOutputObjectService {

    private final IntegrationOutputObjectRepository integrationOutputObjectRepository;

    public IntegrationOutputObject getIntegrationOutputObject(UUID id) {
        return integrationOutputObjectRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("output object with such id doesn't exist")
        );
    }
}
