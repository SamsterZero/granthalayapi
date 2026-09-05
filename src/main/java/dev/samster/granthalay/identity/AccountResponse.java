package dev.samster.granthalay.identity;

import java.time.Instant;

public record AccountResponse(String id, String email, AccountStatus status, Instant createdAt) {
}
