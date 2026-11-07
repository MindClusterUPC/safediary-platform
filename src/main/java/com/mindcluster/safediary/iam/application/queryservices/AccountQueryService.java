package com.mindcluster.safediary.iam.application.queryservices;

import com.mindcluster.safediary.iam.domain.model.aggregates.Account;
import com.mindcluster.safediary.iam.domain.model.queries.GetAccountByIdQuery;
import java.util.Optional;

public interface AccountQueryService {
    Optional<Account> handle(GetAccountByIdQuery query);
}
