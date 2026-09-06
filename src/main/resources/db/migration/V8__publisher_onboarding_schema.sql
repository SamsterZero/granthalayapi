ALTER TABLE publishing_publishers ADD COLUMN payout_reference VARCHAR(255);

CREATE TABLE publishing_publisher_members (
	id VARCHAR(36) NOT NULL,
	publisher_id VARCHAR(36) NOT NULL,
	user_id VARCHAR(36) NOT NULL,
	role VARCHAR(50) NOT NULL,
	created_at TIMESTAMP WITH TIME ZONE NOT NULL,
	CONSTRAINT pk_publishing_publisher_members PRIMARY KEY (id),
	CONSTRAINT fk_publishing_publisher_members_publisher FOREIGN KEY (publisher_id) REFERENCES publishing_publishers (id) ON DELETE CASCADE,
	CONSTRAINT uq_publishing_publisher_members UNIQUE (publisher_id, user_id)
);

CREATE INDEX idx_publishing_publisher_members_publisher ON publishing_publisher_members (publisher_id);
CREATE INDEX idx_publishing_publisher_members_user ON publishing_publisher_members (user_id);
