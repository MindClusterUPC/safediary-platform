package com.mindcluster.safediary.cliniciandirectory.domain.model.entities;

public record ReviewHelpfulVote(Long id, Long reviewId, Long accountId, boolean active) {
    public ReviewHelpfulVote withActive(boolean value) { return new ReviewHelpfulVote(id, reviewId, accountId, value); }
}
