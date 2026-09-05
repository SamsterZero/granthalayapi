package dev.samster.granthalay.identity;

import java.time.Instant;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResetPasswordUseCase {

	private final UserAccountRepository accountRepository;

	private final PasswordEncoder passwordEncoder;

	private final RevokeUserSessionsUseCase revokeUserSessionsUseCase;

	public ResetPasswordUseCase(UserAccountRepository accountRepository, PasswordEncoder passwordEncoder,
			RevokeUserSessionsUseCase revokeUserSessionsUseCase) {
		this.accountRepository = accountRepository;
		this.passwordEncoder = passwordEncoder;
		this.revokeUserSessionsUseCase = revokeUserSessionsUseCase;
	}

	@Transactional
	public MessageResponse execute(ResetPasswordRequest request) {
		var account = accountRepository.findByPasswordResetToken(request.token())
			.orElseThrow(() -> new IllegalArgumentException("Invalid or expired password reset token"));

		if (account.getPasswordResetTokenExpiry() == null
				|| Instant.now().isAfter(account.getPasswordResetTokenExpiry())) {
			throw new IllegalArgumentException("Invalid or expired password reset token");
		}

		String newHash = passwordEncoder.encode(request.newPassword());
		account.updatePasswordHash(newHash);
		accountRepository.save(account);

		// Revoke all existing sessions for security
		revokeUserSessionsUseCase.execute(account.getEmail());

		return new MessageResponse("Password reset successfully. You may now sign in with your new password.");
	}

}
