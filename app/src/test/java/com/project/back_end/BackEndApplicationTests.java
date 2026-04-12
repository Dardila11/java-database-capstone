package com.project.back_end;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
class BackEndApplicationTests {

	@DynamicPropertySource
	static void setupProperties(DynamicPropertyRegistry registry) {
		Dotenv dotenv = Dotenv.load();
		registry.add("SPRING_DATASOURCE_URL", () -> dotenv.get("SPRING_DATASOURCE_URL"));
		registry.add( "SPRING_DATA_MONGODB_URI", () -> dotenv.get("SPRING_DATA_MONGODB_URI"));
		registry.add("SPRING_DATASOURCE_PASSWORD", () -> dotenv.get("SPRING_DATASOURCE_PASSWORD"));
		registry.add("SPRING_DATASOURCE_USERNAME", () -> dotenv.get("SPRING_DATASOURCE_USERNAME"));
		registry.add("JWT_SECRET", () -> dotenv.get("JWT_SECRET"));
	}

	@Test
	void contextLoads() {
	}

}
