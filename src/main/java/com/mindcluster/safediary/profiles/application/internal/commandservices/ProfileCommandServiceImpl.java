package com.mindcluster.safediary.profiles.application.internal.commandservices;

import com.mindcluster.safediary.iam.interfaces.acl.IamContextFacade;
import com.mindcluster.safediary.profiles.application.commandservices.ProfileCommandService;
import com.mindcluster.safediary.profiles.domain.model.aggregates.Patient;
import com.mindcluster.safediary.profiles.domain.model.aggregates.Psychologist;
import com.mindcluster.safediary.profiles.domain.model.commands.*;
import com.mindcluster.safediary.profiles.domain.model.entities.AvailabilitySlot;
import com.mindcluster.safediary.profiles.domain.model.entities.EmergencyContact;
import com.mindcluster.safediary.profiles.domain.model.valueobjects.*;
import com.mindcluster.safediary.profiles.domain.repositories.PatientRepository;
import com.mindcluster.safediary.profiles.domain.repositories.PsychologistRepository;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.application.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProfileCommandServiceImpl implements ProfileCommandService {

    private final PatientRepository patientRepository;
    private final PsychologistRepository psychologistRepository;
    private final IamContextFacade iamContextFacade;

    public ProfileCommandServiceImpl(PatientRepository patientRepository,
                                     PsychologistRepository psychologistRepository,
                                     @Autowired(required = false) IamContextFacade iamContextFacade) {
        this.patientRepository = patientRepository;
        this.psychologistRepository = psychologistRepository;
        this.iamContextFacade = iamContextFacade;
    }

    private Optional<Patient> findPatientByIdOrAccountId(Long id) {
        if (id == null) return Optional.empty();
        var direct = patientRepository.findById(id);
        if (direct.isPresent()) return direct;
        if (iamContextFacade != null) {
            var accountOpt = iamContextFacade.fetchAccount(id);
            if (accountOpt.isPresent()) {
                return patientRepository.findByEmail(EmailAddress.of(accountOpt.get().email()));
            }
        }
        return Optional.empty();
    }

    @Override
    @Transactional
    public Result<Patient, ApplicationError> handle(RegisterPatientCommand command) {
        try {
            var emailVo = EmailAddress.of(command.email());
            if (patientRepository.existsByEmail(emailVo)) {
                return Result.failure(ApplicationError.conflict("Patient", "Patient email already registered: " + command.email()));
            }

            var fullNameVo = FullName.of(command.firstName(), command.lastName());
            var passwordVo = PasswordHash.of(command.password());
            var emergencyContact = EmergencyContact.of(
                    command.emergencyContactName(),
                    command.emergencyContactRelationship(),
                    command.emergencyContactPhone(),
                    command.emergencyContactEmail()
            );

            var patient = new Patient(fullNameVo, emailVo, passwordVo, emergencyContact);
            var saved = patientRepository.save(patient);
            return Result.success(saved);
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("patient-registration", ex.getMessage()));
        } catch (Exception ex) {
            return Result.failure(ApplicationError.unexpected("patient-registration", ex.getMessage()));
        }
    }

    @Override
    @Transactional
    public Result<Patient, ApplicationError> handle(UpdateEmergencyContactCommand command) {
        try {
            var patientOpt = findPatientByIdOrAccountId(command.patientId());
            if (patientOpt.isEmpty()) {
                return Result.failure(ApplicationError.notFound("Patient", String.valueOf(command.patientId())));
            }
            var patient = patientOpt.get();
            var newContact = EmergencyContact.of(
                    command.name(),
                    command.relationship(),
                    command.phoneNumber(),
                    command.email()
            );
            patient.updateEmergencyContact(newContact);
            var saved = patientRepository.save(patient);
            return Result.success(saved);
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("emergency-contact", ex.getMessage()));
        } catch (Exception ex) {
            return Result.failure(ApplicationError.unexpected("emergency-contact", ex.getMessage()));
        }
    }

    @Override
    @Transactional
    public Result<Patient, ApplicationError> handle(UpdatePatientProfileCommand command) {
        try {
            var patientOpt = findPatientByIdOrAccountId(command.patientId());
            if (patientOpt.isEmpty()) {
                return Result.failure(ApplicationError.notFound("Patient", String.valueOf(command.patientId())));
            }
            var patient = patientOpt.get();
            patient.updateFullName(FullName.of(command.firstName(), command.lastName()));
            var saved = patientRepository.save(patient);
            return Result.success(saved);
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("patient-profile", ex.getMessage()));
        } catch (Exception ex) {
            return Result.failure(ApplicationError.unexpected("patient-profile", ex.getMessage()));
        }
    }

    @Override
    @Transactional
    public Result<Patient, ApplicationError> handle(RemoveEmergencyContactCommand command) {
        try {
            var patientOpt = findPatientByIdOrAccountId(command.patientId());
            if (patientOpt.isEmpty()) {
                return Result.failure(ApplicationError.notFound("Patient", String.valueOf(command.patientId())));
            }
            var patient = patientOpt.get();
            patient.removeEmergencyContact();
            var saved = patientRepository.save(patient);
            return Result.success(saved);
        } catch (Exception ex) {
            return Result.failure(ApplicationError.unexpected("emergency-contact", ex.getMessage()));
        }
    }

    @Override
    @Transactional
    public Result<Patient, ApplicationError> handle(UpdatePatientEmailCommand command) {
        try {
            var patientOpt = findPatientByIdOrAccountId(command.patientId());
            if (patientOpt.isEmpty()) {
                return Result.failure(ApplicationError.notFound("Patient", String.valueOf(command.patientId())));
            }
            var patient = patientOpt.get();
            String currentEmail = patient.getEmail().value();
            String newEmail = command.newEmail() != null ? command.newEmail().trim() : "";

            // 1. Password verification
            boolean passwordValid = false;
            if (iamContextFacade != null) {
                passwordValid = iamContextFacade.verifyPasswordByEmail(currentEmail, command.currentPassword());
            }
            if (!passwordValid && patient.getPassword() != null) {
                passwordValid = command.currentPassword().equals(patient.getPassword().value());
            }

            if (!passwordValid) {
                return Result.failure(ApplicationError.unauthorized("La contraseña actual no coincide con la registrada."));
            }

            // 2. Conflict check
            var newEmailVo = EmailAddress.of(newEmail);
            if (!currentEmail.equalsIgnoreCase(newEmail)) {
                if (patientRepository.existsByEmail(newEmailVo)
                        || (iamContextFacade != null && iamContextFacade.existsAccountByEmail(newEmail))) {
                    return Result.failure(ApplicationError.conflict("Patient", "El correo electrónico ya está registrado: " + newEmail));
                }
            }

            // 3. Update IAM account if present
            if (iamContextFacade != null) {
                iamContextFacade.updateAccountEmail(currentEmail, newEmail);
            }

            // 4. Update Profile entity
            patient.updateEmail(newEmailVo);
            var saved = patientRepository.save(patient);
            return Result.success(saved);
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("patient-email", ex.getMessage()));
        } catch (Exception ex) {
            return Result.failure(ApplicationError.unexpected("patient-email", ex.getMessage()));
        }
    }

    @Override
    @Transactional
    public Result<Psychologist, ApplicationError> handle(RegisterPsychologistCommand command) {
        try {
            var emailVo = EmailAddress.of(command.email());
            if (psychologistRepository.existsByEmail(emailVo)) {
                return Result.failure(ApplicationError.conflict("Psychologist", "Psychologist email already registered: " + command.email()));
            }

            var fullNameVo = FullName.of(command.firstName(), command.lastName());
            var pricingVo = SessionPricing.of(command.costPerSession(), command.durationMinutes(), command.currency());
            var modalityVo = ModalityOptions.of(command.videoCallEnabled(), command.encryptedChatEnabled());
            var descVo = PsychologistDescription.of(command.bio(), command.categories());
            var certVo = CertificateTitles.of(command.certificateTitles());

            var psychologist = new Psychologist(
                    fullNameVo,
                    emailVo,
                    pricingVo,
                    PsychologistAvailability.empty(),
                    modalityVo,
                    descVo,
                    certVo
            );

            var saved = psychologistRepository.save(psychologist);
            return Result.success(saved);
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("psychologist-registration", ex.getMessage()));
        } catch (Exception ex) {
            return Result.failure(ApplicationError.unexpected("psychologist-registration", ex.getMessage()));
        }
    }

    @Override
    @Transactional
    public Result<Psychologist, ApplicationError> handle(ConfigurePsychologistAvailabilityCommand command) {
        try {
            var psychOpt = psychologistRepository.findById(command.psychologistId());
            if (psychOpt.isEmpty()) {
                return Result.failure(ApplicationError.notFound("Psychologist", String.valueOf(command.psychologistId())));
            }
            var psychologist = psychOpt.get();

            List<AvailabilitySlot> domainSlots = new ArrayList<>();
            if (command.slots() != null) {
                for (var slotEntry : command.slots()) {
                    domainSlots.add(AvailabilitySlot.of(slotEntry.dayOfWeek(), slotEntry.startTime(), slotEntry.endTime()));
                }
            }

            psychologist.configureAvailability(domainSlots);

            var saved = psychologistRepository.save(psychologist);
            return Result.success(saved);
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("psychologist-availability", ex.getMessage()));
        } catch (Exception ex) {
            return Result.failure(ApplicationError.unexpected("psychologist-availability", ex.getMessage()));
        }
    }

    @Override
    @Transactional
    public Result<Psychologist, ApplicationError> handle(UpdatePsychologistProfileCommand command) {
        try {
            var psychOpt = psychologistRepository.findById(command.psychologistId());
            if (psychOpt.isEmpty()) {
                return Result.failure(ApplicationError.notFound("Psychologist", String.valueOf(command.psychologistId())));
            }
            var psychologist = psychOpt.get();

            psychologist.updatePricing(command.costPerSession(), command.durationMinutes(), command.currency());
            psychologist.updateModalityOptions(command.videoCallEnabled(), command.encryptedChatEnabled());
            psychologist.updateDescription(command.bio(), command.categories());
            psychologist.updateCertificateTitles(command.certificateTitles());

            var saved = psychologistRepository.save(psychologist);
            return Result.success(saved);
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("psychologist-update", ex.getMessage()));
        } catch (Exception ex) {
            return Result.failure(ApplicationError.unexpected("psychologist-update", ex.getMessage()));
        }
    }
}
