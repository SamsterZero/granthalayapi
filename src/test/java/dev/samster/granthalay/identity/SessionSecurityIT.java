package dev.samster.granthalay.identity;

import java.net.CookieManager;
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
class SessionSecurityIT {

	@LocalServerPort
	int port;

	@Autowired
	UserAccountRepository accountRepository;

	@Autowired
	RegisterAccountUseCase registerAccountUseCase;

	@Autowired
	VerifyEmailUseCase verifyEmailUseCase;

	@Test
	void verifiesCookieAttributesSessionRotationAndRevocation() throws Exception {
		var email = "sessiontest@example.com";
		var password = "SecureP@ssw0rd123!";

		// Register and verify user directly
		registerAccountUseCase.execute(new RegisterRequest(email, password));
		var account = accountRepository.findByEmailIgnoreCase(email).orElseThrow();
		verifyEmailUseCase.execute(new VerifyEmailRequest(account.getVerificationToken()));

		// Client 1 Sign In
		var client1CookieManager = new CookieManager();
		var client1 = HttpClient.newBuilder().cookieHandler(client1CookieManager).build();

		var signInBody = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
		var signInReq1 = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/auth/sign-in"))
			.header("Content-Type", "application/json")
			.POST(HttpRequest.BodyPublishers.ofString(signInBody))
			.build();
		var signInRes1 = client1.send(signInReq1, HttpResponse.BodyHandlers.ofString());
		assertThat(signInRes1.statusCode()).isEqualTo(200);

		var setCookie1 = signInRes1.headers().firstValue("Set-Cookie").orElseThrow();
		assertThat(setCookie1).contains("SESSION=").contains("HttpOnly").contains("SameSite=Lax");

		// Client 1 /me access succeeds
		var meReq1 = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/auth/me")).GET().build();
		var meRes1 = client1.send(meReq1, HttpResponse.BodyHandlers.ofString());
		assertThat(meRes1.statusCode()).isEqualTo(200);

		// Client 2 Sign In (simulating second browser session for same user)
		var client2CookieManager = new CookieManager();
		var client2 = HttpClient.newBuilder().cookieHandler(client2CookieManager).build();

		var signInReq2 = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/auth/sign-in"))
			.header("Content-Type", "application/json")
			.POST(HttpRequest.BodyPublishers.ofString(signInBody))
			.build();
		var signInRes2 = client2.send(signInReq2, HttpResponse.BodyHandlers.ofString());
		assertThat(signInRes2.statusCode()).isEqualTo(200);

		var setCookie2 = signInRes2.headers().firstValue("Set-Cookie").orElseThrow();
		assertThat(setCookie2).contains("SESSION=");
		// Verify different session cookie IDs generated
		assertThat(setCookie1).isNotEqualTo(setCookie2);

		// Client 2 /me access succeeds
		var meReq2 = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/auth/me")).GET().build();
		var meRes2 = client2.send(meReq2, HttpResponse.BodyHandlers.ofString());
		assertThat(meRes2.statusCode()).isEqualTo(200);

		// Client 1 triggers revocation of all active sessions
		var revokeReq = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/auth/revoke-sessions"))
			.POST(HttpRequest.BodyPublishers.noBody())
			.build();
		var revokeRes = client1.send(revokeReq, HttpResponse.BodyHandlers.ofString());
		assertThat(revokeRes.statusCode()).isEqualTo(204);

		// Subsequent requests from both Client 1 and Client 2 should be rejected with 403
		// Forbidden
		var meRes1AfterRevoke = client1.send(meReq1, HttpResponse.BodyHandlers.ofString());
		assertThat(meRes1AfterRevoke.statusCode()).isEqualTo(403);

		var meRes2AfterRevoke = client2.send(meReq2, HttpResponse.BodyHandlers.ofString());
		assertThat(meRes2AfterRevoke.statusCode()).isEqualTo(403);
	}

}
