package com.emacorrea.spc;

import com.emacorrea.spc.config.SwaggerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import springfox.documentation.spring.web.plugins.Docket;

import java.net.URI;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.temporaryRedirect;

// TODO: Add SpotBugs/PMD to app
// TODO: Add Vault to store client id/secrets and refresh token secrets
// TODO: Add datasource/databse configuration for spring batch tables
// TODO: Add task executor set to job launcher
// TODO: Update swagger UI
// TODO: Create Postman suite for this api
@Slf4j
@SpringBootApplication
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackageClasses = { Application.class, AppConstants.class })
@EnableScheduling
public class Application {

	@Autowired
	JobLauncher jobLauncher;

	@Autowired
	Job job;

	public static void main(final String[] args) {
		SpringApplication.run(Application.class);
	}

	@Bean
	public Docket initSwagger(final SwaggerConfig swaggerConfig) {
		return swaggerConfig.getDocket("com.emacorrea.spc");
	}

	@Bean
	public RouterFunction<ServerResponse> routerFunction() {
		return route().GET("/", req -> temporaryRedirect(URI.create("/swagger-ui/index.html")).build())
				.GET("/info", req -> temporaryRedirect(URI.create("/actuator/info")).build())
				.GET("/health", req -> temporaryRedirect(URI.create("/actuator/health")).build()).build();
	}

}
