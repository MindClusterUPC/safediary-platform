package com.mindcluster.safediary.cliniciandirectory.domain.repositories;

import com.mindcluster.safediary.cliniciandirectory.domain.model.aggregates.Review;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository {
    Optional<Long> findClinicianIdById(Long id);
    Optional<Review> findById(Long id);
    Optional<Review> findByAppointmentId(Long id);
    List<Review> findByClinicianId(Long id);
    Review save(Review value);
}
