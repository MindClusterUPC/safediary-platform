package com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.entities;

import com.mindcluster.safediary.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name="review_helpful_votes", schema="clinician_directory", uniqueConstraints=@UniqueConstraint(name="ux_review_helpful_vote", columnNames={"review_id", "account_id"}))
@Getter @Setter @NoArgsConstructor
public class ReviewHelpfulVotePersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(nullable=false)
    private Long reviewId;
    @Column(nullable=false)
    private Long accountId;
    @Column(nullable=false)
    private boolean active;

}
