package dev.samster.granthalay.catalog;

import java.time.LocalDate;
import java.util.List;

public record CatalogEditionResponse(String id, String isbn, String format, int editionNumber, String publisherId,
		LocalDate publishedDate, String status, List<EditionPriceResponse> prices,
		List<EditionAvailabilityResponse> availability) {
}
