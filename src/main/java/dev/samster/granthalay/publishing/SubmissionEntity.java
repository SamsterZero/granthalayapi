package dev.samster.granthalay.publishing;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "publishing_submissions")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class SubmissionEntity {

	@Id
	@Column(name = "id", nullable = false, length = 36)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "publisher_id", nullable = false)
	private PublisherEntity publisher;

	@Column(name = "edition_id", nullable = false, length = 36)
	private String editionId;

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "isbn", length = 20)
	private String isbn;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 50)
	private SubmissionStatus status;

	@Column(name = "rejection_reason", columnDefinition = "TEXT")
	private String rejectionReason;

	@Column(name = "scheduled_at")
	private Instant scheduledAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	SubmissionEntity(String id, PublisherEntity publisher, String editionId, String title, String isbn,
			SubmissionStatus status, String rejectionReason, Instant scheduledAt, Instant createdAt,
			Instant updatedAt) {
		this.id = id;
		this.publisher = publisher;
		this.editionId = editionId;
		this.title = title;
		this.isbn = isbn;
		this.status = status;
		this.rejectionReason = rejectionReason;
		this.scheduledAt = scheduledAt;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

}
