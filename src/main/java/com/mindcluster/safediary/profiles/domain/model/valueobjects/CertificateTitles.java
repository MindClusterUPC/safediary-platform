package com.mindcluster.safediary.profiles.domain.model.valueobjects;

import java.util.Collections;
import java.util.List;

/**
 * Value Object holding the psychologist's certified degree/credential titles.
 */
public record CertificateTitles(List<String> titles) {

    public CertificateTitles {
        if (titles == null) {
            titles = Collections.emptyList();
        } else {
            titles = titles.stream()
                    .filter(t -> t != null && !t.isBlank())
                    .map(String::trim)
                    .distinct()
                    .toList();
        }
    }

    public static CertificateTitles of(List<String> titles) {
        return new CertificateTitles(titles);
    }
}
