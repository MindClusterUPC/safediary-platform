package com.mindcluster.safediary.profiles.domain.model.valueobjects;

/**
 * Value Object representing the care modality options offered by the psychologist.
 * Matches front-end toggleable features: Video Call and Encrypted Chat.
 */
public record ModalityOptions(boolean videoCallEnabled, boolean encryptedChatEnabled) {

    public ModalityOptions {
        if (!videoCallEnabled && !encryptedChatEnabled) {
            throw new IllegalArgumentException("Psychologist must accept at least one attention modality (Video Call or Encrypted Chat)");
        }
    }

    public static ModalityOptions of(boolean videoCallEnabled, boolean encryptedChatEnabled) {
        return new ModalityOptions(videoCallEnabled, encryptedChatEnabled);
    }
}
