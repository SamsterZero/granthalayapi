package dev.samster.granthalay.catalog;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ManageCatalogUseCase {

	private final TitleRepository titleRepository;

	private final ContributorRepository contributorRepository;

	private final EditionRepository editionRepository;

	ManageCatalogUseCase(TitleRepository titleRepository, ContributorRepository contributorRepository,
			EditionRepository editionRepository) {
		this.titleRepository = titleRepository;
		this.contributorRepository = contributorRepository;
		this.editionRepository = editionRepository;
	}

	public String createContributor(String name, String bio) {
		Instant now = Instant.now();
		ContributorEntity contributor = new ContributorEntity(UUID.randomUUID().toString(), name, bio, now, now);
		return contributorRepository.save(contributor).getId();
	}

	public String createTitle(String slug, String title, String subtitle, String description, String language) {
		if (titleRepository.existsBySlug(slug)) {
			throw new IllegalArgumentException("Title with slug '" + slug + "' already exists");
		}
		Instant now = Instant.now();
		TitleEntity titleEntity = new TitleEntity(UUID.randomUUID().toString(), slug, title, subtitle, description,
				language, now, now);
		return titleRepository.save(titleEntity).getId();
	}

	public void addContributorToTitle(String titleId, String contributorId, ContributorRole role, int displayOrder) {
		TitleEntity title = titleRepository.findById(titleId)
			.orElseThrow(() -> new IllegalArgumentException("Title not found: " + titleId));
		ContributorEntity contributor = contributorRepository.findById(contributorId)
			.orElseThrow(() -> new IllegalArgumentException("Contributor not found: " + contributorId));

		TitleContributorEntity tc = new TitleContributorEntity(title, contributor, role, displayOrder);
		title.getTitleContributors().add(tc);
		titleRepository.save(title);
	}

	public String addEdition(String titleId, String isbn, EditionFormat format, int editionNumber, String publisherId,
			LocalDate publishedDate, EditionStatus status) {
		TitleEntity title = titleRepository.findById(titleId)
			.orElseThrow(() -> new IllegalArgumentException("Title not found: " + titleId));

		if (isbn != null && editionRepository.existsByIsbn(isbn)) {
			throw new IllegalArgumentException("Edition with ISBN '" + isbn + "' already exists");
		}

		Instant now = Instant.now();
		EditionEntity edition = new EditionEntity(UUID.randomUUID().toString(), title, isbn, format, editionNumber,
				publisherId, publishedDate, status, now, now);
		title.getEditions().add(edition);
		titleRepository.save(title);
		return edition.getId();
	}

	public void setPrice(String editionId, String currency, long amountInCents, String territory) {
		EditionEntity edition = editionRepository.findById(editionId)
			.orElseThrow(() -> new IllegalArgumentException("Edition not found: " + editionId));

		Instant now = Instant.now();
		EditionPriceEntity price = new EditionPriceEntity(UUID.randomUUID().toString(), edition, currency,
				amountInCents, territory, now, now);
		edition.getPrices().add(price);
		editionRepository.save(edition);
	}

	public void setAvailability(String editionId, String territory, Instant availableFrom, Instant availableUntil,
			boolean isAvailable) {
		EditionEntity edition = editionRepository.findById(editionId)
			.orElseThrow(() -> new IllegalArgumentException("Edition not found: " + editionId));

		Instant now = Instant.now();
		EditionAvailabilityEntity availability = new EditionAvailabilityEntity(UUID.randomUUID().toString(), edition,
				territory, availableFrom, availableUntil, isAvailable, now, now);
		edition.getAvailability().add(availability);
		editionRepository.save(edition);
	}

	public void updateEditionStatus(String editionId, EditionStatus status) {
		EditionEntity edition = editionRepository.findById(editionId)
			.orElseThrow(() -> new IllegalArgumentException("Edition not found: " + editionId));
		edition.setStatus(status);
		edition.setUpdatedAt(Instant.now());
		editionRepository.save(edition);
	}

}
