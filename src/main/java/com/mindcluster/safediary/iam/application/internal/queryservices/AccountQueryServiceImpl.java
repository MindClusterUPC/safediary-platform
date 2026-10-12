package com.mindcluster.safediary.iam.application.internal.queryservices;

import com.mindcluster.safediary.iam.application.queryservices.AccountQueryService;
import com.mindcluster.safediary.iam.domain.model.aggregates.Account;
import com.mindcluster.safediary.iam.domain.model.queries.GetAccountByIdQuery;
import com.mindcluster.safediary.iam.domain.repositories.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class AccountQueryServiceImpl implements AccountQueryService {
    private final AccountRepository accounts;

    public Optional<Account> handle(GetAccountByIdQuery q) {
        return q.accountId() == null ? Optional.empty() : accounts.findById(q.accountId());
    }
}
