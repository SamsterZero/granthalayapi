CREATE TABLE catalog_titles (
	id VARCHAR(36) NOT NULL,
	slug VARCHAR(255) NOT NULL UNIQUE,
	title VARCHAR(255) NOT NULL,
	subtitle VARCHAR(255),
	description TEXT,
	language VARCHAR(10) NOT NULL,
	created_at TIMESTAMP WITH TIME ZONE NOT NULL,
	updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
	CONSTRAINT pk_catalog_titles PRIMARY KEY (id)
);

CREATE INDEX idx_catalog_titles_slug ON catalog_titles (slug);
CREATE INDEX idx_catalog_titles_language ON catalog_titles (language);

CREATE TABLE catalog_contributors (
	id VARCHAR(36) NOT NULL,
	name VARCHAR(255) NOT NULL,
	bio TEXT,
	created_at TIMESTAMP WITH TIME ZONE NOT NULL,
	updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
	CONSTRAINT pk_catalog_contributors PRIMARY KEY (id)
);

CREATE TABLE catalog_title_contributors (
	title_id VARCHAR(36) NOT NULL,
	contributor_id VARCHAR(36) NOT NULL,
	role VARCHAR(50) NOT NULL,
	display_order INT NOT NULL DEFAULT 0,
	CONSTRAINT pk_catalog_title_contributors PRIMARY KEY (title_id, contributor_id, role),
	CONSTRAINT fk_catalog_title_contributors_title FOREIGN KEY (title_id) REFERENCES catalog_titles (id) ON DELETE CASCADE,
	CONSTRAINT fk_catalog_title_contributors_contributor FOREIGN KEY (contributor_id) REFERENCES catalog_contributors (id) ON DELETE RESTRICT
);

CREATE INDEX idx_catalog_title_contributors_title ON catalog_title_contributors (title_id);
CREATE INDEX idx_catalog_title_contributors_contributor ON catalog_title_contributors (contributor_id);

CREATE TABLE catalog_editions (
	id VARCHAR(36) NOT NULL,
	title_id VARCHAR(36) NOT NULL,
	isbn VARCHAR(20) UNIQUE,
	format VARCHAR(50) NOT NULL,
	edition_number INT NOT NULL DEFAULT 1,
	publisher_id VARCHAR(36),
	published_date DATE,
	status VARCHAR(50) NOT NULL,
	created_at TIMESTAMP WITH TIME ZONE NOT NULL,
	updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
	CONSTRAINT pk_catalog_editions PRIMARY KEY (id),
	CONSTRAINT fk_catalog_editions_title FOREIGN KEY (title_id) REFERENCES catalog_titles (id) ON DELETE CASCADE
);

CREATE INDEX idx_catalog_editions_title ON catalog_editions (title_id);
CREATE INDEX idx_catalog_editions_isbn ON catalog_editions (isbn);
CREATE INDEX idx_catalog_editions_status ON catalog_editions (status);

CREATE TABLE catalog_edition_prices (
	id VARCHAR(36) NOT NULL,
	edition_id VARCHAR(36) NOT NULL,
	currency VARCHAR(3) NOT NULL,
	amount_in_cents BIGINT NOT NULL,
	territory VARCHAR(10) NOT NULL,
	created_at TIMESTAMP WITH TIME ZONE NOT NULL,
	updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
	CONSTRAINT pk_catalog_edition_prices PRIMARY KEY (id),
	CONSTRAINT fk_catalog_edition_prices_edition FOREIGN KEY (edition_id) REFERENCES catalog_editions (id) ON DELETE CASCADE,
	CONSTRAINT uq_catalog_edition_prices UNIQUE (edition_id, currency, territory)
);

CREATE INDEX idx_catalog_edition_prices_edition ON catalog_edition_prices (edition_id);

CREATE TABLE catalog_edition_availability (
	id VARCHAR(36) NOT NULL,
	edition_id VARCHAR(36) NOT NULL,
	territory VARCHAR(10) NOT NULL,
	available_from TIMESTAMP WITH TIME ZONE,
	available_until TIMESTAMP WITH TIME ZONE,
	is_available BOOLEAN NOT NULL DEFAULT TRUE,
	created_at TIMESTAMP WITH TIME ZONE NOT NULL,
	updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
	CONSTRAINT pk_catalog_edition_availability PRIMARY KEY (id),
	CONSTRAINT fk_catalog_edition_availability_edition FOREIGN KEY (edition_id) REFERENCES catalog_editions (id) ON DELETE CASCADE,
	CONSTRAINT uq_catalog_edition_availability UNIQUE (edition_id, territory)
);

CREATE INDEX idx_catalog_edition_availability_edition ON catalog_edition_availability (edition_id);
