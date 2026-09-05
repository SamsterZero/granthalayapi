package dev.samster.granthalay.publishing;

import jakarta.validation.constraints.NotBlank;

public record SubmitEditionRequest(@NotBlank String publisherId, @NotBlank String editionId, @NotBlank String title,
		String isbn) {
}
