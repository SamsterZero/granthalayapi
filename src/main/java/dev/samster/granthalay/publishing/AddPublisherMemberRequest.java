package dev.samster.granthalay.publishing;

import jakarta.validation.constraints.NotBlank;

public record AddPublisherMemberRequest(@NotBlank String userId, @NotBlank String role) {
}
