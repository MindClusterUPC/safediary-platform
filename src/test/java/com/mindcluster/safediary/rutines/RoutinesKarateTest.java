package com.mindcluster.safediary.rutines;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.profiles.active=dev",
        "spring.datasource.url=jdbc:h2:mem:karate_routines;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "clinician-directory.demo-identity.enabled=true",
        "spring.jpa.show-sql=false"
})
class RoutinesKarateTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        System.setProperty("baseUrl", "http://localhost:" + port);
    }

    @Karate.Test
    Karate testDailyRoutines() {
        return Karate.run("classpath:karate/rutines/daily-routines.feature")
                .systemProperty("baseUrl", "http://localhost:" + port);
    }
}
