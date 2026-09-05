package dev.samster.granthalay.catalog;

import java.time.Instant;

public record EditionAvailabilityResponse(String territory, boolean isAvailable, Instant availableFrom,
		Instant availableUntil) {
}
