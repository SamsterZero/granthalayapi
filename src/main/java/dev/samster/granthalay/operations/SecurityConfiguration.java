package dev.samster.granthalay.operations;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration(proxyBeanMethods = false)
class SecurityConfiguration {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
			.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
				.csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
				.ignoringRequestMatchers("/api/v1/auth/register", "/api/v1/auth/verify-email", "/api/v1/auth/sign-in",
						"/api/v1/auth/sign-out", "/api/v1/auth/revoke-sessions"))
			.requestCache(cache -> cache.disable())
			.sessionManagement(session -> session.sessionFixation(fixation -> fixation.changeSessionId()))
			.logout(logout -> logout.disable())
			.cors(Customizer.withDefaults())
			.authorizeHttpRequests(requests -> requests
				.requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/liveness",
						"/actuator/health/readiness", "/api/v1", "/openapi/granthalay-api-v1.yaml")
				.permitAll()
				.requestMatchers(HttpMethod.POST, "/api/v1/auth/register", "/api/v1/auth/verify-email",
						"/api/v1/auth/sign-in", "/api/v1/auth/sign-out")
				.permitAll()
				.requestMatchers(HttpMethod.GET, "/api/v1/auth/me")
				.authenticated()
				.requestMatchers(HttpMethod.POST, "/api/v1/auth/revoke-sessions")
				.authenticated()
				.anyRequest()
				.denyAll())
			.exceptionHandling(
					errors -> errors.authenticationEntryPoint((request, response, exception) -> forbidden(response))
						.accessDeniedHandler((request, response, exception) -> forbidden(response)))
			.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource(
			@Value("${granthalay.web.allowed-origins}") List<String> allowedOrigins) {
		var configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(allowedOrigins);
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("Accept", "Content-Type", "Authorization", "X-Request-ID"));
		configuration.setExposedHeaders(List.of("Location", "X-Request-ID"));
		configuration.setAllowCredentials(true);
		configuration.setMaxAge(3600L);
		var source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", configuration);
		return source;
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(12);
	}

	@Bean
	UserDetailsService userDetailsService() {
		return new InMemoryUserDetailsManager();
	}

	private static void forbidden(HttpServletResponse response) throws IOException {
		response.setStatus(403);
		response.setContentType("application/problem+json");
		response.getOutputStream()
			.write("{\"type\":\"about:blank\",\"title\":\"Forbidden\",\"status\":403,\"detail\":\"Access is denied.\"}"
				.getBytes(StandardCharsets.UTF_8));
	}

}
