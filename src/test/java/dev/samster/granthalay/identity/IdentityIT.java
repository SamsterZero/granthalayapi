package dev.samster.granthalay.identity;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import dev.samster.granthalay.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = "granthalay.web.allowed-origins=https://reader.example")
class IdentityIT {

	@LocalServerPort
	int port;

	@Autowired
	UserAccountRepository accountRepository;

	@Test
	void performsRegistrationVerificationSignInAndSessionAccess() throws Exception {
		var client = HttpClient.newBuilder().cookieHandler(new java.net.CookieManager()).build();

		// 1. Register Account
		var registerBody = "{\"email\":\"newreader@example.com\",\"password\":\"SecureP@ssw0rd!\"}";
		var registerReq = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/auth/register"))
			.header("Content-Type", "application/json")
			.POST(HttpRequest.BodyPublishers.ofString(registerBody))
			.build();
		var registerRes = client.send(registerReq, HttpResponse.BodyHandlers.ofString());
		assertThat(registerRes.statusCode()).isEqualTo(202);
		assertThat(registerRes.body()).contains("Verification link");

		// Fetch created token from database
		var account = accountRepository.findByEmailIgnoreCase("newreader@example.com").orElseThrow();
		assertThat(account.getStatus()).isEqualTo(AccountStatus.PENDING_VERIFICATION);
		var token = account.getVerificationToken();

		// 2. Verify Email
		var verifyBody = "{\"token\":\"" + token + "\"}";
		var verifyReq = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/auth/verify-email"))
			.header("Content-Type", "application/json")
			.POST(HttpRequest.BodyPublishers.ofString(verifyBody))
			.build();
		var verifyRes = client.send(verifyReq, HttpResponse.BodyHandlers.ofString());
		assertThat(verifyRes.statusCode()).isEqualTo(200);

		// 3. Sign In
		var signInBody = "{\"email\":\"newreader@example.com\",\"password\":\"SecureP@ssw0rd!\"}";
		var signInReq = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/auth/sign-in"))
			.header("Content-Type", "application/json")
			.POST(HttpRequest.BodyPublishers.ofString(signInBody))
			.build();
		var signInRes = client.send(signInReq, HttpResponse.BodyHandlers.ofString());
		assertThat(signInRes.statusCode()).isEqualTo(200);
		assertThat(signInRes.body()).contains("newreader@example.com", "ACTIVE");
		var setCookieHeader = signInRes.headers().firstValue("Set-Cookie");
		assertThat(setCookieHeader).isPresent();
		assertThat(setCookieHeader.get()).contains("SESSION");

		// 4. Access Authenticated Endpoint /api/v1/auth/me
		var meReq = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/auth/me")).GET().build();
		var meRes = client.send(meReq, HttpResponse.BodyHandlers.ofString());
		assertThat(meRes.statusCode()).isEqualTo(200);
		assertThat(meRes.body()).contains("newreader@example.com");

		// 5. Sign Out
		var signOutReq = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/auth/sign-out"))
			.POST(HttpRequest.BodyPublishers.noBody())
			.build();
		var signOutRes = client.send(signOutReq, HttpResponse.BodyHandlers.ofString());
		assertThat(signOutRes.statusCode()).isEqualTo(204);
	}

}
