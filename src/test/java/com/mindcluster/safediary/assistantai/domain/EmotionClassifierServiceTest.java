package com.mindcluster.safediary.assistantai.domain;

import com.mindcluster.safediary.assistantai.domain.model.valueobjects.PlutchikEmotionTag;
import com.mindcluster.safediary.assistantai.domain.services.EmotionClassifierService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmotionClassifierServiceTest {

    private final EmotionClassifierService service = new EmotionClassifierService();

    @Test
    void normalizesEnumNamesAndSynonyms() {
        assertThat(service.normalizeToPlutchik("joy")).isEqualTo(PlutchikEmotionTag.JOY);
        assertThat(service.normalizeToPlutchik("tristeza")).isEqualTo(PlutchikEmotionTag.SADNESS);
        assertThat(service.normalizeToPlutchik("Preocupación")).isEqualTo(PlutchikEmotionTag.APPREHENSION);
    }

    @Test
    void unknownOrMissingTagsAreNull() {
        assertThat(service.normalizeToPlutchik("xyz")).isNull();
        assertThat(service.normalizeToPlutchik(null)).isNull();
    }
}
