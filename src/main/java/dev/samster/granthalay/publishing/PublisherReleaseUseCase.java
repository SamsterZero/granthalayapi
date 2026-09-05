package dev.samster.granthalay.publishing;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import dev.samster.granthalay.catalog.EditionStatus;
import dev.samster.granthalay.catalog.ManageCatalogUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PublisherReleaseUseCase {

	private final PublisherRepository publisherRepository;

	private final SubmissionRepository submissionRepository;

	private final PublishingAuditEventRepository auditEventRepository;

	private final ManageCatalogUseCase manageCatalogUseCase;

	PublisherReleaseUseCase(PublisherRepository publisherRepository, SubmissionRepository submissionRepository,
			PublishingAuditEventRepository auditEventRepository, ManageCatalogUseCase manageCatalogUseCase) {
		this.publisherRepository = publisherRepository;
		this.submissionRepository = submissionRepository;
		this.auditEventRepository = auditEventRepository;
		this.manageCatalogUseCase = manageCatalogUseCase;
	}

	public PublisherEntity createPublisher(String name, String contactEmail) {
		Instant now = Instant.now();
		PublisherEntity publisher = new PublisherEntity(UUID.randomUUID().toString(), name, PublisherStatus.APPROVED,
				contactEmail, now, now);
		return publisherRepository.save(publisher);
	}

	public SubmissionResponse submitEdition(SubmitEditionRequest request, String performedBy) {
		PublisherEntity publisher = publisherRepository.findById(request.publisherId())
			.orElseThrow(() -> new IllegalArgumentException("Publisher not found: " + request.publisherId()));

		Instant now = Instant.now();
		SubmissionEntity submission = new SubmissionEntity(UUID.randomUUID().toString(), publisher, request.editionId(),
				request.title(), request.isbn(), SubmissionStatus.SUBMITTED, null, null, now, now);
		submissionRepository.save(submission);

		recordAudit(submission, publisher.getId(), PublishingAction.SUBMIT, performedBy,
				"Submitted edition for review");

		return toResponse(submission);
	}

	public SubmissionResponse reviewSubmission(String submissionId, ReviewSubmissionRequest request) {
		SubmissionEntity submission = getSubmissionEntity(submissionId);

		if (submission.getStatus() != SubmissionStatus.SUBMITTED
				&& submission.getStatus() != SubmissionStatus.IN_REVIEW) {
			throw new IllegalStateException("Cannot review submission in status: " + submission.getStatus());
		}

		Instant now = Instant.now();
		if (request.approve()) {
			submission.setStatus(SubmissionStatus.APPROVED);
			submission.setRejectionReason(null);
			recordAudit(submission, submission.getPublisher().getId(), PublishingAction.APPROVE, request.reviewedBy(),
					"Approved submission");
		}
		else {
			submission.setStatus(SubmissionStatus.REJECTED);
			submission.setRejectionReason(request.rejectionReason());
			recordAudit(submission, submission.getPublisher().getId(), PublishingAction.REJECT, request.reviewedBy(),
					"Rejected submission: " + request.rejectionReason());
		}
		submission.setUpdatedAt(now);
		submissionRepository.save(submission);

		return toResponse(submission);
	}

	public SubmissionResponse publishRelease(String submissionId, PublishReleaseRequest request) {
		SubmissionEntity submission = getSubmissionEntity(submissionId);

		if (submission.getStatus() != SubmissionStatus.APPROVED
				&& submission.getStatus() != SubmissionStatus.SCHEDULED) {
			throw new IllegalStateException("Cannot publish submission in status: " + submission.getStatus());
		}

		Instant now = Instant.now();
		if (request.scheduledAt() != null && request.scheduledAt().isAfter(now)) {
			submission.setStatus(SubmissionStatus.SCHEDULED);
			submission.setScheduledAt(request.scheduledAt());
			recordAudit(submission, submission.getPublisher().getId(), PublishingAction.SCHEDULE, request.performedBy(),
					"Scheduled release for " + request.scheduledAt());
		}
		else {
			submission.setStatus(SubmissionStatus.PUBLISHED);
			submission.setScheduledAt(now);
			manageCatalogUseCase.updateEditionStatus(submission.getEditionId(), EditionStatus.PUBLISHED);
			recordAudit(submission, submission.getPublisher().getId(), PublishingAction.PUBLISH, request.performedBy(),
					"Published edition release");
		}
		submission.setUpdatedAt(now);
		submissionRepository.save(submission);

		return toResponse(submission);
	}

	public SubmissionResponse withdrawRelease(String submissionId, WithdrawReleaseRequest request) {
		SubmissionEntity submission = getSubmissionEntity(submissionId);

		if (submission.getStatus() != SubmissionStatus.PUBLISHED
				&& submission.getStatus() != SubmissionStatus.SCHEDULED) {
			throw new IllegalStateException("Cannot withdraw submission in status: " + submission.getStatus());
		}

		Instant now = Instant.now();
		submission.setStatus(SubmissionStatus.WITHDRAWN);
		submission.setUpdatedAt(now);

		manageCatalogUseCase.updateEditionStatus(submission.getEditionId(), EditionStatus.ARCHIVED);
		recordAudit(submission, submission.getPublisher().getId(), PublishingAction.WITHDRAW, request.performedBy(),
				"Withdrew release: " + request.reason());

		submissionRepository.save(submission);
		return toResponse(submission);
	}

	public SubmissionResponse replaceEdition(String submissionId, ReplaceEditionRequest request) {
		SubmissionEntity submission = getSubmissionEntity(submissionId);

		String oldEditionId = submission.getEditionId();
		submission.setEditionId(request.newEditionId());
		submission.setUpdatedAt(Instant.now());

		manageCatalogUseCase.updateEditionStatus(oldEditionId, EditionStatus.ARCHIVED);
		manageCatalogUseCase.updateEditionStatus(request.newEditionId(), EditionStatus.PUBLISHED);

		recordAudit(submission, submission.getPublisher().getId(), PublishingAction.REPLACE, request.performedBy(),
				"Replaced edition " + oldEditionId + " with " + request.newEditionId());

		submissionRepository.save(submission);
		return toResponse(submission);
	}

	public void updateTerritoryAvailability(String editionId, UpdateAvailabilityRequest request) {
		manageCatalogUseCase.setAvailability(editionId, request.territory(), request.availableFrom(),
				request.availableUntil(), request.isAvailable());
	}

	@Transactional(readOnly = true)
	public SubmissionResponse getSubmission(String submissionId) {
		return toResponse(getSubmissionEntity(submissionId));
	}

	@Transactional(readOnly = true)
	public List<PublishingAuditEventResponse> getSubmissionHistory(String submissionId) {
		return auditEventRepository.findBySubmissionIdOrderByCreatedAtAsc(submissionId)
			.stream()
			.map(a -> new PublishingAuditEventResponse(a.getId(), a.getSubmission().getId(), a.getPublisherId(),
					a.getAction().name(), a.getPerformedBy(), a.getDetails(), a.getCreatedAt()))
			.toList();
	}

	private SubmissionEntity getSubmissionEntity(String submissionId) {
		return submissionRepository.findById(submissionId)
			.orElseThrow(() -> new IllegalArgumentException("Submission not found: " + submissionId));
	}

	private void recordAudit(SubmissionEntity submission, String publisherId, PublishingAction action,
			String performedBy, String details) {
		PublishingAuditEventEntity audit = new PublishingAuditEventEntity(UUID.randomUUID().toString(), submission,
				publisherId, action, performedBy, details, Instant.now());
		auditEventRepository.save(audit);
	}

	private SubmissionResponse toResponse(SubmissionEntity submission) {
		return new SubmissionResponse(submission.getId(), submission.getPublisher().getId(), submission.getEditionId(),
				submission.getTitle(), submission.getIsbn(), submission.getStatus().name(),
				submission.getRejectionReason(), submission.getScheduledAt(), submission.getCreatedAt(),
				submission.getUpdatedAt());
	}

}
