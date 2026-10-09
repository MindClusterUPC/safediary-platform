package com.mindcluster.safediary.profiles;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.profiles.active=dev",
        "spring.datasource.url=jdbc:h2:mem:karate_profiles;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "clinician-directory.demo-identity.enabled=true",
        "spring.jpa.show-sql=false"
})
class ProfilesKarateTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        System.setProperty("baseUrl", "http://localhost:" + port);
    }

    @Karate.Test
    Karate testPatients() {
        return Karate.run("classpath:karate/profiles/patients.feature")
                .systemProperty("baseUrl", "http://localhost:" + port);
    }
}
