package com.mindcluster.safediary.profiles.application.internal.queryservices;

import com.mindcluster.safediary.profiles.application.queryservices.ProfileQueryService;
import com.mindcluster.safediary.profiles.domain.model.aggregates.Patient;
import com.mindcluster.safediary.profiles.domain.model.aggregates.Psychologist;
import com.mindcluster.safediary.profiles.domain.model.queries.GetAllPatientsQuery;
import com.mindcluster.safediary.profiles.domain.model.queries.GetAllPsychologistsQuery;
import com.mindcluster.safediary.profiles.domain.model.queries.GetPatientByIdQuery;
import com.mindcluster.safediary.profiles.domain.model.queries.GetPsychologistByIdQuery;
import com.mindcluster.safediary.profiles.domain.repositories.PatientRepository;
import com.mindcluster.safediary.profiles.domain.repositories.PsychologistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProfileQueryServiceImpl implements ProfileQueryService {

    private final PatientRepository patientRepository;
    private final PsychologistRepository psychologistRepository;

    public ProfileQueryServiceImpl(PatientRepository patientRepository,
                                   PsychologistRepository psychologistRepository) {
        this.patientRepository = patientRepository;
        this.psychologistRepository = psychologistRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Patient> handle(GetPatientByIdQuery query) {
        return patientRepository.findById(query.id());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Patient> handle(GetAllPatientsQuery query) {
        return patientRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Psychologist> handle(GetPsychologistByIdQuery query) {
        return psychologistRepository.findById(query.id());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Psychologist> handle(GetAllPsychologistsQuery query) {
        return psychologistRepository.findAll();
    }
}
