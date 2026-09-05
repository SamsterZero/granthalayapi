package dev.samster.granthalay;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HealthEndpointIT {

	@LocalServerPort
	int port;

	@Autowired
	ApplicationContext context;

	@Test
	void anonymousProbesExposeOnlyStatus() throws Exception {
		for (String path : new String[] { "/actuator/health/liveness", "/actuator/health/readiness" }) {
			var response = request("GET", path);
			assertThat(response.statusCode()).isEqualTo(200);
			assertThat(response.body()).isEqualTo("{\"status\":\"UP\"}");
			assertThat(response.headers().firstValue("Set-Cookie").isPresent()).isFalse();
		}
		var health = request("GET", "/actuator/health");
		assertThat(health.statusCode()).isEqualTo(200);
		assertThat(health.body()).isEqualTo("{\"groups\":[\"liveness\",\"readiness\"],\"status\":\"UP\"}");
	}

	@Test
	void refusingTrafficDoesNotTakeLivenessDown() throws Exception {
		AvailabilityChangeEvent.publish(context, ReadinessState.REFUSING_TRAFFIC);
		try {
			var response = request("GET", "/actuator/health/readiness");
			assertThat(response.statusCode()).isEqualTo(503);
			assertThat(response.body()).isEqualTo("{\"status\":\"OUT_OF_SERVICE\"}");
			assertThat(request("GET", "/actuator/health/liveness").statusCode()).isEqualTo(200);
		}
		finally {
			AvailabilityChangeEvent.publish(context, ReadinessState.ACCEPTING_TRAFFIC);
		}
	}

	@Test
	void otherRoutesAndUnsafeMethodsAreDenied() throws Exception {
		for (String path : new String[] { "/actuator/env", "/actuator/configprops", "/actuator/health/db",
				"/api/v1/accounts", "/v3/api-docs", "/swagger-ui.html", "/logout" }) {
			var response = request("GET", path);
			assertForbidden(response);
			assertThat(response.headers().firstValue("Set-Cookie").isPresent()).isFalse();
		}
		assertForbidden(request("POST", "/api/v1/accounts"));
		assertForbidden(request("POST", "/actuator/health"));
	}

	private void assertForbidden(HttpResponse<String> response) {
		assertThat(response.statusCode()).isEqualTo(403);
		assertThat(response.headers().firstValue("Content-Type")).hasValue("application/problem+json");
		assertThat(response.body()).isEqualTo(
				"{\"type\":\"about:blank\",\"title\":\"Forbidden\",\"status\":403,\"detail\":\"Access is denied.\"}");
	}

	private HttpResponse<String> request(String method, String path) throws Exception {
		try (var client = HttpClient.newHttpClient()) {
			return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
				.method(method, HttpRequest.BodyPublishers.noBody())
				.build(), HttpResponse.BodyHandlers.ofString());
		}
	}

}
