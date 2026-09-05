package dev.samster.granthalay.publishing;

import jakarta.validation.constraints.NotBlank;

public record ReviewSubmissionRequest(boolean approve, String rejectionReason, @NotBlank String reviewedBy) {
}
