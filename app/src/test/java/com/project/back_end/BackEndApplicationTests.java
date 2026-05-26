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
		String rawUrl = dotenv.get("SPRING_DATASOURCE_URL");
		final String jdbcUrl = (rawUrl != null && !rawUrl.contains("allowPublicKeyRetrieval"))
				? rawUrl + "&allowPublicKeyRetrieval=true"
				: rawUrl;
		registry.add("SPRING_DATASOURCE_URL", () -> jdbcUrl);
		registry.add("SPRING_DATA_MONGODB_URI", () -> dotenv.get("SPRING_DATA_MONGODB_URI"));
		registry.add("SPRING_DATASOURCE_PASSWORD", () -> dotenv.get("SPRING_DATASOURCE_PASSWORD"));
		registry.add("SPRING_DATASOURCE_USERNAME", () -> dotenv.get("SPRING_DATASOURCE_USERNAME"));
		registry.add("JWT_SECRET", () -> dotenv.get("JWT_SECRET"));
	}

	@Test
	void contextLoads() {
	}

}
