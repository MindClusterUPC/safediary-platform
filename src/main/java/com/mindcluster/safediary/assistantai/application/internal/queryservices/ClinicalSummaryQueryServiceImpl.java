package com.mindcluster.safediary.assistantai.application.internal.queryservices;

import com.mindcluster.safediary.assistantai.application.queryservices.ClinicalSummaryQueryService;
import com.mindcluster.safediary.assistantai.domain.model.aggregates.ClinicalSummary;
import com.mindcluster.safediary.assistantai.domain.model.queries.GetWeeklyClinicalSummaryQuery;
import com.mindcluster.safediary.assistantai.domain.repositories.ClinicalSummaryRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Clinical summary query service implementation.
 */
@Service
public class ClinicalSummaryQueryServiceImpl implements ClinicalSummaryQueryService {

    private final ClinicalSummaryRepository clinicalSummaryRepository;

    public ClinicalSummaryQueryServiceImpl(ClinicalSummaryRepository clinicalSummaryRepository) {
        this.clinicalSummaryRepository = clinicalSummaryRepository;
    }

    @Override
    public Optional<ClinicalSummary> handle(GetWeeklyClinicalSummaryQuery query) {
        return query.weekStart() == null
                ? clinicalSummaryRepository.findLatestByAccountId(query.accountId())
                : clinicalSummaryRepository.findByAccountIdAndPeriodStart(query.accountId(), query.weekStart());
    }
}
