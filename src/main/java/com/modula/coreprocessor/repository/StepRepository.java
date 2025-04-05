package com.modula.coreprocessor.repository;

import com.modula.common.domain.workflow.step.Step;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StepRepository extends JpaRepository<Step, UUID> {
}
