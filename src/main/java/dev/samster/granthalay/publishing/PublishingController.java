package dev.samster.granthalay.publishing;

import java.net.URI;
import java.security.Principal;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/publishing")
public class PublishingController {

	private final PublisherReleaseUseCase releaseUseCase;

	public PublishingController(PublisherReleaseUseCase releaseUseCase) {
		this.releaseUseCase = releaseUseCase;
	}

	@PostMapping("/submissions")
	public ResponseEntity<SubmissionResponse> submitEdition(@Valid @RequestBody SubmitEditionRequest request,
			Principal principal) {
		String performedBy = principal != null ? principal.getName() : "system";
		SubmissionResponse response = releaseUseCase.submitEdition(request, performedBy);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping("/submissions/{submissionId}/review")
	public ResponseEntity<SubmissionResponse> reviewSubmission(@PathVariable String submissionId,
			@Valid @RequestBody ReviewSubmissionRequest request) {
		SubmissionResponse response = releaseUseCase.reviewSubmission(submissionId, request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/submissions/{submissionId}/publish")
	public ResponseEntity<SubmissionResponse> publishRelease(@PathVariable String submissionId,
			@Valid @RequestBody PublishReleaseRequest request) {
		SubmissionResponse response = releaseUseCase.publishRelease(submissionId, request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/submissions/{submissionId}/withdraw")
	public ResponseEntity<SubmissionResponse> withdrawRelease(@PathVariable String submissionId,
			@Valid @RequestBody WithdrawReleaseRequest request) {
		SubmissionResponse response = releaseUseCase.withdrawRelease(submissionId, request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/submissions/{submissionId}/replace")
	public ResponseEntity<SubmissionResponse> replaceEdition(@PathVariable String submissionId,
			@Valid @RequestBody ReplaceEditionRequest request) {
		SubmissionResponse response = releaseUseCase.replaceEdition(submissionId, request);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/editions/{editionId}/availability")
	public ResponseEntity<Void> updateAvailability(@PathVariable String editionId,
			@Valid @RequestBody UpdateAvailabilityRequest request) {
		releaseUseCase.updateTerritoryAvailability(editionId, request);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/submissions/{submissionId}/history")
	public ResponseEntity<List<PublishingAuditEventResponse>> getSubmissionHistory(@PathVariable String submissionId) {
		List<PublishingAuditEventResponse> history = releaseUseCase.getSubmissionHistory(submissionId);
		return ResponseEntity.ok(history);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ProblemDetail> handleIllegalArgumentException(IllegalArgumentException ex) {
		var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
		problem.setType(URI.create("about:blank"));
		problem.setTitle("Bad Request");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ProblemDetail> handleIllegalStateException(IllegalStateException ex) {
		var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
		problem.setType(URI.create("about:blank"));
		problem.setTitle("Conflict");
		return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
	}

}
