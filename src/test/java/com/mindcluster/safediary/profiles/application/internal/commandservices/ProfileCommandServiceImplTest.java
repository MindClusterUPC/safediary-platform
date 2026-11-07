package com.mindcluster.safediary.profiles.application.internal.commandservices;

import com.mindcluster.safediary.profiles.domain.model.aggregates.Patient;
import com.mindcluster.safediary.profiles.domain.model.aggregates.Psychologist;
import com.mindcluster.safediary.profiles.domain.model.commands.*;
import com.mindcluster.safediary.profiles.domain.model.entities.EmergencyContact;
import com.mindcluster.safediary.profiles.domain.model.valueobjects.EmailAddress;
import com.mindcluster.safediary.profiles.domain.model.valueobjects.FullName;
import com.mindcluster.safediary.profiles.domain.model.valueobjects.PasswordHash;
import com.mindcluster.safediary.profiles.domain.model.valueobjects.RelationshipType;
import com.mindcluster.safediary.profiles.domain.repositories.PatientRepository;
import com.mindcluster.safediary.profiles.domain.repositories.PsychologistRepository;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.application.result.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileCommandServiceImplTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PsychologistRepository psychologistRepository;

    @InjectMocks
    private ProfileCommandServiceImpl profileCommandService;

    @Test
    @DisplayName("handle(RegisterPatientCommand) saves and returns patient on happy path")
    void handle_registerPatient_whenEmailNotExists_savesAndReturnsPatient() {
        var command = new RegisterPatientCommand(
                "Carlos",
                "Mendoza",
                "carlos.mendoza@example.com",
                "Secret123*",
                "Maria Mendoza",
                RelationshipType.PARENT,
                "+51987654321",
                "maria@example.com"
        );

        when(patientRepository.existsByEmail(any(EmailAddress.class))).thenReturn(false);
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = profileCommandService.handle(command);

        assertThat(result).isInstanceOf(Result.Success.class);
        var patient = ((Result.Success<Patient, ?>) result).value();
        assertThat(patient.getFullName().firstName()).isEqualTo("Carlos");
        assertThat(patient.getEmail().value()).isEqualTo("carlos.mendoza@example.com");
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    @DisplayName("handle(RegisterPatientCommand) returns conflict when email already exists")
    void handle_registerPatient_whenEmailAlreadyExists_returnsConflictError() {
        var command = new RegisterPatientCommand(
                "Carlos",
                "Mendoza",
                "carlos.mendoza@example.com",
                "Secret123*",
                "Maria Mendoza",
                RelationshipType.PARENT,
                "+51987654321",
                null
        );

        when(patientRepository.existsByEmail(any(EmailAddress.class))).thenReturn(true);

        var result = profileCommandService.handle(command);

        assertThat(result).isInstanceOf(Result.Failure.class);
        var failure = (Result.Failure<?, ApplicationError>) result;
        assertThat(failure.error().code()).isEqualTo("PATIENT_CONFLICT");
        verify(patientRepository, never()).save(any());
    }

    @Test
    @DisplayName("handle(RegisterPatientCommand) returns validation error on invalid input")
    void handle_registerPatient_whenValidationFails_returnsValidationError() {
        var command = new RegisterPatientCommand(
                "Carlos",
                "Mendoza",
                "invalid-email",
                "Secret123*",
                "Maria Mendoza",
                RelationshipType.PARENT,
                "+51987654321",
                null
        );

        var result = profileCommandService.handle(command);

        assertThat(result).isInstanceOf(Result.Failure.class);
        var failure = (Result.Failure<?, ApplicationError>) result;
        assertThat(failure.error().code()).isEqualTo("VALIDATION_ERROR");
        verify(patientRepository, never()).save(any());
    }

    @Test
    @DisplayName("handle(UpdateEmergencyContactCommand) updates contact when patient exists")
    void handle_updateEmergencyContact_whenPatientExists_updatesAndReturnsPatient() {
        var patient = new Patient(
                1L,
                FullName.of("Carlos", "Mendoza"),
                EmailAddress.of("carlos.mendoza@example.com"),
                PasswordHash.of("Secret123*"),
                EmergencyContact.of("Maria Mendoza", RelationshipType.PARENT, "+51987654321", null)
        );

        var command = new UpdateEmergencyContactCommand(
                1L,
                "Ana Gomez",
                RelationshipType.SPOUSE,
                "+51912345678",
                "ana.gomez@example.com"
        );

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = profileCommandService.handle(command);

        assertThat(result).isInstanceOf(Result.Success.class);
        var updated = ((Result.Success<Patient, ?>) result).value();
        assertThat(updated.getEmergencyContact().getName()).isEqualTo("Ana Gomez");
        assertThat(updated.getEmergencyContact().getRelationship()).isEqualTo(RelationshipType.SPOUSE);
        verify(patientRepository).save(patient);
    }

    @Test
    @DisplayName("handle(UpdateEmergencyContactCommand) returns not found when patient does not exist")
    void handle_updateEmergencyContact_whenPatientNotFound_returnsNotFoundError() {
        var command = new UpdateEmergencyContactCommand(
                999L,
                "Ana Gomez",
                RelationshipType.SPOUSE,
                "+51912345678",
                null
        );

        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        var result = profileCommandService.handle(command);

        assertThat(result).isInstanceOf(Result.Failure.class);
        var failure = (Result.Failure<?, ApplicationError>) result;
        assertThat(failure.error().code()).isEqualTo("PATIENT_NOT_FOUND");
        verify(patientRepository, never()).save(any());
    }

    @Test
    @DisplayName("handle(RegisterPsychologistCommand) saves and returns psychologist when data is valid")
    void handle_registerPsychologist_whenValid_savesAndReturnsPsychologist() {
        var command = new RegisterPsychologistCommand(
                "Lucia",
                "Perez",
                "lucia.perez@example.com",
                new BigDecimal("120.00"),
                50,
                "PEN",
                true,
                true,
                "Especialista en ansiedad y terapia cognitivo-conductual",
                List.of("Ansiedad", "Depresion"),
                List.of("Licenciada en Psicologia")
        );

        when(psychologistRepository.existsByEmail(any(EmailAddress.class))).thenReturn(false);
        when(psychologistRepository.save(any(Psychologist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = profileCommandService.handle(command);

        assertThat(result).isInstanceOf(Result.Success.class);
        var psychologist = ((Result.Success<Psychologist, ?>) result).value();
        assertThat(psychologist.getFullName().firstName()).isEqualTo("Lucia");
        verify(psychologistRepository).save(any(Psychologist.class));
    }

    @Test
    @DisplayName("handle(ConfigurePsychologistAvailabilityCommand) returns not found when psychologist missing")
    void handle_configurePsychologistAvailability_whenNotFound_returnsNotFoundError() {
        var command = new ConfigurePsychologistAvailabilityCommand(888L, List.of());
        when(psychologistRepository.findById(888L)).thenReturn(Optional.empty());

        var result = profileCommandService.handle(command);

        assertThat(result).isInstanceOf(Result.Failure.class);
        var failure = (Result.Failure<?, ApplicationError>) result;
        assertThat(failure.error().code()).isEqualTo("PSYCHOLOGIST_NOT_FOUND");
        verify(psychologistRepository, never()).save(any());
    }
}
