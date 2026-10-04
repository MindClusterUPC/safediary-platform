package com.mindcluster.safediary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SafediaryPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(SafediaryPlatformApplication.class, args);
	}

}
