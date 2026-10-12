package com.mindcluster.safediary.iam.infrastructure.persistence.jpa.adapters;

import com.mindcluster.safediary.iam.domain.model.aggregates.AuditEvent;
import com.mindcluster.safediary.iam.domain.repositories.AuditEventRepository;
import com.mindcluster.safediary.iam.infrastructure.persistence.jpa.assemblers.IamPersistenceAssembler;
import com.mindcluster.safediary.iam.infrastructure.persistence.jpa.entities.AuditEventPersistenceEntity;
import com.mindcluster.safediary.iam.infrastructure.persistence.jpa.repositories.AuditEventPersistenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository @RequiredArgsConstructor @Transactional(readOnly=true)
public class AuditEventRepositoryImpl implements AuditEventRepository {
    private final AuditEventPersistenceRepository persistence;

    @Transactional public AuditEvent append(AuditEvent event) {
        if (event.id() != null) throw new IllegalArgumentException("Audit events are append-only");
        var entity = new AuditEventPersistenceEntity();
        IamPersistenceAssembler.copyToEntity(event, entity);
        return IamPersistenceAssembler.toDomain(persistence.saveAndFlush(entity));
    }

    public List<AuditEvent> findByTargetAccountId(Long targetAccountId) {
        return persistence.findByTargetAccountId(targetAccountId).stream().map(IamPersistenceAssembler::toDomain).toList();
    }
}
