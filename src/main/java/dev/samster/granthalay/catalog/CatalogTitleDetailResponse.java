package dev.samster.granthalay.catalog;

import java.util.List;

public record CatalogTitleDetailResponse(String id, String slug, String title, String subtitle, String description,
		String language, List<CatalogContributorResponse> contributors, List<CatalogEditionResponse> editions) {
}
