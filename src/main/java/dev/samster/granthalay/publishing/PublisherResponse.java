package dev.samster.granthalay.publishing;

import java.time.Instant;

public record PublisherResponse(String id, String name, String status, String contactEmail, String payoutReference,
		Instant createdAt, Instant updatedAt) {
}
