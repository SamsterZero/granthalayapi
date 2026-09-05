package dev.samster.granthalay.publishing;

import java.time.Instant;

public record PublishingAuditEventResponse(String id, String submissionId, String publisherId, String action,
		String performedBy, String details, Instant createdAt) {
}
