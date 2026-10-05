package com.mindcluster.safediary.assistantai.application.internal.outboundservices.crisis;

import com.mindcluster.safediary.assistantai.domain.model.valueobjects.CrisisHotline;

import java.util.List;

/**
 * Outbound port that provides the emergency hotlines offered during the crisis protocol.
 */
public interface CrisisHotlineDirectory {

    List<CrisisHotline> findAll();
}
