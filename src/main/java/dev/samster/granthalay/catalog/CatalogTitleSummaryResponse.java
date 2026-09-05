package dev.samster.granthalay.catalog;

public record CatalogTitleSummaryResponse(String id, String slug, String title, String subtitle, String language,
		String primaryAuthorName) {
}
