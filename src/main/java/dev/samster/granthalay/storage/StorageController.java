package dev.samster.granthalay.storage;

import java.io.IOException;
import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/storage")
public class StorageController {

	private final IngestEpubUseCase ingestUseCase;

	public StorageController(IngestEpubUseCase ingestUseCase) {
		this.ingestUseCase = ingestUseCase;
	}

	@PostMapping("/epubs/{editionId}")
	public ResponseEntity<StorageObjectResponse> uploadEpub(@PathVariable String editionId,
			@RequestParam("file") MultipartFile file) throws IOException {
		if (file.isEmpty()) {
			throw new IllegalArgumentException("Uploaded file cannot be empty");
		}
		byte[] bytes = file.getBytes();
		String originalFilename = file.getOriginalFilename();
		StorageObjectResponse response = ingestUseCase.ingestEpub(editionId, originalFilename, bytes);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/epubs/{editionId}")
	public ResponseEntity<StorageObjectResponse> getStorageObjectByEdition(@PathVariable String editionId) {
		StorageObjectResponse response = ingestUseCase.getStoredObject(editionId);
		return ResponseEntity.ok(response);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ProblemDetail> handleIllegalArgumentException(IllegalArgumentException ex) {
		HttpStatus status = ex.getMessage() != null && ex.getMessage().contains("No EPUB storage object found")
				? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
		var problem = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
		problem.setType(URI.create("about:blank"));
		problem.setTitle(status == HttpStatus.NOT_FOUND ? "Not Found" : "Bad Request");
		return ResponseEntity.status(status).body(problem);
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ProblemDetail> handleIllegalStateException(IllegalStateException ex) {
		var problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
		problem.setType(URI.create("about:blank"));
		problem.setTitle("Internal Server Error");
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
	}

}
