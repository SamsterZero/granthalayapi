package dev.samster.granthalay.publishing;

import jakarta.validation.constraints.NotBlank;

public record ReplaceEditionRequest(@NotBlank String newEditionId, @NotBlank String performedBy) {
}
