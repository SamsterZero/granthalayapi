CREATE TABLE storage_objects (
	id VARCHAR(36) NOT NULL,
	edition_id VARCHAR(36) NOT NULL,
	version INT NOT NULL DEFAULT 1,
	storage_key VARCHAR(255) NOT NULL,
	filename VARCHAR(255) NOT NULL,
	content_type VARCHAR(100) NOT NULL,
	size_bytes BIGINT NOT NULL,
	sha256_hash VARCHAR(64) NOT NULL,
	status VARCHAR(50) NOT NULL,
	created_at TIMESTAMP WITH TIME ZONE NOT NULL,
	updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
	CONSTRAINT pk_storage_objects PRIMARY KEY (id),
	CONSTRAINT uq_storage_objects_key UNIQUE (storage_key),
	CONSTRAINT uq_storage_objects_edition_version UNIQUE (edition_id, version)
);

CREATE INDEX idx_storage_objects_edition ON storage_objects (edition_id);
CREATE INDEX idx_storage_objects_key ON storage_objects (storage_key);
CREATE INDEX idx_storage_objects_status ON storage_objects (status);
