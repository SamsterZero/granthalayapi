package dev.samster.granthalay.catalog;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/catalog")
public class CatalogController {

	private final CatalogQueryUseCase catalogQueryUseCase;

	public CatalogController(CatalogQueryUseCase catalogQueryUseCase) {
		this.catalogQueryUseCase = catalogQueryUseCase;
	}

	@GetMapping("/titles")
	public ResponseEntity<CatalogPageResponse<CatalogTitleSummaryResponse>> listTitles(
			@RequestParam(required = false) String search, @RequestParam(required = false) String language,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
		CatalogPageResponse<CatalogTitleSummaryResponse> response = catalogQueryUseCase.listTitles(search, language,
				page, size);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/titles/{slug}")
	public ResponseEntity<CatalogTitleDetailResponse> getTitleBySlug(@PathVariable String slug) {
		CatalogTitleDetailResponse response = catalogQueryUseCase.getTitleBySlug(slug)
			.orElseThrow(() -> new CatalogNotFoundException("Title not found for slug: " + slug));
		return ResponseEntity.ok(response);
	}

	@GetMapping("/editions/{editionId}")
	public ResponseEntity<CatalogEditionResponse> getEditionById(@PathVariable String editionId) {
		CatalogEditionResponse response = catalogQueryUseCase.getEditionById(editionId)
			.orElseThrow(() -> new CatalogNotFoundException("Edition not found for id: " + editionId));
		return ResponseEntity.ok(response);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ProblemDetail> handleIllegalArgumentException(IllegalArgumentException ex) {
		var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
		problem.setType(URI.create("about:blank"));
		problem.setTitle("Bad Request");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
	}

	@ExceptionHandler(CatalogNotFoundException.class)
	public ResponseEntity<ProblemDetail> handleNotFoundException(CatalogNotFoundException ex) {
		var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
		problem.setType(URI.create("about:blank"));
		problem.setTitle("Not Found");
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
	}

	private static class CatalogNotFoundException extends RuntimeException {

		CatalogNotFoundException(String message) {
			super(message);
		}

	}

}
