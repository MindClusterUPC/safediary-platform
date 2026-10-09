package com.mindcluster.safediary.profiles.domain.repositories;

import com.mindcluster.safediary.profiles.domain.model.aggregates.Psychologist;
import com.mindcluster.safediary.profiles.domain.model.valueobjects.EmailAddress;

import java.util.List;
import java.util.Optional;

public interface PsychologistRepository {
    Psychologist save(Psychologist psychologist);
    Optional<Psychologist> findById(Long id);
    Optional<Psychologist> findByEmail(EmailAddress email);
    List<Psychologist> findAll();
    void delete(Psychologist psychologist);
    boolean existsByEmail(EmailAddress email);
}
