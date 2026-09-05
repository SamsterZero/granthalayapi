package dev.samster.granthalay.identity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RequestPasswordResetRequest(
		@NotBlank(message = "Email is required") @Email(message = "Invalid email address format") String email) {
}
