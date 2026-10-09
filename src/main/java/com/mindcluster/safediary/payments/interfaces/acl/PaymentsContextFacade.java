package com.mindcluster.safediary.payments.interfaces.acl;

import com.mindcluster.safediary.payments.interfaces.acl.dto.PatientPlanStatusDto;

/**
 * ACL Facade exposing subscription plan queries to other bounded contexts
 * (e.g., AssistantAI, Rutines, ClinicianDirectory).
 */
public interface PaymentsContextFacade {

    /**
     * Retrieves the current plan tier and active status for a given patient account.
     */
    PatientPlanStatusDto fetchPatientPlanStatus(Long patientAccountId);
}
