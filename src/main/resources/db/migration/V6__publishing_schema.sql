CREATE TABLE publishing_publishers (
	id VARCHAR(36) NOT NULL,
	name VARCHAR(255) NOT NULL,
	status VARCHAR(50) NOT NULL,
	contact_email VARCHAR(255) NOT NULL,
	created_at TIMESTAMP WITH TIME ZONE NOT NULL,
	updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
	CONSTRAINT pk_publishing_publishers PRIMARY KEY (id)
);

CREATE INDEX idx_publishing_publishers_status ON publishing_publishers (status);

CREATE TABLE publishing_submissions (
	id VARCHAR(36) NOT NULL,
	publisher_id VARCHAR(36) NOT NULL,
	edition_id VARCHAR(36) NOT NULL,
	title VARCHAR(255) NOT NULL,
	isbn VARCHAR(20),
	status VARCHAR(50) NOT NULL,
	rejection_reason TEXT,
	scheduled_at TIMESTAMP WITH TIME ZONE,
	created_at TIMESTAMP WITH TIME ZONE NOT NULL,
	updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
	CONSTRAINT pk_publishing_submissions PRIMARY KEY (id),
	CONSTRAINT fk_publishing_submissions_publisher FOREIGN KEY (publisher_id) REFERENCES publishing_publishers (id) ON DELETE CASCADE
);

CREATE INDEX idx_publishing_submissions_publisher ON publishing_submissions (publisher_id);
CREATE INDEX idx_publishing_submissions_edition ON publishing_submissions (edition_id);
CREATE INDEX idx_publishing_submissions_status ON publishing_submissions (status);

CREATE TABLE publishing_audit_events (
	id VARCHAR(36) NOT NULL,
	submission_id VARCHAR(36) NOT NULL,
	publisher_id VARCHAR(36) NOT NULL,
	action VARCHAR(50) NOT NULL,
	performed_by VARCHAR(255) NOT NULL,
	details TEXT,
	created_at TIMESTAMP WITH TIME ZONE NOT NULL,
	CONSTRAINT pk_publishing_audit_events PRIMARY KEY (id),
	CONSTRAINT fk_publishing_audit_events_submission FOREIGN KEY (submission_id) REFERENCES publishing_submissions (id) ON DELETE CASCADE
);

CREATE INDEX idx_publishing_audit_events_submission ON publishing_audit_events (submission_id);
