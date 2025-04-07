package com.modula.coreprocessor.service;

import com.modula.common.domain.workflow.step.Step;
import com.modula.coreprocessor.repository.StepRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StepService {

    private final StepRepository stepRepository;

    public Step getStepById(UUID id) {
        return stepRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("step with such id doesn't exist")
        );
    }

}
