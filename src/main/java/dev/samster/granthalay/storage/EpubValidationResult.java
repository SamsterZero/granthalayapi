package dev.samster.granthalay.storage;

public record EpubValidationResult(boolean valid, String errorMessage, long sizeBytes, String sha256Hash,
		String rootfilePath) {
	public static EpubValidationResult success(long sizeBytes, String sha256Hash, String rootfilePath) {
		return new EpubValidationResult(true, null, sizeBytes, sha256Hash, rootfilePath);
	}

	public static EpubValidationResult failure(String errorMessage) {
		return new EpubValidationResult(false, errorMessage, 0L, null, null);
	}
}
