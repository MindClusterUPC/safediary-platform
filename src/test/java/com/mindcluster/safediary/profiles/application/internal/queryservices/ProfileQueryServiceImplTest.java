package com.mindcluster.safediary.profiles.application.internal.queryservices;

import com.mindcluster.safediary.profiles.domain.model.aggregates.Patient;
import com.mindcluster.safediary.profiles.domain.model.aggregates.Psychologist;
import com.mindcluster.safediary.profiles.domain.model.entities.EmergencyContact;
import com.mindcluster.safediary.profiles.domain.model.queries.GetAllPatientsQuery;
import com.mindcluster.safediary.profiles.domain.model.queries.GetAllPsychologistsQuery;
import com.mindcluster.safediary.profiles.domain.model.queries.GetPatientByIdQuery;
import com.mindcluster.safediary.profiles.domain.model.queries.GetPsychologistByIdQuery;
import com.mindcluster.safediary.profiles.domain.model.valueobjects.*;
import com.mindcluster.safediary.profiles.domain.repositories.PatientRepository;
import com.mindcluster.safediary.profiles.domain.repositories.PsychologistRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileQueryServiceImplTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PsychologistRepository psychologistRepository;

    @InjectMocks
    private ProfileQueryServiceImpl profileQueryService;

    @Test
    @DisplayName("handle(GetPatientByIdQuery) returns patient when found")
    void handle_getPatientById_whenFound_returnsPatient() {
        var patient = new Patient(
                1L,
                FullName.of("Carlos", "Mendoza"),
                EmailAddress.of("carlos.mendoza@example.com"),
                PasswordHash.of("Secret123*"),
                EmergencyContact.of("Maria Mendoza", RelationshipType.PARENT, "+51987654321", null)
        );
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        var result = profileQueryService.handle(new GetPatientByIdQuery(1L));

        assertThat(result).isPresent();
        assertThat(result.get().getFullName().firstName()).isEqualTo("Carlos");
        verify(patientRepository).findById(1L);
    }

    @Test
    @DisplayName("handle(GetPatientByIdQuery) returns empty when patient does not exist")
    void handle_getPatientById_whenNotFound_returnsEmpty() {
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        var result = profileQueryService.handle(new GetPatientByIdQuery(999L));

        assertThat(result).isEmpty();
        verify(patientRepository).findById(999L);
    }

    @Test
    @DisplayName("handle(GetAllPatientsQuery) returns all patients")
    void handle_getAllPatients_returnsPatientsList() {
        var patient = new Patient(
                1L,
                FullName.of("Carlos", "Mendoza"),
                EmailAddress.of("carlos.mendoza@example.com"),
                PasswordHash.of("Secret123*"),
                EmergencyContact.of("Maria Mendoza", RelationshipType.PARENT, "+51987654321", null)
        );
        when(patientRepository.findAll()).thenReturn(List.of(patient));

        var result = profileQueryService.handle(new GetAllPatientsQuery());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(patientRepository).findAll();
    }

    @Test
    @DisplayName("handle(GetPsychologistByIdQuery) returns psychologist when found")
    void handle_getPsychologistById_whenFound_returnsPsychologist() {
        var psychologist = new Psychologist(
                FullName.of("Lucia", "Perez"),
                EmailAddress.of("lucia.perez@example.com"),
                SessionPricing.of(new BigDecimal("100.00"), 50, "PEN"),
                PsychologistAvailability.empty(),
                ModalityOptions.of(true, true),
                PsychologistDescription.of("Experta en terapia cognitivo-conductual", List.of("TCC")),
                CertificateTitles.of(List.of("Licenciada"))
        );
        when(psychologistRepository.findById(2L)).thenReturn(Optional.of(psychologist));

        var result = profileQueryService.handle(new GetPsychologistByIdQuery(2L));

        assertThat(result).isPresent();
        assertThat(result.get().getFullName().firstName()).isEqualTo("Lucia");
        verify(psychologistRepository).findById(2L);
    }

    @Test
    @DisplayName("handle(GetPsychologistByIdQuery) returns empty when psychologist not found")
    void handle_getPsychologistById_whenNotFound_returnsEmpty() {
        when(psychologistRepository.findById(999L)).thenReturn(Optional.empty());

        var result = profileQueryService.handle(new GetPsychologistByIdQuery(999L));

        assertThat(result).isEmpty();
        verify(psychologistRepository).findById(999L);
    }

    @Test
    @DisplayName("handle(GetAllPsychologistsQuery) returns all registered psychologists")
    void handle_getAllPsychologists_returnsPsychologistsList() {
        var psychologist = new Psychologist(
                FullName.of("Lucia", "Perez"),
                EmailAddress.of("lucia.perez@example.com"),
                SessionPricing.of(new BigDecimal("100.00"), 50, "PEN"),
                PsychologistAvailability.empty(),
                ModalityOptions.of(true, true),
                PsychologistDescription.of("Experta en terapia cognitivo-conductual", List.of("TCC")),
                CertificateTitles.of(List.of("Licenciada"))
        );
        when(psychologistRepository.findAll()).thenReturn(List.of(psychologist));

        var result = profileQueryService.handle(new GetAllPsychologistsQuery());

        assertThat(result).hasSize(1);
        verify(psychologistRepository).findAll();
    }
}
