package com.mindcluster.safediary.profiles.application.commandservices;

import com.mindcluster.safediary.profiles.domain.model.aggregates.Patient;
import com.mindcluster.safediary.profiles.domain.model.aggregates.Psychologist;
import com.mindcluster.safediary.profiles.domain.model.commands.*;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.application.result.Result;

public interface ProfileCommandService {
    Result<Patient, ApplicationError> handle(RegisterPatientCommand command);
    Result<Patient, ApplicationError> handle(UpdateEmergencyContactCommand command);
    Result<Psychologist, ApplicationError> handle(RegisterPsychologistCommand command);
    Result<Psychologist, ApplicationError> handle(ConfigurePsychologistAvailabilityCommand command);
    Result<Psychologist, ApplicationError> handle(UpdatePsychologistProfileCommand command);
}
