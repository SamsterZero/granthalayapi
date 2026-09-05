package dev.samster.granthalay.identity;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(@NotBlank(message = "Verification token is required") String token) {
}
