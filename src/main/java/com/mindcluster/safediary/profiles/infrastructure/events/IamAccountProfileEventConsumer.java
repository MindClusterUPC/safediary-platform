package com.mindcluster.safediary.profiles.infrastructure.events;

import com.mindcluster.safediary.iam.domain.model.events.IamDomainEvent;
import com.mindcluster.safediary.iam.interfaces.acl.IamContextFacade;
import com.mindcluster.safediary.profiles.domain.model.aggregates.Patient;
import com.mindcluster.safediary.profiles.domain.model.entities.EmergencyContact;
import com.mindcluster.safediary.profiles.domain.model.valueobjects.EmailAddress;
import com.mindcluster.safediary.profiles.domain.model.valueobjects.FullName;
import com.mindcluster.safediary.profiles.domain.model.valueobjects.PasswordHash;
import com.mindcluster.safediary.profiles.domain.model.valueobjects.RelationshipType;
import com.mindcluster.safediary.profiles.domain.repositories.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Event consumer listening to IAM events to automatically provision
 * default profiles when an account is registered.
 */
@Component
@RequiredArgsConstructor
public class IamAccountProfileEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(IamAccountProfileEventConsumer.class);

    private final PatientRepository patientRepository;
    private final IamContextFacade iamContextFacade;

    @EventListener
    public void onAccountRegistered(IamDomainEvent event) {
        if (!"AccountRegistered".equalsIgnoreCase(event.type())) {
            return;
        }

        Long accountId = event.accountId();
        if (accountId == null || accountId <= 0) {
            return;
        }

        var accountDtoOpt = iamContextFacade.fetchAccount(accountId);
        if (accountDtoOpt.isEmpty()) {
            log.warn("AccountRegistered event received for accountId={}, but account not found in IAM", accountId);
            return;
        }

        var accountDto = accountDtoOpt.get();
        if ("PATIENT".equalsIgnoreCase(accountDto.role())) {
            var emailVo = EmailAddress.of(accountDto.email());
            if (!patientRepository.existsByEmail(emailVo)) {
                String localPart = accountDto.email().split("@")[0];
                FullName fullName = FullName.of(localPart, "User");
                EmergencyContact emergencyContact = EmergencyContact.of(
                        "Not configured",
                        RelationshipType.OTHER,
                        null,
                        accountDto.email()
                );
                Patient patient = new Patient(
                        fullName,
                        emailVo,
                        PasswordHash.of("OAuthOrIAMManaged123*"),
                        emergencyContact
                );
                patientRepository.save(patient);
                log.info("Provisioned default profile for newly registered patient accountId={}, email={}", accountId, accountDto.email());
            }
        }
    }
}
