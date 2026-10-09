package com.mindcluster.safediary.carescheduling.infrastructure.persistence.jpa.adapters;

import com.mindcluster.safediary.carescheduling.domain.model.entities.SummaryAccessAudit;
import com.mindcluster.safediary.carescheduling.domain.repositories.SummaryAccessAuditRepository;
import com.mindcluster.safediary.carescheduling.infrastructure.persistence.jpa.assemblers.CareSchedulingPersistenceAssembler;
import com.mindcluster.safediary.carescheduling.infrastructure.persistence.jpa.entities.SummaryAccessAuditPersistenceEntity;
import com.mindcluster.safediary.carescheduling.infrastructure.persistence.jpa.repositories.SummaryAccessAuditPersistenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository @RequiredArgsConstructor @Transactional(readOnly=true)
public class SummaryAccessAuditRepositoryImpl implements SummaryAccessAuditRepository {
    private final SummaryAccessAuditPersistenceRepository persistence;

    @Transactional public SummaryAccessAudit save(SummaryAccessAudit value) {
        var entity = new SummaryAccessAuditPersistenceEntity();
        CareSchedulingPersistenceAssembler.copyToEntity(value, entity);
        return CareSchedulingPersistenceAssembler.toDomain(persistence.saveAndFlush(entity));
    }
}
