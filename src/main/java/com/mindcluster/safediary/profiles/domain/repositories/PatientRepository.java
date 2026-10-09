package com.mindcluster.safediary.profiles.domain.repositories;

import com.mindcluster.safediary.profiles.domain.model.aggregates.Patient;
import com.mindcluster.safediary.profiles.domain.model.valueobjects.EmailAddress;

import java.util.List;
import java.util.Optional;

public interface PatientRepository {
    Patient save(Patient patient);
    Optional<Patient> findById(Long id);
    Optional<Patient> findByEmail(EmailAddress email);
    List<Patient> findAll();
    void delete(Patient patient);
    boolean existsByEmail(EmailAddress email);
}
