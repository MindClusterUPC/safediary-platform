package com.mindcluster.safediary.profiles.application.queryservices;

import com.mindcluster.safediary.profiles.domain.model.aggregates.Patient;
import com.mindcluster.safediary.profiles.domain.model.aggregates.Psychologist;
import com.mindcluster.safediary.profiles.domain.model.queries.GetAllPatientsQuery;
import com.mindcluster.safediary.profiles.domain.model.queries.GetAllPsychologistsQuery;
import com.mindcluster.safediary.profiles.domain.model.queries.GetPatientByIdQuery;
import com.mindcluster.safediary.profiles.domain.model.queries.GetPsychologistByIdQuery;

import java.util.List;
import java.util.Optional;

public interface ProfileQueryService {
    Optional<Patient> handle(GetPatientByIdQuery query);
    List<Patient> handle(GetAllPatientsQuery query);
    Optional<Psychologist> handle(GetPsychologistByIdQuery query);
    List<Psychologist> handle(GetAllPsychologistsQuery query);
}
