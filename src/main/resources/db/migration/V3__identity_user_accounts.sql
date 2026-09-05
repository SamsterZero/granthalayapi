CREATE TABLE user_accounts (
	id VARCHAR(36) NOT NULL,
	email VARCHAR(255) NOT NULL UNIQUE,
	password_hash VARCHAR(255) NOT NULL,
	status VARCHAR(32) NOT NULL,
	verification_token VARCHAR(255),
	verification_token_expiry TIMESTAMP WITH TIME ZONE,
	created_at TIMESTAMP WITH TIME ZONE NOT NULL,
	updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
	CONSTRAINT pk_user_accounts PRIMARY KEY (id)
);

CREATE INDEX idx_user_accounts_email ON user_accounts (email);
CREATE INDEX idx_user_accounts_verification_token ON user_accounts (verification_token);
