ALTER TABLE user_accounts
	ADD COLUMN password_reset_token VARCHAR(255),
	ADD COLUMN password_reset_token_expiry TIMESTAMP WITH TIME ZONE;

CREATE INDEX idx_user_accounts_password_reset_token ON user_accounts (password_reset_token);
