package com.mindcluster.safediary.rutines.domain.repositories;

import com.mindcluster.safediary.rutines.domain.model.aggregates.PromptReflection;
import com.mindcluster.safediary.rutines.domain.model.aggregates.PromptReflection.ReflectionStatus;

import java.util.List;
import java.util.Optional;

public interface PromptReflectionRepository {
    PromptReflection save(PromptReflection reflection);
    Optional<PromptReflection> findById(Long id);
    
    Optional<PromptReflection> findByPatientIdAndStatus(Long patientId, ReflectionStatus status);
    
    List<PromptReflection> findAll();
    void delete(PromptReflection reflection);
}