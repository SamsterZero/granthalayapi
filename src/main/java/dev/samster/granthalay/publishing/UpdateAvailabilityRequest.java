package dev.samster.granthalay.publishing;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;

public record UpdateAvailabilityRequest(@NotBlank String territory, Instant availableFrom, Instant availableUntil,
		boolean isAvailable, @NotBlank String performedBy) {
}
