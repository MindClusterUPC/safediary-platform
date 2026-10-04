package com.mindcluster.safediary.assistantai.infrastructure.crisis;

import com.mindcluster.safediary.assistantai.application.internal.outboundservices.crisis.CrisisHotlineDirectory;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.CrisisHotline;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Static directory of Peruvian emergency hotlines.
 */
@Component
public class StaticCrisisHotlineDirectory implements CrisisHotlineDirectory {

    private static final List<CrisisHotline> HOTLINES = List.of(
            new CrisisHotline("Línea 113 – opción 5 (MINSA)", "113", "Orientación en salud mental gratuita, 24 horas, Perú."),
            new CrisisHotline("SAMU", "106", "Emergencias médicas, Perú."));

    @Override
    public List<CrisisHotline> findAll() {
        return HOTLINES;
    }
}
