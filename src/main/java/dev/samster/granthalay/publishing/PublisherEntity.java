package dev.samster.granthalay.publishing;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "publishing_publishers")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class PublisherEntity {

	@Id
	@Column(name = "id", nullable = false, length = 36)
	private String id;

	@Column(name = "name", nullable = false)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 50)
	private PublisherStatus status;

	@Column(name = "contact_email", nullable = false)
	private String contactEmail;

	@Column(name = "payout_reference")
	private String payoutReference;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	PublisherEntity(String id, String name, PublisherStatus status, String contactEmail, String payoutReference,
			Instant createdAt, Instant updatedAt) {
		this.id = id;
		this.name = name;
		this.status = status;
		this.contactEmail = contactEmail;
		this.payoutReference = payoutReference;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	PublisherEntity(String id, String name, PublisherStatus status, String contactEmail, Instant createdAt,
			Instant updatedAt) {
		this(id, name, status, contactEmail, null, createdAt, updatedAt);
	}

}
