package com.modula.coreprocessor.repository;

import com.modula.common.domain.workflow.execution.IntegrationOutputObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IntegrationOutputObjectRepository extends JpaRepository<IntegrationOutputObject, UUID> {
}
