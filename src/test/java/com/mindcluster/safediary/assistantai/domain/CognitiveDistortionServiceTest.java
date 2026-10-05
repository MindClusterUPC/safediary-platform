package com.mindcluster.safediary.assistantai.domain;

import com.mindcluster.safediary.assistantai.domain.model.valueobjects.DistortionType;
import com.mindcluster.safediary.assistantai.domain.services.CognitiveDistortionService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CognitiveDistortionServiceTest {

    private final CognitiveDistortionService service = new CognitiveDistortionService();

    @Test
    void keepsReliableDistortion() {
        var distortion = service.toReliableDistortion("catastrophizing", "todo saldrá mal", 0.8);

        assertThat(distortion).isPresent();
        assertThat(distortion.get().getType()).isEqualTo(DistortionType.CATASTROPHIZING);
    }

    @Test
    void discardsUnreliableOrUnknownDistortions() {
        assertThat(service.toReliableDistortion("CATASTROPHIZING", "x", 0.5)).isEmpty();
        assertThat(service.toReliableDistortion("UNKNOWN", "x", 0.9)).isEmpty();
        assertThat(service.toReliableDistortion("CATASTROPHIZING", "x", null)).isEmpty();
    }
}
