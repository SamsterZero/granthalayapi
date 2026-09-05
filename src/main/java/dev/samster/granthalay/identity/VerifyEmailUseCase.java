package dev.samster.granthalay.identity;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VerifyEmailUseCase {

	private final UserAccountRepository accountRepository;

	public VerifyEmailUseCase(UserAccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

	@Transactional
	public MessageResponse execute(VerifyEmailRequest request) {
		var account = accountRepository.findByVerificationToken(request.token().trim())
			.filter(acc -> acc.getVerificationTokenExpiry() != null
					&& acc.getVerificationTokenExpiry().isAfter(Instant.now()))
			.orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification token"));

		account.setStatus(AccountStatus.ACTIVE);
		account.setVerificationToken(null);
		account.setVerificationTokenExpiry(null);
		account.setUpdatedAt(Instant.now());
		accountRepository.save(account);

		return new MessageResponse("Email verified and account activated successfully.");
	}

}
