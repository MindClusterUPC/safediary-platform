package com.mindcluster.safediary.cliniciandirectory;

import com.mindcluster.safediary.cliniciandirectory.application.internal.DirectoryException;
import com.mindcluster.safediary.cliniciandirectory.infrastructure.identity.DevelopmentIamRoleClient;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.DirectoryRole;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DevelopmentIamRoleClientTest {
    @SuppressWarnings("unchecked")
    private DevelopmentIamRoleClient client(boolean enabled, String... profiles) {
        var environment = new MockEnvironment();
        environment.setActiveProfiles(profiles);
        ObjectProvider<HttpServletRequest> provider = mock(ObjectProvider.class);
        var request = new MockHttpServletRequest();
        request.addHeader("X-Account-Id", "12");
        request.addHeader("X-Role", "PSYCHOLOGIST");
        when(provider.getIfAvailable()).thenReturn(request);
        return new DevelopmentIamRoleClient(provider, environment, enabled);
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
}
