package com.mindcluster.safediary.iam.infrastructure.persistence.jpa.repositories;

import com.mindcluster.safediary.iam.infrastructure.persistence.jpa.entities.*;
import com.mindcluster.safediary.iam.domain.model.valueobjects.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface AuditEventPersistenceRepository extends JpaRepository<AuditEventPersistenceEntity, Long> {
    List<AuditEventPersistenceEntity> findByTargetAccountId(Long targetAccountId);
}
