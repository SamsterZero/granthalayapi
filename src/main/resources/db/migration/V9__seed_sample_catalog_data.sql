-- Seed sample catalog titles, contributors, editions, prices, and availability for testing

INSERT INTO catalog_titles (id, slug, title, subtitle, description, language, created_at, updated_at)
VALUES 
	('ct-1', 'the-great-gatsby', 'The Great Gatsby', 'A Novel of the Roaring Twenties', 'Set in Long Island during the Jazz Age, the story tells the tragic tale of Jay Gatsby and his unrequited love for Daisy Buchanan.', 'en', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
	('ct-2', 'pride-and-prejudice', 'Pride and Prejudice', 'A Classic Romance of Manners', 'Elizabeth Bennet navigates issues of manners, upbringing, morality, education, and marriage in the society of the British landed gentry.', 'en', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
	('ct-3', 'moby-dick', 'Moby Dick', 'The Whale', 'Sailor Ishmael recounts the obsessive quest of Ahab, captain of the whaling ship Pequod, for revenge against Moby Dick.', 'en', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
	('ct-4', 'frankenstein', 'Frankenstein', 'The Modern Prometheus', 'A young scientist creates a sentient creature in an unorthodox scientific experiment with unforeseen consequences.', 'en', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
	('ct-5', 'le-petit-prince', 'Le Petit Prince', 'Fable illustrée', 'Un jeune prince qui visite divers planètes dans l''espace, y compris la Terre, abordant les thèmes de la solitude, de l''amitié et de l''amour.', 'fr', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
	('ct-6', 'die-verwandlung', 'Die Verwandlung', 'Die Metamorphose', 'Ein Kaufmann wacht eines Morgens auf und stellt fest, dass er sich in ein ungeheures Ungeziefer verwandelt hat.', 'de', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO catalog_contributors (id, name, bio, created_at, updated_at)
VALUES
	('cc-1', 'F. Scott Fitzgerald', 'American novelist and short story writer widely regarded as one of the greatest American writers of the 20th century.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
	('cc-2', 'Jane Austen', 'English novelist known primarily for her six major novels, which interpret, critique and comment upon the British landed gentry.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
	('cc-3', 'Herman Melville', 'American novelist, short story writer, and poet of the American Renaissance period.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
	('cc-4', 'Mary Shelley', 'English novelist who wrote the Gothic novel Frankenstein; or, The Modern Prometheus.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
	('cc-5', 'Antoine de Saint-Exupéry', 'French writer, poet, aristocrat, journalist and pioneering aviator.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
	('cc-6', 'Franz Kafka', 'German-language writer of novels and short stories, widely regarded as one of the major figures of 20th-century literature.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO catalog_title_contributors (title_id, contributor_id, role, display_order)
VALUES
	('ct-1', 'cc-1', 'AUTHOR', 0),
	('ct-2', 'cc-2', 'AUTHOR', 0),
	('ct-3', 'cc-3', 'AUTHOR', 0),
	('ct-4', 'cc-4', 'AUTHOR', 0),
	('ct-5', 'cc-5', 'AUTHOR', 0),
	('ct-6', 'cc-6', 'AUTHOR', 0)
ON CONFLICT (title_id, contributor_id, role) DO NOTHING;

INSERT INTO catalog_editions (id, title_id, isbn, format, edition_number, publisher_id, published_date, status, created_at, updated_at)
VALUES
	('ce-1', 'ct-1', '9780743273565', 'EPUB', 1, 'pub-1', '1925-04-10', 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
	('ce-2', 'ct-2', '9780141439518', 'EPUB', 1, 'pub-1', '1813-01-28', 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
	('ce-3', 'ct-3', '9780142437247', 'EPUB', 1, 'pub-1', '1851-10-18', 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
	('ce-4', 'ct-4', '9780141439471', 'EPUB', 1, 'pub-1', '1818-01-01', 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
	('ce-5', 'ct-5', '9780156012195', 'EPUB', 1, 'pub-1', '1943-04-06', 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
	('ce-6', 'ct-6', '9780811200783', 'EPUB', 1, 'pub-1', '1915-10-01', 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO catalog_edition_prices (id, edition_id, currency, amount_in_cents, territory, created_at, updated_at)
VALUES
	('cep-1', 'ce-1', 'USD', 999, 'WORLD', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
	('cep-2', 'ce-2', 'USD', 799, 'WORLD', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
	('cep-3', 'ce-3', 'USD', 1299, 'WORLD', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
	('cep-4', 'ce-4', 'USD', 899, 'WORLD', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
	('cep-5', 'ce-5', 'EUR', 699, 'WORLD', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
	('cep-6', 'ce-6', 'EUR', 599, 'WORLD', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO catalog_edition_availability (id, edition_id, territory, is_available, created_at, updated_at)
VALUES
	('cea-1', 'ce-1', 'WORLD', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
	('cea-2', 'ce-2', 'WORLD', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
	('cea-3', 'ce-3', 'WORLD', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
	('cea-4', 'ce-4', 'WORLD', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
	('cea-5', 'ce-5', 'WORLD', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
	('cea-6', 'ce-6', 'WORLD', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;
