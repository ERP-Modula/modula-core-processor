package com.modula.coreprocessor.client;

import com.modula.common.domain.workflow.Workflow;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "core-builder-client", url = "${feign.clients.core-builder.url}")
public interface CoreBuilderClient {

    @GetMapping("api/v1/workflows/{workflowId}")
    Workflow getWorkflow(@PathVariable UUID workflowId);
}
