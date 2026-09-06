package dev.samster.granthalay.storage;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "storage_objects")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class StorageObjectEntity {

	@Id
	@Column(name = "id", nullable = false, length = 36)
	private String id;

	@Column(name = "edition_id", nullable = false, length = 36)
	private String editionId;

	@Column(name = "version", nullable = false)
	private int version;

	@Column(name = "storage_key", nullable = false, unique = true)
	private String storageKey;

	@Column(name = "filename", nullable = false)
	private String filename;

	@Column(name = "content_type", nullable = false, length = 100)
	private String contentType;

	@Column(name = "size_bytes", nullable = false)
	private long sizeBytes;

	@Column(name = "sha256_hash", nullable = false, length = 64)
	private String sha256Hash;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 50)
	private StorageObjectStatus status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	StorageObjectEntity(String id, String editionId, int version, String storageKey, String filename,
			String contentType, long sizeBytes, String sha256Hash, StorageObjectStatus status, Instant createdAt,
			Instant updatedAt) {
		this.id = id;
		this.editionId = editionId;
		this.version = version;
		this.storageKey = storageKey;
		this.filename = filename;
		this.contentType = contentType;
		this.sizeBytes = sizeBytes;
		this.sha256Hash = sha256Hash;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

}
