package dev.samster.granthalay.identity;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterAccountUseCase {

	private static final Duration TOKEN_VALIDITY = Duration.ofHours(24);

	private final UserAccountRepository accountRepository;

	private final PasswordEncoder passwordEncoder;

	private final EmailDeliveryProvider emailDeliveryProvider;

	public RegisterAccountUseCase(UserAccountRepository accountRepository, PasswordEncoder passwordEncoder,
			EmailDeliveryProvider emailDeliveryProvider) {
		this.accountRepository = accountRepository;
		this.passwordEncoder = passwordEncoder;
		this.emailDeliveryProvider = emailDeliveryProvider;
	}

	@Transactional
	public MessageResponse execute(RegisterRequest request) {
		var normalizedEmail = request.email().trim().toLowerCase();
		if (!accountRepository.existsByEmailIgnoreCase(normalizedEmail)) {
			var now = Instant.now();
			var token = UUID.randomUUID().toString();
			var account = new UserAccount(UUID.randomUUID().toString(), normalizedEmail,
					passwordEncoder.encode(request.password()), AccountStatus.PENDING_VERIFICATION, token,
					now.plus(TOKEN_VALIDITY), now, now);
			accountRepository.save(account);
			emailDeliveryProvider.sendVerificationEmail(normalizedEmail, token);
		}
		// Return uniform response to resist email enumeration attacks
		return new MessageResponse("Verification link has been sent if eligible.");
	}

}
