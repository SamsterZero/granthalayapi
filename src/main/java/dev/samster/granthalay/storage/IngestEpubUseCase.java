package dev.samster.granthalay.storage;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class IngestEpubUseCase {

	private final StorageObjectRepository repository;

	private final ObjectStorageProvider storageProvider;

	private final EpubValidator epubValidator;

	public IngestEpubUseCase(StorageObjectRepository repository, ObjectStorageProvider storageProvider,
			EpubValidator epubValidator) {
		this.repository = repository;
		this.storageProvider = storageProvider;
		this.epubValidator = epubValidator;
	}

	public StorageObjectResponse ingestEpub(String editionId, String filename, byte[] epubBytes) {
		if (editionId == null || editionId.isBlank()) {
			throw new IllegalArgumentException("Edition ID is required");
		}

		EpubValidationResult result = epubValidator.validate(epubBytes);
		if (!result.valid()) {
			throw new IllegalArgumentException("EPUB validation failed: " + result.errorMessage());
		}

		int nextVersion = repository.findFirstByEditionIdOrderByVersionDesc(editionId)
			.map(existing -> existing.getVersion() + 1)
			.orElse(1);

		String safeFilename = (filename != null && !filename.isBlank()) ? filename : "book.epub";
		String storageKey = "epubs/" + editionId + "/v" + nextVersion + "/" + result.sha256Hash() + ".epub";

		storageProvider.store(storageKey, new ByteArrayInputStream(epubBytes), result.sizeBytes(),
				"application/epub+zip");

		Instant now = Instant.now();
		StorageObjectEntity entity = new StorageObjectEntity(UUID.randomUUID().toString(), editionId, nextVersion,
				storageKey, safeFilename, "application/epub+zip", result.sizeBytes(), result.sha256Hash(),
				StorageObjectStatus.STORED, now, now);

		StorageObjectEntity saved = repository.save(entity);
		return toResponse(saved);
	}

	@Transactional(readOnly = true)
	public StorageObjectResponse getStoredObject(String editionId) {
		if (editionId == null || editionId.isBlank()) {
			throw new IllegalArgumentException("Edition ID is required");
		}

		return repository.findFirstByEditionIdOrderByVersionDesc(editionId)
			.map(this::toResponse)
			.orElseThrow(() -> new IllegalArgumentException("No EPUB storage object found for edition: " + editionId));
	}

	private StorageObjectResponse toResponse(StorageObjectEntity entity) {
		return new StorageObjectResponse(entity.getId(), entity.getEditionId(), entity.getVersion(),
				entity.getStorageKey(), entity.getFilename(), entity.getContentType(), entity.getSizeBytes(),
				entity.getSha256Hash(), entity.getStatus().name(), entity.getCreatedAt());
	}

}
