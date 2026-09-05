package dev.samster.granthalay.publishing;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;

public record PublishReleaseRequest(Instant scheduledAt, @NotBlank String performedBy) {
}
