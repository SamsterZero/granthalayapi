package dev.samster.granthalay.publishing;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OnboardPublisherRequest(@NotBlank String name, @NotBlank @Email String contactEmail,
		String payoutReference) {
}
