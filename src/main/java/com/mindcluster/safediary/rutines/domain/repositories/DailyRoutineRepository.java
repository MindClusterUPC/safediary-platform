package com.mindcluster.safediary.rutines.domain.repositories;

import com.mindcluster.safediary.rutines.domain.model.aggregates.DailyRoutine;

import java.util.List;
import java.util.Optional;

public interface DailyRoutineRepository {
    DailyRoutine save(DailyRoutine routine);
    Optional<DailyRoutine> findById(Long id);
    
    List<DailyRoutine> findAllByPatientId(Long patientId); 
    
    List<DailyRoutine> findAll();
    void delete(DailyRoutine routine);
}