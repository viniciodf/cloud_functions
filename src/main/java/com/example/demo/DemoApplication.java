package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.InetAddress;
import java.net.URI;
import java.util.Optional;

import static org.springframework.web.servlet.function.RequestPredicates.GET;
import static org.springframework.web.servlet.function.RouterFunctions.route;

@Slf4j
@SpringBootApplication
public class DemoApplication {

	private static final String ACCESS_URLS_MESSAGE_LOG =
			"\n\n Access URLs:\n----------------------------------------------------------\n\tExternal:\thttp://{}:{}{}/swagger-ui.html \n\tProfiles:\t{}\n----------------------------------------------------------\n";

	public static void main(String[] args) {
		try {
			final SpringApplication app = new SpringApplication(DemoApplication.class);
			final Environment env = app.run().getEnvironment();
			log.info(
					ACCESS_URLS_MESSAGE_LOG,
					InetAddress.getLocalHost().getHostAddress(),
					env.getProperty("local.server.port"),
					Optional.ofNullable(env.getProperty("server.servlet.context-path"))
							.orElse(""),
					env.getActiveProfiles());
		} catch (Exception e) {
			log.error("SpringBootApplication", e);
		}
	}

	@Bean
	RouterFunction<ServerResponse> routerFunction() {
		return route(
				GET("/"),
				req -> ServerResponse.temporaryRedirect(URI.create("swagger-ui.html")).build());
	}

}
