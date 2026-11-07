package com.mindcluster.safediary.payments.application.queryservices;

import com.mindcluster.safediary.payments.application.queryservices.dto.ConnectedAccountDto;
import com.mindcluster.safediary.payments.domain.model.queries.GetConnectedAccountQuery;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.application.result.Result;

public interface ConnectedAccountQueryService {
    Result<ConnectedAccountDto, ApplicationError> handle(GetConnectedAccountQuery query);
}
