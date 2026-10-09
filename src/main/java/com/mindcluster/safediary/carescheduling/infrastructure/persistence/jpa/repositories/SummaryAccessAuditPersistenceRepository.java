package com.mindcluster.safediary.carescheduling.infrastructure.persistence.jpa.repositories;

import com.mindcluster.safediary.carescheduling.infrastructure.persistence.jpa.entities.*;
import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SummaryAccessAuditPersistenceRepository extends JpaRepository<SummaryAccessAuditPersistenceEntity, Long> {
}
