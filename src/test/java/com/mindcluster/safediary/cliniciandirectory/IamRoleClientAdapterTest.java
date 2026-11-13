package com.mindcluster.safediary.cliniciandirectory;

import com.mindcluster.safediary.cliniciandirectory.application.internal.DirectoryException;
import com.mindcluster.safediary.cliniciandirectory.infrastructure.identity.IamRoleClientAdapter;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.DirectoryRole;
import com.mindcluster.safediary.iam.interfaces.acl.IamContextFacade;
import com.mindcluster.safediary.iam.interfaces.acl.dto.IamAccountDto;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class IamRoleClientAdapterTest {
    private final IamContextFacade iam = mock(IamContextFacade.class);
    @SuppressWarnings("unchecked")
    private IamRoleClientAdapter client(boolean enabled, String... profiles) {
        var environment = new MockEnvironment();
        environment.setActiveProfiles(profiles);
        ObjectProvider<HttpServletRequest> provider = mock(ObjectProvider.class);
        var request = new MockHttpServletRequest();
        request.addHeader("X-Account-Id", "12");
        request.addHeader("X-Role", "PSYCHOLOGIST");
        when(provider.getIfAvailable()).thenReturn(request);
        return new IamRoleClientAdapter(iam, provider, environment, enabled);
    }
    @Test void acceptsDevelopmentIdentityOnlyWhenExplicitlyEnabledInDev() {
        assertThat(client(true, "dev").currentActor().role()).isEqualTo(DirectoryRole.PSYCHOLOGIST);
        assertThatThrownBy(() -> client(false, "dev").currentActor()).isInstanceOf(DirectoryException.class);
    }
    @Test void refusesForgedHeadersInProductionIncludingWhenDevIsAlsoActive() {
        assertThatThrownBy(() -> client(true, "prod").currentActor()).isInstanceOf(DirectoryException.class);
        assertThatThrownBy(() -> client(true, "dev", "prod").currentActor()).isInstanceOf(DirectoryException.class);
        assertThatThrownBy(() -> client(true, "test").currentActor()).isInstanceOf(DirectoryException.class);
    }
    @Test void authenticatedAccountWorksInProductionWithoutDevelopmentIdentity() {
        when(iam.fetchAuthenticatedAccount()).thenReturn(Optional.of(
                new IamAccountDto(7L, "professional@example.test", "PSYCHOLOGIST", "ACTIVE")));
        var actor = client(false, "prod").currentActor();
        assertThat(actor.accountId()).isEqualTo(7L);
        assertThat(actor.role()).isEqualTo(DirectoryRole.PSYCHOLOGIST);
    }
    @Test void authenticatedAccountWinsOverConflictingDevelopmentHeaders() {
        when(iam.fetchAuthenticatedAccount()).thenReturn(Optional.of(
                new IamAccountDto(8L, "patient@example.test", "PATIENT", "ACTIVE")));
        var actor = client(true, "dev").currentActor();
        assertThat(actor.accountId()).isEqualTo(8L);
        assertThat(actor.role()).isEqualTo(DirectoryRole.PATIENT);
    }
    @Test void inactiveOrInvalidAccountsCannotFallBackToDevelopmentHeaders() {
        when(iam.fetchAuthenticatedAccount()).thenReturn(Optional.of(
                new IamAccountDto(7L, "professional@example.test", "PSYCHOLOGIST", "SUSPENDED")));
        assertThatThrownBy(() -> client(true, "dev").currentActor()).isInstanceOf(DirectoryException.class);
        when(iam.fetchAuthenticatedAccount()).thenReturn(Optional.of(
                new IamAccountDto(7L, "professional@example.test", "UNKNOWN", "ACTIVE")));
        assertThatThrownBy(() -> client(true, "dev").currentActor()).isInstanceOf(DirectoryException.class);
    }
}
