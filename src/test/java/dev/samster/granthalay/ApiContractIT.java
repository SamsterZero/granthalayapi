package dev.samster.granthalay;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = "granthalay.web.allowed-origins=https://reader.example")
class ApiContractIT {

	@LocalServerPort
	int port;

	@Test
	void publishesVersionedIndexAndOpenApiSourceOfTruth() throws Exception {
		var index = request("/api/v1", null);
		assertThat(index.statusCode()).isEqualTo(200);
		assertThat(index.headers().firstValue("Content-Type")).hasValue("application/json");
		assertThat(index.body()).isEqualTo(
				"{\"name\":\"Granthalay API\",\"version\":\"v1\",\"openapi\":\"/openapi/granthalay-api-v1.yaml\"}");

		var contract = request("/openapi/granthalay-api-v1.yaml", null);
		assertThat(contract.statusCode()).isEqualTo(200);
		assertThat(contract.body()).contains("openapi: 3.1.0", "  /api/v1:");
	}

	@Test
	void allowsCredentialedCorsOnlyForConfiguredOrigins() throws Exception {
		var allowed = request("/api/v1", "https://reader.example");
		assertThat(allowed.headers().firstValue("Access-Control-Allow-Origin"))
			.hasValue("https://reader.example");
		assertThat(allowed.headers().firstValue("Access-Control-Allow-Credentials")).hasValue("true");

		var denied = request("/api/v1", "https://attacker.example");
		assertThat(denied.headers().firstValue("Access-Control-Allow-Origin")).isEmpty();
		assertThat(denied.headers().firstValue("Access-Control-Allow-Credentials")).isEmpty();
	}

	private HttpResponse<String> request(String path, String origin) throws Exception {
		var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET();
		if (origin != null) {
			builder.header("Origin", origin);
		}
		try (var client = HttpClient.newHttpClient()) {
			return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
		}
	}

}
