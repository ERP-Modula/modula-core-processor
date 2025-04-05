package com.modula.coreprocessor.controller;

import com.modula.coreprocessor.domain.dto.execution.StartConfDTO;
import com.modula.coreprocessor.service.ExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/execution")
@RequiredArgsConstructor
public class ExecutionController {

    private final ExecutionService executionService;

    @PostMapping
    public void startWorkflow(StartConfDTO startConfDTO) {
        executionService.executeWorkflow(startConfDTO);
    }
}
