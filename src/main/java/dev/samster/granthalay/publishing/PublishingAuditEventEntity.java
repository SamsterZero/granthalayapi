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
@Table(name = "publishing_audit_events")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class PublishingAuditEventEntity {

	@Id
	@Column(name = "id", nullable = false, length = 36)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "submission_id", nullable = false)
	private SubmissionEntity submission;

	@Column(name = "publisher_id", nullable = false, length = 36)
	private String publisherId;

	@Enumerated(EnumType.STRING)
	@Column(name = "action", nullable = false, length = 50)
	private PublishingAction action;

	@Column(name = "performed_by", nullable = false)
	private String performedBy;

	@Column(name = "details", columnDefinition = "TEXT")
	private String details;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	PublishingAuditEventEntity(String id, SubmissionEntity submission, String publisherId, PublishingAction action,
			String performedBy, String details, Instant createdAt) {
		this.id = id;
		this.submission = submission;
		this.publisherId = publisherId;
		this.action = action;
		this.performedBy = performedBy;
		this.details = details;
		this.createdAt = createdAt;
	}

}
