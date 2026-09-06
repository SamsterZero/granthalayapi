package dev.samster.granthalay.storage;

import java.time.Instant;

public record StorageObjectResponse(String id, String editionId, int version, String storageKey, String filename,
		String contentType, long sizeBytes, String sha256Hash, String status, Instant createdAt) {
}
