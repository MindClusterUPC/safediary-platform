package com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.repositories;

import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.entities.ReviewPersistenceEntity;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface ReviewPersistenceRepository extends JpaRepository<ReviewPersistenceEntity, Long> {
    @Query("select e.clinicianId from ReviewPersistenceEntity e where e.id = :id")
    Optional<Long> findClinicianIdById(@Param("id") Long id);
    Optional<ReviewPersistenceEntity> findByAppointmentId(Long id);
    List<ReviewPersistenceEntity> findByClinicianIdOrderByPublishedAtDescIdDesc(Long id);
}
