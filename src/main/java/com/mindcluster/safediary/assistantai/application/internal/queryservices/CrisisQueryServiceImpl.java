package com.mindcluster.safediary.assistantai.application.internal.queryservices;

import com.mindcluster.safediary.assistantai.application.internal.outboundservices.crisis.CrisisHotlineDirectory;
import com.mindcluster.safediary.assistantai.application.queryservices.CrisisQueryService;
import com.mindcluster.safediary.assistantai.domain.model.aggregates.RiskAssessment;
import com.mindcluster.safediary.assistantai.domain.model.queries.GetCurrentRiskAssessmentQuery;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.CrisisHotline;
import com.mindcluster.safediary.assistantai.domain.repositories.RiskAssessmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Crisis query service implementation.
 */
@Service
public class CrisisQueryServiceImpl implements CrisisQueryService {

    private final RiskAssessmentRepository riskAssessmentRepository;
    private final CrisisHotlineDirectory crisisHotlineDirectory;

    public CrisisQueryServiceImpl(RiskAssessmentRepository riskAssessmentRepository,
                                  CrisisHotlineDirectory crisisHotlineDirectory) {
        this.riskAssessmentRepository = riskAssessmentRepository;
        this.crisisHotlineDirectory = crisisHotlineDirectory;
    }

    @Override
    public Optional<RiskAssessment> handle(GetCurrentRiskAssessmentQuery query) {
        return riskAssessmentRepository.findLatestBySessionId(query.sessionId());
    }

    @Override
    public List<CrisisHotline> getCrisisHotlines() {
        return crisisHotlineDirectory.findAll();
    }
}
