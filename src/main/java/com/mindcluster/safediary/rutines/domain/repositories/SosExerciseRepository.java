package com.mindcluster.safediary.rutines.domain.repositories;

import com.mindcluster.safediary.rutines.domain.model.aggregates.SosExercise;

import java.util.List;
import java.util.Optional;

public interface SosExerciseRepository {
    SosExercise save(SosExercise exercise);
    Optional<SosExercise> findById(Long id);
    
    List<SosExercise> findAllByPatientId(Long patientId);
    
    List<SosExercise> findAll();
    void delete(SosExercise exercise);
}