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
class PasswordResetIT {

	@LocalServerPort
	int port;

	@Autowired
	UserAccountRepository accountRepository;

	@Autowired
	RegisterAccountUseCase registerAccountUseCase;

	@Autowired
	VerifyEmailUseCase verifyEmailUseCase;

	@Test
	void verifiesEnumerationResistancePasswordResetAndSessionRevocation() throws Exception {
		var email = "resetuser@example.com";
		var oldPassword = "OldSecureP@ssw0rd!";
		var newPassword = "NewSecureP@ssw0rd123!";

		// 1. Test enumeration resistance: request reset for non-existent email
		var client = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
		var nonExistentBody = "{\"email\":\"nonexistent@example.com\"}";
		var nonExistentReq = HttpRequest
			.newBuilder(URI.create("http://localhost:" + port + "/api/v1/auth/request-password-reset"))
			.header("Content-Type", "application/json")
			.POST(HttpRequest.BodyPublishers.ofString(nonExistentBody))
			.build();
		var nonExistentRes = client.send(nonExistentReq, HttpResponse.BodyHandlers.ofString());
		assertThat(nonExistentRes.statusCode()).isEqualTo(202);
		assertThat(nonExistentRes.body()).contains("If an active account exists");

		// 2. Register & verify real account
		registerAccountUseCase.execute(new RegisterRequest(email, oldPassword));
		var account = accountRepository.findByEmailIgnoreCase(email).orElseThrow();
		verifyEmailUseCase.execute(new VerifyEmailRequest(account.getVerificationToken()));

		// 3. Sign in to establish active session prior to password reset
		var signInOldBody = "{\"email\":\"" + email + "\",\"password\":\"" + oldPassword + "\"}";
		var signInOldReq = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/auth/sign-in"))
			.header("Content-Type", "application/json")
			.POST(HttpRequest.BodyPublishers.ofString(signInOldBody))
			.build();
		var signInOldRes = client.send(signInOldReq, HttpResponse.BodyHandlers.ofString());
		assertThat(signInOldRes.statusCode()).isEqualTo(200);

		// Verify session active by querying /me
		var meReq = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/auth/me")).GET().build();
		var meResBeforeReset = client.send(meReq, HttpResponse.BodyHandlers.ofString());
		assertThat(meResBeforeReset.statusCode()).isEqualTo(200);

		// 4. Request password reset for active account
		var requestResetBody = "{\"email\":\"" + email + "\"}";
		var requestResetReq = HttpRequest
			.newBuilder(URI.create("http://localhost:" + port + "/api/v1/auth/request-password-reset"))
			.header("Content-Type", "application/json")
			.POST(HttpRequest.BodyPublishers.ofString(requestResetBody))
			.build();
		var requestResetRes = client.send(requestResetReq, HttpResponse.BodyHandlers.ofString());
		assertThat(requestResetRes.statusCode()).isEqualTo(202);

		// Fetch generated reset token from database
		var updatedAccount = accountRepository.findByEmailIgnoreCase(email).orElseThrow();
		var resetToken = updatedAccount.getPasswordResetToken();
		assertThat(resetToken).isNotNull();

		// 5. Reset password using token
		var resetBody = "{\"token\":\"" + resetToken + "\",\"newPassword\":\"" + newPassword + "\"}";
		var resetReq = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/auth/reset-password"))
			.header("Content-Type", "application/json")
			.POST(HttpRequest.BodyPublishers.ofString(resetBody))
			.build();
		var resetRes = client.send(resetReq, HttpResponse.BodyHandlers.ofString());
		assertThat(resetRes.statusCode()).isEqualTo(200);

		// 6. Pre-existing session must now be revoked (403 Forbidden on /me)
		var meResAfterReset = client.send(meReq, HttpResponse.BodyHandlers.ofString());
		assertThat(meResAfterReset.statusCode()).isEqualTo(403);

		// 7. Old password sign-in must fail (403 Forbidden via security
		// authenticationEntryPoint)
		var postResetClient = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
		var signInOldRes2 = postResetClient.send(signInOldReq, HttpResponse.BodyHandlers.ofString());
		assertThat(signInOldRes2.statusCode()).isEqualTo(403);

		// 8. New password sign-in must succeed (200 OK)
		var signInNewBody = "{\"email\":\"" + email + "\",\"password\":\"" + newPassword + "\"}";
		var signInNewReq = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/auth/sign-in"))
			.header("Content-Type", "application/json")
			.POST(HttpRequest.BodyPublishers.ofString(signInNewBody))
			.build();
		var signInNewRes = postResetClient.send(signInNewReq, HttpResponse.BodyHandlers.ofString());
		assertThat(signInNewRes.statusCode()).isEqualTo(200);

		// 9. Reusing token must fail (400 Bad Request)
		var reuseResetRes = postResetClient.send(resetReq, HttpResponse.BodyHandlers.ofString());
		assertThat(reuseResetRes.statusCode()).isEqualTo(400);
	}

}
