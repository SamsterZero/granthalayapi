package dev.samster.granthalay.identity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountRegistrationTests {

	private UserAccountRepository accountRepository;

	private PasswordEncoder passwordEncoder;

	private EmailDeliveryProvider emailDeliveryProvider;

	private RegisterAccountUseCase registerAccountUseCase;

	@BeforeEach
	void setUp() {
		accountRepository = mock(UserAccountRepository.class);
		passwordEncoder = new BCryptPasswordEncoder(12);
		emailDeliveryProvider = mock(EmailDeliveryProvider.class);
		registerAccountUseCase = new RegisterAccountUseCase(accountRepository, passwordEncoder, emailDeliveryProvider);
	}

	@Test
	void registersAccountAndHashesPasswordAdaptively() {
		var request = new RegisterRequest("reader@example.com", "SecureP@ssw0rd!");
		when(accountRepository.existsByEmailIgnoreCase("reader@example.com")).thenReturn(false);

		var response = registerAccountUseCase.execute(request);

		assertThat(response.message()).contains("Verification link");
		verify(accountRepository).save(any(UserAccount.class));
		verify(emailDeliveryProvider).sendVerificationEmail(eq("reader@example.com"), any(String.class));
	}

	@Test
	void resistsAccountEnumerationWhenEmailExists() {
		var request = new RegisterRequest("existing@example.com", "SecureP@ssw0rd!");
		when(accountRepository.existsByEmailIgnoreCase("existing@example.com")).thenReturn(true);

		var response = registerAccountUseCase.execute(request);

		assertThat(response.message()).contains("Verification link");
		verify(accountRepository, never()).save(any());
		verify(emailDeliveryProvider, never()).sendVerificationEmail(any(), any());
	}

}
