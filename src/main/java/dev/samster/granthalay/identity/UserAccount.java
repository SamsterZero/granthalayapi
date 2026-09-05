package dev.samster.granthalay.identity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_accounts")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class UserAccount {

	@Id
	@Column(name = "id", nullable = false, length = 36)
	private String id;

	@Column(name = "email", nullable = false, unique = true)
	private String email;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 32)
	private AccountStatus status;

	@Column(name = "verification_token")
	private String verificationToken;

	@Column(name = "verification_token_expiry")
	private Instant verificationTokenExpiry;

	@Column(name = "password_reset_token")
	private String passwordResetToken;

	@Column(name = "password_reset_token_expiry")
	private Instant passwordResetTokenExpiry;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	UserAccount(String id, String email, String passwordHash, AccountStatus status, String verificationToken,
			Instant verificationTokenExpiry, Instant createdAt, Instant updatedAt) {
		this.id = id;
		this.email = email;
		this.passwordHash = passwordHash;
		this.status = status;
		this.verificationToken = verificationToken;
		this.verificationTokenExpiry = verificationTokenExpiry;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	void createPasswordResetToken(String token, Instant expiry) {
		this.passwordResetToken = token;
		this.passwordResetTokenExpiry = expiry;
		this.updatedAt = Instant.now();
	}

	void updatePasswordHash(String newPasswordHash) {
		this.passwordHash = newPasswordHash;
		this.passwordResetToken = null;
		this.passwordResetTokenExpiry = null;
		this.updatedAt = Instant.now();
	}

}
