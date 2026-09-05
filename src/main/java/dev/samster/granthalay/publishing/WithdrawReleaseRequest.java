package dev.samster.granthalay.publishing;

import jakarta.validation.constraints.NotBlank;

public record WithdrawReleaseRequest(@NotBlank String reason, @NotBlank String performedBy) {
}
