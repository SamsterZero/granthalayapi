package dev.samster.granthalay.catalog;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CatalogQueryUseCase {

	private final TitleRepository titleRepository;

	private final EditionRepository editionRepository;

	CatalogQueryUseCase(TitleRepository titleRepository, EditionRepository editionRepository) {
		this.titleRepository = titleRepository;
		this.editionRepository = editionRepository;
	}

	public CatalogPageResponse<CatalogTitleSummaryResponse> listTitles(String search, String language, int page,
			int size) {
		int boundedSize = Math.max(1, Math.min(size, 100));
		int boundedPage = Math.max(0, page);
		var pageable = PageRequest.of(boundedPage, boundedSize, Sort.by("title").ascending());
		var pageResult = titleRepository.findCatalogTitles(search, language, pageable);

		var summaries = pageResult.getContent().stream().map(this::toSummaryResponse).toList();

		return new CatalogPageResponse<>(summaries, pageResult.getNumber(), pageResult.getSize(),
				pageResult.getTotalElements(), pageResult.getTotalPages());
	}

	public Optional<CatalogTitleDetailResponse> getTitleBySlug(String slug) {
		return titleRepository.findBySlug(slug).map(this::toDetailResponse);
	}

	public Optional<CatalogEditionResponse> getEditionById(String editionId) {
		return editionRepository.findById(editionId).map(this::toEditionResponse);
	}

	private CatalogTitleSummaryResponse toSummaryResponse(TitleEntity title) {
		String primaryAuthor = title.getTitleContributors()
			.stream()
			.filter(tc -> tc.getId().getRole() == ContributorRole.AUTHOR)
			.sorted(Comparator.comparingInt(TitleContributorEntity::getDisplayOrder))
			.map(tc -> tc.getContributor().getName())
			.findFirst()
			.orElse(null);

		return new CatalogTitleSummaryResponse(title.getId(), title.getSlug(), title.getTitle(), title.getSubtitle(),
				title.getLanguage(), primaryAuthor);
	}

	private CatalogTitleDetailResponse toDetailResponse(TitleEntity title) {
		var contributors = title.getTitleContributors()
			.stream()
			.sorted(Comparator.comparingInt(TitleContributorEntity::getDisplayOrder))
			.map(tc -> new CatalogContributorResponse(tc.getContributor().getId(), tc.getContributor().getName(),
					tc.getId().getRole().name(), tc.getContributor().getBio()))
			.toList();

		var editions = title.getEditions().stream().map(this::toEditionResponse).toList();

		return new CatalogTitleDetailResponse(title.getId(), title.getSlug(), title.getTitle(), title.getSubtitle(),
				title.getDescription(), title.getLanguage(), contributors, editions);
	}

	private CatalogEditionResponse toEditionResponse(EditionEntity edition) {
		var prices = edition.getPrices()
			.stream()
			.map(p -> new EditionPriceResponse(p.getCurrency(), p.getAmountInCents(), p.getTerritory()))
			.toList();

		var availability = edition.getAvailability()
			.stream()
			.map(a -> new EditionAvailabilityResponse(a.getTerritory(), a.isAvailable(), a.getAvailableFrom(),
					a.getAvailableUntil()))
			.toList();

		return new CatalogEditionResponse(edition.getId(), edition.getIsbn(), edition.getFormat().name(),
				edition.getEditionNumber(), edition.getPublisherId(), edition.getPublishedDate(),
				edition.getStatus().name(), prices, availability);
	}

}
