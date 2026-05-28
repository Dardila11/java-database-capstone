package com.project.back_end;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.modulith.Modulithic;

@SpringBootApplication
@Modulithic(
		systemName = "CMS Backend",
		sharedModules = {"shared", "enums", "exceptions"}
)
public class BackEndApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
		dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));

		SpringApplication.run(BackEndApplication.class, args);
	}

}
