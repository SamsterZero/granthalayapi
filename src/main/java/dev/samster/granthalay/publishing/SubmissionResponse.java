package dev.samster.granthalay.publishing;

import java.time.Instant;

public record SubmissionResponse(String id, String publisherId, String editionId, String title, String isbn,
		String status, String rejectionReason, Instant scheduledAt, Instant createdAt, Instant updatedAt) {
}
