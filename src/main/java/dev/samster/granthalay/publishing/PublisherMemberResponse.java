package dev.samster.granthalay.publishing;

import java.time.Instant;

public record PublisherMemberResponse(String id, String publisherId, String userId, String role, Instant createdAt) {
}
