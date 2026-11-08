package com.mindcluster.safediary.carescheduling;

import com.mindcluster.safediary.carescheduling.application.internal.CareSchedulingException;
import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.CareRole;
import com.mindcluster.safediary.carescheduling.infrastructure.acl.IamContextClient;
import com.mindcluster.safediary.iam.interfaces.acl.IamContextFacade;
import com.mindcluster.safediary.iam.interfaces.acl.dto.IamAccountDto;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class IamContextClientTest {
    private final IamContextFacade iam = mock(IamContextFacade.class);

    @SuppressWarnings("unchecked")
    private IamContextClient client(boolean developmentIdentity, String... profiles) {
        var environment = new MockEnvironment();
        environment.setActiveProfiles(profiles);
        ObjectProvider<HttpServletRequest> provider = mock(ObjectProvider.class);
        var request = new MockHttpServletRequest();
        request.addHeader("X-Account-Id", "99");
        request.addHeader("X-Role", "PSYCHOLOGIST");
        when(provider.getIfAvailable()).thenReturn(request);
        return new IamContextClient(iam, provider, environment, developmentIdentity, "America/Lima");
    }

    @Test void theAuthenticatedIamAccountWinsOverDevelopmentHeaders() {
        when(iam.fetchAuthenticatedAccount()).thenReturn(Optional.of(new IamAccountDto(20L, "maria@safediary.app", "PATIENT", "ACTIVE")));
        var actor = client(true, "dev").currentActor();
        assertThat(actor.accountId()).isEqualTo(20L);
        assertThat(actor.role()).isEqualTo(CareRole.PATIENT);
    }

    @Test void worksInProductionWithTheTokenAndRefusesHeadersWithoutIt() {
        when(iam.fetchAuthenticatedAccount()).thenReturn(Optional.of(new IamAccountDto(10L, "laura@safediary.app", "PSYCHOLOGIST", "ACTIVE")));
        assertThat(client(false, "prod").currentActor().role()).isEqualTo(CareRole.PSYCHOLOGIST);

        when(iam.fetchAuthenticatedAccount()).thenReturn(Optional.empty());
        assertThatThrownBy(() -> client(true, "prod").currentActor()).isInstanceOf(CareSchedulingException.class);
        assertThatThrownBy(() -> client(true, "dev", "prod").currentActor()).isInstanceOf(CareSchedulingException.class);
        assertThatThrownBy(() -> client(false, "dev").currentActor()).isInstanceOf(CareSchedulingException.class);
        assertThat(client(true, "dev").currentActor().accountId()).isEqualTo(99L);
    }

    @Test void inactiveAccountsCannotTakePart() {
        when(iam.fetchAuthenticatedAccount()).thenReturn(Optional.of(new IamAccountDto(20L, "maria@safediary.app", "PATIENT", "SUSPENDED")));
        assertThatThrownBy(() -> client(true, "dev").currentActor()).isInstanceOf(CareSchedulingException.class);
    }

    @Test void summaryConsentIsValidatedInIamForTheAppointment() {
        var today = LocalDate.now(ZoneId.of("America/Lima"));
        when(iam.authorizeConsentedAccess(eq(20L), eq(10L), eq("AI_CLINICAL_SUMMARIES"), any(), eq("CARE_SCHEDULING"), eq("APPOINTMENT"), eq(7L)))
                .thenReturn(Optional.of(31L));
        assertThat(client(false, "prod").findActiveSummaryConsent(20L, 10L, 7L)).contains("31");
        verify(iam).authorizeConsentedAccess(20L, 10L, "AI_CLINICAL_SUMMARIES", today, "CARE_SCHEDULING", "APPOINTMENT", 7L);
        assertThat(client(false, "prod").findActiveSummaryConsent(21L, 10L, 7L)).isEmpty();
    }
}
