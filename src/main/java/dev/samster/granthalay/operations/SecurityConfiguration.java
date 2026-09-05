package dev.samster.granthalay.operations;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
class SecurityConfiguration {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
			.requestCache(cache -> cache.disable())
			.logout(logout -> logout.disable())
			.authorizeHttpRequests(requests -> requests
				.requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/liveness",
						"/actuator/health/readiness")
				.permitAll()
				.anyRequest().denyAll())
			.exceptionHandling(errors -> errors
				.authenticationEntryPoint((request, response, exception) -> forbidden(response))
				.accessDeniedHandler((request, response, exception) -> forbidden(response)))
			.build();
	}

	@Bean
	UserDetailsService userDetailsService() {
		// No default account or generated password before identity is implemented.
		return new InMemoryUserDetailsManager();
	}

	private static void forbidden(HttpServletResponse response) throws IOException {
		response.setStatus(403);
		response.setContentType("application/problem+json");
		response.getOutputStream().write(
				"{\"type\":\"about:blank\",\"title\":\"Forbidden\",\"status\":403,\"detail\":\"Access is denied.\"}"
					.getBytes(StandardCharsets.UTF_8));
	}

}
