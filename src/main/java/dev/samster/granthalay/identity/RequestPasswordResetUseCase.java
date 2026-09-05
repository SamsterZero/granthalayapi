package dev.samster.granthalay.identity;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RequestPasswordResetUseCase {

	private static final Duration RESET_TOKEN_VALIDITY = Duration.ofMinutes(15);

	private final UserAccountRepository accountRepository;

	private final EmailDeliveryProvider emailDeliveryProvider;

	public RequestPasswordResetUseCase(UserAccountRepository accountRepository,
			EmailDeliveryProvider emailDeliveryProvider) {
		this.accountRepository = accountRepository;
		this.emailDeliveryProvider = emailDeliveryProvider;
	}

	@Transactional
	public MessageResponse execute(RequestPasswordResetRequest request) {
		long startTime = System.currentTimeMillis();
		var normalizedEmail = request.email().trim().toLowerCase();

		var optionalAccount = accountRepository.findByEmailIgnoreCase(normalizedEmail);
		if (optionalAccount.isPresent()) {
			var account = optionalAccount.get();
			if (account.getStatus() == AccountStatus.ACTIVE) {
				String resetToken = UUID.randomUUID().toString();
				Instant expiry = Instant.now().plus(RESET_TOKEN_VALIDITY);
				account.createPasswordResetToken(resetToken, expiry);
				accountRepository.save(account);

				emailDeliveryProvider.sendPasswordResetEmail(account.getEmail(), resetToken);
			}
		}

		// Constant timing delay to resist account enumeration attacks
		long elapsed = System.currentTimeMillis() - startTime;
		long targetDelayMs = 250;
		if (elapsed < targetDelayMs) {
			try {
				Thread.sleep(targetDelayMs - elapsed);
			}
			catch (InterruptedException ignored) {
				Thread.currentThread().interrupt();
			}
		}

		return new MessageResponse(
				"If an active account exists for that email address, a password reset link has been dispatched.");
	}

}
